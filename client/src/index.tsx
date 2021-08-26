import React from "react";
import ReactDOM from 'react-dom';
import { Provider } from "react-redux";

import "./index.css";
import * as serviceWorker from './serviceWorker';

import { Grommet } from "grommet";
//import { dark as theme } from "grommet/themes/dark";
import App from "./Components/App";
import { store } from "./store/store";

// Theme are available here: https://github.com/search?q=org%3Agrommet+grommet-theme&unscoped_q=grommet-theme
// import { theme } from "./themes/black";
/*
const colors = [
  "accent-1",
  "accent-2",
  "accent-3",
  "brand",
  "dark-1",
  "dark-2",
  "dark-3",
  "dark-4",
  "dark-5",
  "dark-6",
  "focus",
  "light-1",
  "light-2",
  "light-3",
  "light-4",
  "light-5",
  "light-6",
  "neutral-1",
  "neutral-2",
  "neutral-3",
  "status-critical",
  "status-disabled",
  "status-ok",
  "status-unknown",
  "status-warning",
];

const customTheme = {
  ...theme,
  global: {
    colors: {
      custom: "#cc6633",
    },
  },
};

const updatedTheme = {
  ...theme,
  formField:
  {
    ...theme.formField,
  },
};
*/
// </Grommet> theme={updatedTheme}>
const GrommetApp = () => (
  <Grommet>
    <App mainMenuVisible={true}/>
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