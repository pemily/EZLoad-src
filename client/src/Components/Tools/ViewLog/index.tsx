import { Fragment, useEffect, useState } from 'react';
import { Box, Text } from "grommet";
import { stream, ezApi, valued } from '../../../ez-api/tools';
import { EzProcess } from '../../../ez-api/gen-api/EZLoadApi';
import './ezLoad/ezLoadHeaderReporting.css';

var { DynamicLogger } = require( './ezLoad/ezLoadHeaderReporting.js');

export interface ViewLogProps {
    ezProcess: EzProcess|undefined;
    processFinished: () => void;
}

export function ViewLog(props: ViewLogProps) {    

    function showLog(){                   
        if (props.ezProcess !== undefined){
            const dynLogger = new DynamicLogger();

            function add(s: string){
                dynLogger.add(s);
            }
            function popSection(){
                dynLogger.popSection();
            }
            function pushSection(s: string){
                dynLogger.pushSection(s);
            }

            stream(ezApi.home.viewLogProcess(), (update) => { 
                if (!dynLogger.isStopped()){
                    const newCommand = update.replaceAll('<script>', '').replaceAll('</script>', ';');
                    eval(newCommand);           
                    return false; // do not stop the streaming
                }
                return true; // stop the streaming
            }, () => {
                props.processFinished();
            }); 

            return function cleanup(){
                dynLogger.stop();
            };
        }
    }

    useEffect(showLog, []) // Le [] fait que le useEffect ne sera appelé qu'une fois apres le 1er rendu

    
    return (<Box id="ProcessOutput" pad="medium" >
            <Text size="xlarge" alignSelf="center">{valued(props.ezProcess?.title)}</Text>
            <Text>{valued(props.ezProcess?.logFile)}</Text>
            { (props.ezProcess === "") && (
                <Text>Pas de tâche en cours</Text>
              )}                        
              <Fragment>
                <ul id='1' className='br-tree'></ul>
              </Fragment>
        </Box>);
}