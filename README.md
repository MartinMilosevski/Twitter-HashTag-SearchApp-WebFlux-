# Twitter HashTag Search App – Сеопфатна документација за Реактивно програмирање

## 1. Вовед

### Што е WebFlux и кога се користи

**Што е WebFlux?**

Spring WebFlux е реактивен веб-фрејмворк за изградба на асинхрони, неблокирачки веб апликации. Се базира на Project Reactor и користи реактивни типови: Mono (0 или 1 елемент) и Flux (0..N елементи). Наместо класичен thread-per-request модел, WebFlux работи со event-loop и реактивни стримови, што дозволува голема скалабилност со помалку ресурси.

Клучни компоненти и како работат:

    - Mono / Flux — претставуваат реактивни стримови; сервисите враќаат Mono/Flux наместо непосредни вредности.

	- WebClient — неблокирачки HTTP клиент за повикување надворешни API-ја (на пример, Twitter API).

	- R2DBC / реактивни драйвери — за неблокирачко читање/запишување во база.

	- SSE / WebSocket — начини за испраќање на податоци од серверот до клиентот во реално време (SSE е еднонасочно, WebSocket двонасочно).

	- Backpressure — механизам за контрола на проток на податоци (да не се преоптовари потрошувачот).

	- Hot vs Cold streams — Cold: секој претплатник добива свој стрим; Hot: стримот е за сите и емитува податоци во реално време.

**Кога да го користиш WebFlux ?** 

    - Кога апликацијата работи со реално време (стримови, нотификации, live feed).

	- Кога очекуваш многу конекции и сакаш подобра употреба на ресурси (I/O bound работни товари).

	- Кога користиш неблокирачки извори (реактивни бази, асинхрони API повици).

	- Ако имаш апликација што треба да обработува постојани настани (IoT, finance ticks, Twitter stream).

**Кога да не го користиш ?**

    - За едноставни CRUD апликации со ниско оптоварување може да е поедноставно и појасно да се користи Spring MVC (blocking).

	- Ако целата технологија околу тебе (библиотеки, драйвери) не поддржува реактивен модел — нема голема корист.

Кратка техничка слика на архитектурата
**Клиент → SSE/WebSocket ← Сервер (WebFlux контролер) → Сервис (WebClient кон Twitter API, реактивни репозиториуми) → База (R2DBC).**

Податоците се движат низ целиот систем преку Mono и Flux, задржувајќи **асинхронност и контрола на проток (backpressure)**.

---

## 2. Основи на реактивно програмирање
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
- Hot: емитувачот пушта податоци без разлика има ли слушатели; subscriber-ите се приклучуваат во тек. Во нашиот проект, SSE стримот од контролерот е блиску до hot stream за сите клиенти што се приклучуваат во моментот.

### 2.4 Backpressure
Backpressure е механизам што служи за да се спречи клиентот да се „преполни“ со податоци кога серверот испраќа премногу брзо. Во Project Reactor постојат различни оператори и стратегии за справување со ова, како buffer, drop и latest, кои овозможуваат контрола на брзината на проток на податоци.

Во нашиот случај со SSE, стапката на пристигнување на твитовите е релативно умерена, па не користиме дополнителни механизми за backpressure и се потпираме на природното темпо на пристигнување на твитовите и на периодичните пребарувања што ги правиме.


## 3. Архитектура на апликацијата
Апликацијата е изградена како реактивен систем кој овозможува динамично прикажување на твитови во реално време, веднаш штом пристигнат од Twitter API.
Во овој дел е опишан начинот на кој податоците течат низ целиот систем од моментот кога корисникот внесува хаштаг, па сe до прикажувањето на резултатите на фронтендот.

