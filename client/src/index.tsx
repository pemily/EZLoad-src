/*
 * ezClient - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
