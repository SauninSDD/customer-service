import {useEffect, useState} from "react";
import Preloader from "./components/generals/Preloader"
import './App.css';
import NavigationMenu from "./components/generals/NavMenu";
import {FloatButton} from "antd";


function App() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setTimeout(() => {
      setLoading(false);
    }, 2000);
  }, []);

  return (
      <div className="App">
        {loading ? (
            <Preloader />
        ) : (
            <div>
              <NavigationMenu/>
              <FloatButton.BackTop />
            </div>
        )}
      </div>
  );
}

export default App;
