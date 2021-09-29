import { Fragment, useEffect, useState } from 'react';
import { Box, Text } from "grommet";
import { stream, ezApi } from '../../../ez-api/tools';
import {  EzProcess } from '../../../ez-api/gen-api/EZLoadApi';
import './ezLoad/ezLoadHeaderReporting.css';

var { pushSection, popSection, add } = require( './ezLoad/ezLoadHeaderReporting.js');

export interface ViewLogProps {
    ezProcess: EzProcess|undefined;
    processFinished: () => void;
}

export function ViewLog(props: ViewLogProps) {    

    const [isStreaming, setStreaming] = useState(false);
    const [lastProcess, setLastProcess] = useState<undefined|EzProcess>(undefined);
    const [loadedLog, setLoadedLog] = useState<string>("");

    useEffect(() => {        
        showLog();
    })

    function showLog(){
        if (!isStreaming){
            let launchStreaming: boolean = true;
            console.log("COMPARE", lastProcess, props.ezProcess);
            if (lastProcess && lastProcess!.logFile === props.ezProcess?.logFile) {
                launchStreaming = false;            
            }
            if (props.ezProcess === undefined){
                launchStreaming = false;
            }
            if (launchStreaming){
                setStreaming(true);
                setLastProcess(props.ezProcess);
                setLoadedLog("");
                stream(ezApi.home.viewLogProcess(), (update) => { 
                    const newCommand = update.replaceAll('<script>', '').replaceAll('</script>', ';');
                    setLoadedLog(loadedLog+newCommand);                    
                }, () => {
                    setStreaming(false);
                    props.processFinished();
                });
            }
        }
        console.log("EVAL", loadedLog);
        eval(loadedLog);
    }
            
    return (<Box id="ProcessOutput" pad="medium" >
            <Fragment>
                <ul id='1' className='br-tree'></ul>
            </Fragment>            
        </Box>);
}