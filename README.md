# Twitter HashTag Search App – Сеопфатна документација за Реактивно програмирање (за почетници)

## 1. Вовед
Оваа апликација претставува целосен пример како да се изгради веб систем кој работи во реално време со помош на реактивно програмирање. Корисникот внесува хаштаг (пример: finki) и апликацијата преку backend се поврзува со Twitter API, ги презема релевантните твитови, ги зачувува во база и ги стримира кон frontend. Корисникот ги гледа пораките веднаш штом пристигнат – без освежување на страницата.

Оваа документација содржи:
- Што е реактивно програмирање и зошто е важно.
- Основни концепти: Publisher/Subscriber, Mono, Flux, backpressure, hot vs cold streams.
- Како во Spring WebFlux се користат Flux/Mono, WebClient и R2DBC.
- Како функционира SSE (Server-Sent Events) и зошто е добар избор за еднонасочен стрим.
- Детален преглед на нашата имплементација (контролери, сервиси, репозиториуми, конфигурации и frontend делот).
- Како да стартувате локално, да тестирате и да дебагирате.

---

## 2. Основи на реактивно програмирање (објаснето едноставно)
Реактивното програмирање е стил на програмирање каде кодот реагира на настани и податоци што пристигнуваат постепено. Наместо да чекаме долг процес да заврши (blocking), ние ги обработуваме податоците онолку брзо колку што пристигнуваат (non-blocking, asynchronous).

### 2.1 Blocking vs. Reactive
- Blocking (класично): бараш нешто и чекаш одговор; додека чекаш, ништо друго не се случува во истиот thread. Скалирањето е тешко.
- Reactive (не-блокирачки): бараш нешто и кога ќе има резултат, добиваш настан со податок. Во меѓувреме thread-овите се слободни да обработуваат други барања.

Blocking пример:
```java
List<Tweet> tweets = api.getTweets("#java");
```
Reactive пример:
```java
Flux<Tweet> tweets = tweetRepository.findAllByHashtag("#java");
```

### 2.2 Publisher/Subscriber
- Publisher (извор): испорачува елементи (податоци) со тек на време.
- Subscriber (претплатник): се претплаќа на изворот и добива елементи штом се достапни.
- Flux: publisher што може да емитува 0..N елементи.
- Mono: publisher што емитува 0..1 елемент.

### 2.3 Cold vs Hot streams
- Cold: секој subscriber добива „свое“ емитување од почеток.
- Hot: емитувачот пушта податоци без разлика има ли слушатели; subscriber-ите се приклучуваат во тек.
Во нашиот проект, SSE стримот од контролерот е блиску до hot stream за сите клиенти што се приклучуваат во моментот.

### 2.4 Backpressure
Backpressure е механизам да се избегне „прелевање“ на потрошувачот. Reactor има оператори и стратегии (buffer, drop, latest) за контролирање на брзината. Во нашиот SSE сценарио, стапката е умерена и се потпираме на природното темпо на пристигнување на твитовите и на периодичните пребарувања.

---

## 3. Архитектура на апликацијата
Тек на податоци (високо ниво):
1. Корисник внесува хаштаг во React frontend.
2. Frontend отвора SSE конекција до backend: /searchTweets/searchtweet?hashtag=...
3. Backend решава дали има кеширани твитови во база (R2DBC) или треба да ги земе од Twitter API.
4. Ако нема, backend преку WebClient го вика внатрешниот API /twitter/search което потоа повикува Twitter API, ги зачувува твитовите и мета-податоците.
5. Контролерот стримира Flux<Tweet> назад до frontend преку SSE.
6. Frontend ги прикажува твитовите веднаш штом пристигнат и овозможува приказ на најнов/најстар твит според мета-податоци.

Технолошки столбови:
- Spring WebFlux (реактивен web stack)
- Project Reactor (Flux/Mono)
- WebClient (не-блокирачки HTTP клиент)
- R2DBC (реактивен пристап до база)
- SSE (Server-Sent Events) за еднонасочен стрим кон браузер
- React Frontend со EventSource

---

## 4. Backend – Конфигурации
### 4.1 WebClientConfig
```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```
- Регистрира WebClient.Builder bean за инјектирање каде што е потребно.

### 4.2 R2dbcConfig
```java
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
```
- Овозможува реактивни репозиториуми и ентитет темплејт.

### 4.3 Конфигурација на апликација
- application.yml / application.properties: содржи twitter.api.url и twitter.api.bearer-token, како и R2DBC connection параметри. Проверете ги и пополнете ги пред стартување.

---

## 5. Backend – Домен и Репозиториуми
### 5.1 Доменски класи
- Tweet: id, text, authorId, hashtag.
- MetaDataForTweets: newestId, oldestId, resultCount, hashtag.

