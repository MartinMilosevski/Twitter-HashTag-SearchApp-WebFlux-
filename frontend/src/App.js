import logo from './logo.svg';
import './App.css';
import Index from "./components/Index";
import {QueryClient, QueryClientProvider} from "react-query";

function App() {
    const client=new QueryClient();
  return (
    <div className="App">
      <h1>Пребарување на твитови по hashtag</h1>
        <QueryClientProvider client={client}>
        <Index />
        </QueryClientProvider>
    </div>
  );
}

export default App;
