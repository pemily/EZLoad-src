import React from "react";
import ReactDOM from 'react-dom'; 

import { Grommet } from "grommet";
import { grommet } from 'grommet/themes';
import { deepMerge } from 'grommet/utils';
import { App } from "./Components/App";


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
    <GrommetApp />
  </React.StrictMode>,
  document.getElementById('root')
);