### 5.2 Репозиториуми
```java
@Repository
public interface TweetRepository extends ReactiveCrudRepository<Tweet, Long> {
    Flux<Tweet> findAllByHashtag(String hashtag);
}
```
- ReactiveCrudRepository обезбедува реактивни CRUD методи.
- findAllByHashtag враќа Flux<Tweet> – природно вклопување во SSE стримирање.

---

## 6. Backend – Сервиси
Клучни методи:
- getHashtag(String hashtag): Mono<Boolean> дали постојат твитови со тој хаштаг во база.
- getTweets(String hashtag): Flux<Tweet> – зема од база.
- fetchandSaveTweets(String hashtag):
  - Прави повик до локален endpoint /twitter/search?hashtag=...
  - Кога ќе се заврши повикот, повторно чита од базата преку getTweets(hashtag) и враќа Flux<Tweet>.
- streamTweetsByHashtag(String hashtag): пример на периодично емитување со Flux.interval и извлекување од база.

Шаблон за реактивна обработка во fetchandSaveTweets:
```java
public Flux<Tweet> fetchandSaveTweets(String Hashtag) {
    String url = "http://localhost:8080/twitter/search?hashtag=" + Hashtag;
    System.out.println("Calling URL: " + url);

    // Правиме повик преку WebClient
    return webClientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToFlux(String.class)
            .flatMap(response -> {
                System.out.println("Response from search: " + response);
                return getTweets(Hashtag);
            })
            .doOnError(e -> System.out.println("Error during fetch: " + e.getMessage()));
}
```

- getMetaDataForTweet(String hashtag): враќа Flux<MetaDataForTweets> филтрирано по хаштаг.

---

## 7. Backend – API слој
### 7.1 Внатрешен API кон Twitter – TwitterApiController
Главни чекори во /twitter/search:
1) Гради URL за Twitter API (со bearer token од конфигурација).
2) WebClient повик со .retrieve() и .onStatus за обработка на грешки.
3) .bodyToMono(String) за да се земе raw JSON.
4) .publishOn(Schedulers.boundedElastic()) – обработка на JSON парсирање во позадински thread pool (CPU/IO работа).
5) Parsing со ObjectMapper во JsonNode; градење листа Tweet објекти само ако има валидни полиња.
6) Зачувување на твитовите со tweetRepository.saveAll(...).collectList().
7) Зачувување на мета-податоците (newest_id, oldest_id, result_count) во MetaDataForTweetRepository.
8) Враќа Mono<String> како резиме порака.

Фрагмент:
```java
return webClientBuilder.build()
    .get()
    .uri(url)
    .header("Authorization", "Bearer " + bearerToken)
    .retrieve()
    .onStatus(HttpStatusCode::isError, response -> ...)
    .bodyToMono(String.class)
    .publishOn(Schedulers.boundedElastic())
    .flatMap(response -> { /* parse + save */ })
    .onErrorResume(e -> Mono.just("Error fetching tweets: " + e.getMessage()));
```

Зошто Mono а не Flux? – Овој метод прави еден HTTP повик, обработува JSON и враќа една порака. Самиот стрим на твитови кон фронтенд го правиме на друго место.

### 7.2 SSE кон фронтенд – TweetsToFrontendController
Endpoint-и:
- GET /searchTweets/searchtweet (produces = text/event-stream):
```java
return tweetService.getHashtag(hashtag)
    .flatMapMany(exists -> exists ? tweetService.getTweets(hashtag)
                                  : tweetService.fetchandSaveTweets(hashtag));
```
- Ако постојат твитови во база, ги стримира.
- Ако не постојат, прво се иницира вчитување/зачувување (преку локалниот контролер што го повикува Twitter API), па потоа се стримира од база.

- GET /searchTweets/metaDataForTweet: враќа Flux<MetaDataForTweets> за даден хаштаг.
- GET /searchTweets/stream (SSE): пример како периодично се емитуваат твитови со Flux.interval.

SSE објаснување:
- produces = MediaType.TEXT_EVENT_STREAM_VALUE кажува на Spring да испраќа text/event-stream.
- Клиентот добива настани по ред, секој е JSON од Tweet, што React го парсира и додава во листата.

---

