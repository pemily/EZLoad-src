import React from "react";
import ReactDOM from 'react-dom';
import { Provider } from "react-redux";
import * as serviceWorker from './serviceWorker';


import { Grommet } from "grommet";
import { hpe } from 'grommet-theme-hpe';
import { App } from "./Components/App";
import { store } from "./store/store";


const GrommetApp = () => (
  <Grommet full={true} theme={hpe} themeMode="dark"  >
    <App/>
  </Grommet>
);

ReactDOM.render(
  <React.StrictMode>
    <Provider store={store}>
      <GrommetApp />
    </Provider>
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();