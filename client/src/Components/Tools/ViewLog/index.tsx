import { Fragment } from 'react';
import { Box, Text } from "grommet";
import { EzProcess } from '../../../ez-api/gen-api/EZLoadApi';
import { stream, ezApi } from '../../../ez-api/tools';
import './ezLoad/ezLoadHeaderReporting.css';

var { pushSection, popSection, add } = require( './ezLoad/ezLoadHeaderReporting.js');


export interface ViewLogProps {
  process: EzProcess | undefined;
}

export function ViewLog(props: ViewLogProps) {    
    if (props.process !== undefined){        
        stream(ezApi.home.viewLogProcess(), (update) => { 
            console.log("RECEIVED: ", update);
            const newCommand = update.replaceAll('<script>', '').replaceAll('</script>', ';');
            console.log(newCommand);
            eval(newCommand);
        }, () => {});
        return (<Box id="ProcessOutput">
            <Fragment>
                <ul id='1' className='br-tree'></ul>
            </Fragment>
        </Box>);
    }
    else{
        return (<Box><Text>Aucune tâche en cours</Text></Box>);
    }
        
}