## 8. Frontend – Реално време со EventSource
Клучен useEffect:
```javascript
useEffect(() => {
  if (hashtag.trim() === "") return;
  const eventSource = new EventSource(`http://localhost:8080/searchTweets/searchtweet?hashtag=${hashtag}`);
  eventSource.onmessage = (event) => {
    const newTweet = JSON.parse(event.data);
    setTweets((prevTweets) => [newTweet, ...prevTweets]);
  };
  eventSource.onerror = (err) => {
    console.error("EventSource failed:", err);
    eventSource.close();
  };
  return () => { eventSource.close(); };
}, [hashtag]);
```
- Со промена на hashtag, се отвора нова SSE конекција.
- Секој пристигнат настан е JSON Tweet; се додава најгоре во листата.

Добиени мета-податоци:
- Преку axios GET /searchTweets/metaDataForTweet?hashtag=... се прикажуваат најнов/најстар твит (ShowNewestPost/ShowOldestPost).

Компонента ShowNewestPost.js:
- Наоѓа твит во props.tweets што има authorId еднаков на newest пост id и го прикажува.

---

## 9. Error handling и robustness
Во TwitterApiController:
- .onStatus(HttpStatusCode::isError, ...) за фаќање HTTP грешки и логирање на body.
- .onErrorResume(e -> Mono.just("Error fetching tweets: " + e.getMessage())) за контролирано враќање порака наместо пад.

Во TweetServiceImpl.fetchandSaveTweets:
- .doOnError за логирање при проблем.

Frontend EventSource:
- onerror handler го затвора конекцијата за да се избегне бескрајно повторување при проблеми.

Предлози за унапредување (опционално):
- Retry/backoff за повик до Twitter API.
- Timeout-и во WebClient.
- Систематско логирање (SLF4J) наместо System.out.println.
- Валидирање на влезен hashtag.

---

## 10. База на податоци (R2DBC) и миграции
- R2DBC обезбедува реактивен пристап до базата – без блокирање.
- Репозиториумите се реактивни (ReactiveCrudRepository).
- Во src/main/resources/db/migration/ се наоѓаат SQL-скрипти за креирање на табели за твитови и мета-податоци.

Добиените твитови се зачувуваат пред да се стримираат, за да имаме кеш и повторна употреба без нов повик ако истиот хаштаг се бара повторно.

---

## 11. Како да стартувате локално
1) Поставете ги потребните конфигурации:
   - application.properties или application.yml: пополнете twitter.api.url и twitter.api.bearer-token.
   - Конфигурирајте база (R2DBC URL, username/password ако е потребно).
2) Backend:
   - Стартувајте Spring Boot апликацијата (TwitterPostSearchApplication) од IDE или со mvn spring-boot:run.
3) Frontend:
   - Во директориум frontend: npm install, потоа npm start.
4) Отворете http://localhost:3000, внесете hashtag (без #), кликнете Search Tweets.
5) Гледајте како твитовите пристигнуваат во реално време.

---

## 12. Чести прашања (FAQ)
- Зошто SSE наместо WebSocket?
  - SSE е поедноставен за еднонасочен стрим од сервер кон клиент, со вграден reconnection. Доволно е за нашиот случај.
- Каде е реактивноста во базата?
  - Через R2DBC: методите на репозиториумите враќаат Flux/Mono и не блокираат thread-ови.
- Може ли да се враќа Mono<List<Tweet>> наместо Flux<Tweet>?
  - Технички може, но ќе изгубите придобивки од стримирање во реално време и backpressure.

---

## 13. Детален пример: од внес на hashtag до прикажан твит
1) Корисник внесува finki.
2) Frontend отвора EventSource кон /searchTweets/searchtweet?hashtag=finki.
3) TweetsToFrontendController проверува дали има твитови во база:
   - Ако има: враќа Flux<Tweet> од база (реактивно стримирање).
   - Ако нема: повикува fetchandSaveTweets, што внатрешно повикува /twitter/search; по зачувување, чита од база и стримира назад.
4) Frontend добива JSON твитови и ги додава во листата во state.
5) Паралелно, фронтенд може да повика /searchTweets/metaDataForTweet за newest/oldest информации и да ги прикаже.

---

## 14. Најдобри практики и наредни чекори
- Додавање на DTO слој за одвојување на ентитети од транспортни модели.
- Поголема валидација и sanitization на hashtag.
- Користење на Sinks/EmitterProcessor (или новите Sinks) за пуш-поток кон повеќе клиенти ако пристигнуваат твитови независно од барањата.
- Интеграциски тестови со WebTestClient за SSE.
- Структурирано логирање и метрики (Micrometer).

---

## 15. Заклучок
Оваа апликација демонстрира комплетен реактивен тек: не-блокирачки HTTP повици (WebClient), реактивна база (R2DBC), реактивни стримови (Flux/Mono) и реално време испорака преку SSE. Со ваква архитектура добиваме подобра искористеност на ресурси, лесна скалабилност и подобро корисничко искуство.

Напомена: Ако сте почетник, препорачливо е да поиграте со кодот – сменете го hashtag, додајте логови, вметнете оператори како map/filter/take на Flux, и набљудувајте како се однесува апликацијата во реално време.
