import React from "react";
import ReactDOM from 'react-dom';
import { Provider } from "react-redux";
import * as serviceWorker from './serviceWorker';


import { Grommet } from "grommet";
import { grommet } from 'grommet/themes';
import { deepMerge } from 'grommet/utils';
import { hpe } from 'grommet-theme-hpe';
import { App } from "./Components/App";
import { store } from "./store/store";

const customTheme = deepMerge(grommet, {
  formField: {
    border: {
      side: 'all',
    },
    error: {
      size: 'xsmall',
    },
    help: {
      size: 'xsmall',
    },
    info: {
      size: 'xsmall',
    },
    label: {
      size: 'small',
    },
    round: '4px',
  },
  global: { font: { size: 'small' } },
});

const GrommetApp = () => (
  <Grommet full={true} theme={customTheme}>
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