Вака се одвива текот на апликацијата:
1. Корисникот внесува хаштаг во React frontend-от (на пример: #testingHashTag).
2. Frontend-от воспоставува SSE конекција (Server-Sent Events) кон backend endpoint-от:
```/searchTweets/searchtweet?hashtag=testingHashTag```
3. Backend-от проверува дали за дадениот хаштаг веќе постојат зачувани твитови во базата (чува се преку R2DBC).
4. Ако не постојат, backend-от преку WebClient го повикува внатрешниот API /twitter/search, кој потоа контактира со Twitter API, ги презема твитовите и ги зачувува во базата заедно со мета-податоците.
5. Откако ќе ги има податоците, контролерот стримира Flux<Tweet> кон фронтендот преку SSE – твитовите пристигнуваат еден по еден, во реално време.
6. Frontend-от веднаш ги прикажува твитовите штом пристигнат, без потреба од освежување. Дополнително, преку мета-податоците се овозможува приказ на најновиот и најстариот твит во резултатите.

Клучни технологии што ги имаме користено при изработка на овој проект:
* Spring WebFlux – реактивен web framework што овозможува асинхрона комуникација и високи перформанси.
* Project Reactor (Flux/Mono) – библиотека за реактивно програмирање во Java, користена за обработка на стримови на податоци.
* WebClient – неблокирачки HTTP клиент за повици кон надворешни API-и.
* R2DBC – реактивен пристап до базата на податоци без блокирање на нишки.
* SSE (Server-Sent Events) – технологија за еднонасочно испраќање податоци од сервер кон клиент во реално време.
* React Frontend со EventSource – фронтенд што се поврзува со SSE endpoint-от и динамички ги прикажува твитовите веднаш штом пристигнат.


---

## 4. Backend – Конфигурации
Во овој дел се опишани основните конфигурации на backend делот од апликацијата, кои овозможуваат поврзување со надворешни сервиси и работа со базата на податоци преку реактивен програмски модел.
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
Оваа класа служи за конфигурирање на WebClient, кој е реактивна замена за RestTemplate и се користи за правење HTTP барања кон надворешни API сервиси (во нашиот случај), Twitter API.

Со аннотацијата @Configuration ја означуваме класата како извор на bean дефиниции, а со @Bean се регистрира WebClient.Builder во Spring контекстот.
На овој начин, WebClient може да се инјектира (autowire) во сервис класи каде што е потребен, без повторно конфигурирање.

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
Оваа конфигурација овозможува реактивен пристап кон базата на податоци преку R2DBC (Reactive Relational Database Connectivity).
Со @EnableR2dbcRepositories се овозможува користење на реактивни репозиториуми, кои овозможуваат асинхрона комуникација со базата без блокирање на нишки.

R2dbcEntityTemplate овозможува работа со ентитети на пониско ниво и тоа: вметнување, пребарување и ажурирање на записи преку реактивни стримови (Flux и Mono).
Оваа класа е корисна кога сакате поголема контрола над извршувањето на SQL операции, наместо да се потпирате исклучиво на репозиториумите.
### 4.3 Конфигурација на апликација
Конфигурациските параметри се дефинираат во `application.yml` или `application.properties`.
Во нив се сместени информации кои ја поврзуваат апликацијата со Twitter API и со базата на податоци.
```java
twitter:
  api:
    url: "https://api.twitter.com/2/tweets/search/recent"
    bearer-token: "AAAAAAAAAAAAAAAAAAAAAJTI4AEAAAAAaHoCJJsVxSD%2FNJhBMLupTX7z3q0%3DQvv2OyaK6INxVRNsgHAUTFssqORtvHtxyaYIA3orPHMRxLKgzr"
```
```java
spring.application.name=TwitterPostSearch
server.ssl.enabled=false
server.port=8080


spring.r2dbc.url=r2dbc:postgresql://localhost:5432/TwitterDataBase?connectTimeout=30000
spring.r2dbc.username=postgres
spring.r2dbc.password=admin
spring.r2dbc.pool.initial-size=5
spring.r2dbc.pool.max-size=20
logging.level.org.springframework.data.r2dbc=DEBUG
logging.level.io.r2dbc.spi=DEBUG

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
logging.level.org.flywaydb=DEBUG
logging.level.org.springframework.jdbc.core=DEBUG
```
---

## 5. Backend – Домен и Репозиториуми
Во овој дел се опишани доменските класи и репозиториумите кои ја претставуваат основата на backend слојот.
Тие се задолжени за моделирање на податоците и за комуникација со базата преку реактивен пристап.
### 5.1 Доменски класи
Доменските класи го опишуваат моделот на податоци што се користи во апликацијата.
Во нашиот случај, тие ги претставуваат твитовите и мета-податоците поврзани со пребарувањето на твитови.
---
#### Tweet

Класата `Tweet` ја претставува основната структура на еден твит што се зачувува во базата.
Содржи информации за содржината на твитот, авторот и хаштагот според кој е пронајден.

Полиња кој ги содржи Twitter доменот:
* id – уникатен идентификатор на твитот.

* text – содржината на твитот.

* authorId – идентификатор на авторот.

---
#### MetaDataForTweets

Класата `MetaDataForTweets` содржи дополнителни информации поврзани со пребарувањето на твитови преку Twitter API.

Полиња кој ги содржи MetaDataForTweets доменот:
* newestId – ID на најновиот твит во резултатите.

* oldestId – ID на најстариот твит.

* resultCount – вкупен број на твитови што се добиени во резултатот.

* hashtag – хаштагот на кој се однесува мета-податокот.

Овие податоци се корисни за следење на границите на пребарувањето и за можност да се освежуваат или продолжуваат пребарувањата понатаму.

### 5.2 Репозиториуми
Репозиториумите служат како посредник помеѓу апликацијата и базата на податоци.
Во реактивен контекст, тие овозможуваат неблокирачко (асинхроно) извршување на операции преку Flux и Mono типови.
```java
@Repository
public interface TweetRepository extends ReactiveCrudRepository<Tweet, Long> {
    Flux<Tweet> findAllByHashtag(String hashtag);
}
```
Со аннотацијата @Repository оваа класа се регистрира во Spring контекстот како компонентa задолжена за пристап до базата.

ReactiveCrudRepository обезбедува стандардни CRUD методи (save, findById, findAll, delete, итн.) кои работат реактивно.
Дополнително, дефиниран е методот:

* findAllByHashtag(String hashtag) – враќа Flux<Tweet> со сите твитови што припаѓаат на одреден хаштаг.

Овој метод се вклопува природно во SSE стримирањето, бидејќи Flux емитува податоци еден по еден, овозможувајќи им на клиентите да ги добиваат твитовите веднаш штом се достапни – без потреба од чекање сите да се вчитат.

---

Што постигнуваме со оваа структура ?

Со оваа структура, backend-от има чисто разделени одговорности:

* доменските класи ја опишуваат логиката на податоците,

* репозиториумите се грижат за пристапот до базата, а реактивниот пристап обезбедува високи перформанси и ниска латенција при стримирање кон фронтендот.

## 6. Backend – Сервиси

Во овој дел се опишани главните методи и логиката зад сервисниот слој, кој ја поврзува комуникацијата меѓу контролерите, базата и Twitter API-то.
Сервисите се имплементирани реактивно, што овозможува асинхрона и ефикасна обработка на податоци без блокирање на извршувањето.

---

#### Клучни методи:

* getHashtag(String hashtag): Mono<Boolean>

Проверува дали постојат твитови со дадениот хаштаг во базата на податоци.
Овој метод враќа Mono<Boolean> ако постојат записи, враќа true, а ако не постојат, false.
Се користи како прв чекор пред да се одлучи дали твитовите треба да се преземат од Twitter API или директно од базата.

---

* getTweets(String hashtag)

Ги зема сите твитови од базата што се поврзани со конкретниот хаштаг.
Враќа Flux<Tweet>, што значи дека податоците се испраќаат еден по еден што е совршено за стримирање преку SSE.

---

* fetchandSaveTweets(String hashtag)

Овој метод има двојна улога:
1. Го иницира повикот до локалниот endpoint /twitter/search?hashtag=..., кој комуницира со Twitter API и ги зачувува новите твитови во базата.
2. По завршувањето на повикот, повторно ги чита твитовите од базата со getTweets(hashtag) и ги враќа како Flux<Tweet>.

На тој начин се обезбедува дека секогаш се враќаат најновите твитови, без разлика дали веќе биле зачувани или штотуку се преземени.


```java
 @Override
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
Овој шаблон покажува типична реактивна обработка:
* WebClient прави неблокирачки HTTP повик
* резултатот се обработува со flatMap, а во случај на грешка се фаќа и се прикажува преку doOnError

---
* streamTweetsByHashtag(String hashtag)
 
Пример метод кој демонстрира периодично емитување на твитови користејќи Flux.interval().
Ова може да се користи кога сакаме на фронтендот да се испраќаат твитови на одреден временски интервал. На пример за симулација на нови твитови што пристигнуваат во реално време.

---

* getMetaDataForTweet(String hashtag)

Го враќа мета-податокот поврзан со твитовите за дадениот хаштаг.
Мета-податоците вклучуваат информации како најновиот и најстариот твит ID, како и вкупниот број на резултати.
Овој метод е корисен за дополнителни прикази и контроли на фронтенд страната, на пример – прикажување на „Load newer“ или „Load older“ функционалности.
---

## 7. Backend – API Слој

Овој документ детално го објаснува API-слојот на backend-от, кој овозможува интеграција со **Twitter API** и стримирање на твитови кон фронтендот преку **Server-Sent Events (SSE)**.

---

### 7.1 Внатрешен API кон Twitter – `TwitterApiController`

Контролерот `TwitterApiController` е одговорен за преземање твитови од Twitter API, нивна обработка и зачувување во базата.

#### Главни чекори во `/twitter/search`

1. **Градење на URL** за Twitter API, користејќи го *bearer token*-от од конфигурацијата.
2. **HTTP повик преку WebClient** со `.retrieve()` и `.onStatus()` за ракување со можни грешки.
3. **Земање на одговорот како JSON** преку `.bodyToMono(String.class)`.
4. **Парсирање на JSON-от во позадина** со `.publishOn(Schedulers.boundedElastic())` за да се ослободи главниот thread.
5. **Парсирање на JSON-от** со `ObjectMapper` и креирање на валидни `Tweet` објекти.
6. **Зачувување на твитовите** во базата преку `tweetRepository.saveAll(...).collectList()`.
7. **Зачувување на мета-податоците** (`newest_id`, `oldest_id`, `result_count`) во `MetaDataForTweetRepository`.
8. **Враќање на `Mono<String>`** како порака што го сумира резултатот на повикот.


Зошто `Mono`, а не `Flux`?

Затоа што методот прави еден HTTP повик и враќа само една порака и тоа резултат од обработката.  
Самото стримирање на твитовите се изведува во друг контролер.


---

### Пример код

```java
return webClientBuilder.build()
    .get()
    .uri(url)
    .header("Authorization", "Bearer " + bearerToken)
    .retrieve()
    .onStatus(HttpStatusCode::isError, response -> ...)
        .bodyToMono(String.class)
    .publishOn(Schedulers.boundedElastic())
        .flatMap(response -> {
        // Парсирање + зачувување на твитови и мета податоци
        return processAndSaveTweets(response);
    })
            .onErrorResume(e -> Mono.just("Error fetching tweets: " + e.getMessage()));
```
### 7.2 SSE(Server-Sent Events) кон фронтенд – TweetsToFrontendController
Овој дел прикажува како изгледа еден endpoint кој се користи за стримирање твитови до фронтендот преку Server-Sent Events (SSE).
Примерот подолу го покажува методот што се повикува за добивање твитови според одреден hashtag:
```java
@GetMapping(value = "/searchtweet", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Tweet> getAllTweetsFromHashtag(@RequestParam String hashtag) {
    return tweetService.getHashtag(hashtag)
            .flatMapMany(exists->{
                if (exists){
                    return tweetService.getTweets(hashtag);
                }
                return tweetService.fetchandSaveTweets(hashtag);
            });
}
```
Кратко објаснување за тоа што прави овој метод:
* produces = MediaType.TEXT_EVENT_STREAM_VALUE му кажува на Spring дека треба да испраќа одговор во формат text/event-stream.

* Клиентот (на пример React апликацијата) ги прима настаните еден по еден – секој настан е JSON-објект од тип Tweet, кој фронтендот го парсира и го додава во листата со твитови.

* Доколку постојат твитови во базата, тие веднаш се стримираат до клиентот. Ако не постојат, најпрво се иницира повикување на Twitter API (преку локалниот контролер) за да се вчитаат и зачуваат твитовите, а потоа се стримираат од базата.

Покрај овој endpoint, постојат и следниве:

* GET /searchTweets/metaDataForTweet – враќа Flux<MetaDataForTweets> за даден хаштаг.

* GET /searchTweets/stream – пример endpoint што демонстрира како може периодично да се емитуваат твитови со Flux.interval.

---

## 8. Frontend – Извршување во реално време со EventSource
Овој дел опишува како фронтендот во React ја прима и прикажува потеклото на твитови во реално време, користејќи Server-Sent Events (SSE).

### Основен useEffect за стримирање твитови:
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
#### Како функционира овој метод:
* Нова SSE конекција се отвара секој пат кога hashtag се менува.
* Пристигнатите настани се JSON објекти од тип Tweet.
* Секој нов твит се додава на врвот на листата во state-от (setTweets), за да се прикажуваат најновите први.
* Грешките се логираат во конзолата, а конекцијата се затвора автоматски при прекин или промена на hashtag.

---

### Како ги прикажуваме мета-податоците:
Заедно со стримирањето на твитови, фронтендот може да прикажува и најнов/најстар твит користејќи мета-податоци:

* API повик со axios
    ```  
    const takeTweetsFromBackend = async () => {
        const response = axios.get(`http://localhost:8080/searchTweets/searchtweet?hashtag=${hashtag}`)
        setTweets((await response).data)
    
        const response1 = axios.get(`http://localhost:8080/searchTweets/metaDataForTweet?hashtag=${hashtag}`)
        SetmetaDataForTweet((await response1).data)
    }
    ```
* Мета-податоците вклучуваат newestId и oldestId, што овозможува прикажување на конкретни твитови.

### Компонента ShowNewestPost.js

Пример како се прикажува најновиот твит:
``` angular2html
const  ShowNewestPost=(props)=>{

    const oldestTweet = props.tweets.find(tweet => tweet.authorId === props.oldest);

    return (
        <div>
            <Row className={"g-4"}>
                <Col sm={12} md={6} lg={4}>
                    <h3 style={{ color: "blue" }}>Oldest Post:</h3>
                    <CardGroup>
                        <Card style={{ width: '15rem', height: "350px" }}>
                            <Card.Img variant="top" src={"image.png"} style={{ width: "50px", height: "50px", display: "flex" }} />
                            <Card.Body>
                                <Card.Title>Tweet Author ID: {oldestTweet.authorId}</Card.Title>
                                <Card.Text>{oldestTweet.text}</Card.Text>
                                <p>Hashtag: #{oldestTweet.hashtag}</p>
                            </Card.Body>
                        </Card>
                    </CardGroup>
                </Col>
            </Row>
        </div>
    );
}
export default ShowNewestPost;
```
**Објаснување**:

* Компонентата ја пребарува листата tweets и го наоѓа твитот со id еднаков на newestId.
* Доколку твитот постои, се прикажува неговиот текст и автор.
* Истиот принцип може да се примени и за прикажување на најстариот твит

---

Заклучок од што се состои фронт-ендот
* EventSource, ова овозможува еднонасочно, реално-времено примање на твитови без континуирано освежување на страницата.
* Секој нов твит се додава на врвот на листата, што го прави интерфејсот динамичен и интерактивен.
* Мета-податоците се користат за дополнителни контроли, како најнов/најстар твит или филтрирање на резултатите.

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

### База на податоци
Апликацијата користи R2DBC за реактивен пристап до PostgreSQL база, што овозможува не-блокирачки повици кон базата и целосна интеграција со реактивниот WebFlux стек.

Што е клучно да се знае за оваа база на податоци ?
* Reactive repositories: Сите репозиториуми се реактивни, наследуваат од ReactiveCrudRepository.
* Миграции: SQL-скриптите за креирање на табели се наоѓаат во src/main/resources/db/migration/. Таму се дефинирани табелите за:
  * Твитови (Tweet)
  * Мета-податоци (MetaDataForTweets)

**Зошто би ги зачувувале податоците ?**

Прво ги зачувуваме твитовите во базата за да имаме **cache**. Така што ако истиот hashtag се побара повторно, не е потребен нов повик кон Twitter API, што го прави системот поефикасен и помалку оптоварен.

### Миграции

Во проектот, за управување со структурите на базата на податоци се користи Flyway. Flyway е алатка која овозможува верзионирано и автоматизирано управување со SQL скрипти за миграции. Со него, секоја промена во структурата на базата (додавање на нови табели, менување на колони, индекси итн.) се применува кон базата на предвидлив и контролиран начин.

Овие миграции може да ги најдете во делот на `resources -> db.migrations`

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
    Како серверот ги праќа новите твитови?
    Преку Server-Sent Events (SSE) кои автоматски испраќаат нови податоци штом пристигнат.
- Која е улогата на WebClient?
  - WebClient асинхроно го повикува Twitter API и ги враќа резултатите без да го блокира серверот.

- Може ли WebFlux да работи со класична JPA база?
  - Не директно. Потребен е реактивен драјвер како R2DBC, бидејќи JPA е блокирачка.

- Дали може ова да се претвори во мобилна апликација?
  - Да, преку мобилен клиент кој поддржува SSE или WebSocket конекција.

- Што се случува ако Twitter API не врати ништо?
  - Flux едноставно ќе емитува празен стрим, без грешка или блокирање.

- Како фронт-ендот знае дека пристигнал нов твит?
  - Преку отворен SSE канал кој веднаш испраќа настан до клиентот.
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
