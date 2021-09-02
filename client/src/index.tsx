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


const colors = {
  'background-contrast': '#000000FF',
  text: '#444444',
  'text-strong': '#000000',
  'text-weak': '#BBBBBB',
  border: '#999999',
  'border-strong': '#666666',
  'border-weak': '#BBBBBB',
  'active-background': 'background-contrast',
  'active-text': 'text',
};

const customTheme = deepMerge(grommet, {
    heading: {
        extend: `margin: 5px`,
    },
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
    tab: {
        color: 'text-strong',
        active: {
            background: 'background-contrast',
        },
        hover: {
          background: 'background-contrast',
        },
        pad: {
            horizontal: 'small'
        },
        border: {
          side: 'bottom',
          color: 'background-back',
          active: {
            color: 'control',
          },
          hover: {
            color: 'control',
          },
        },
        extend: `box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.5);`
    },
    tabs: {
        background: 'background',
        gap: 'medium',
        header: {
          background: 'dark-1',
          extend: `padding: 10px; box-shadow: 0px 3px 20px rgba(0, 0, 0, 0.50);`
        },

    },
    global: { font: {  family: '-apple-system,BlinkMacSystemFont,"Segoe UI Variable","Segoe UI",system-ui,ui-sans-serif,Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji"', size: 'small' } },
});

const GrommetApp = () => (
  <Grommet full theme={customTheme}>
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