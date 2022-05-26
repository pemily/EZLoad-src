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
/* eslint-disable no-eval */
/* eslint-disable @typescript-eslint/no-unused-vars */
import { Fragment, useEffect } from 'react';
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

            function add(s: string, isError: boolean){
                dynLogger.add(s, isError);
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
                    try{
                        eval(newCommand);           
                    }
                    catch(e2) {console.error("Error while evaluating: ", newCommand); window.location.reload(); return true;};
                    return false; // do not stop the streaming
                }
                return true; // stop the streaming
            }, props.processFinished)
            .catch((e) => {console.error(e); props.processFinished()}); 

            return function cleanup(){
                dynLogger.stop();
            };
        }
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
    useEffect(showLog, []) // Le [] fait que le useEffect ne sera appelé qu'une fois apres le 1er rendu
/*  useEffect(() => {        
    showLog();
    })
*/
    
    return (<Box id="ProcessOutput" pad="medium" >   
            <Text margin="small" size="xlarge" alignSelf="center">{valued(props.ezProcess?.title)}</Text>
            <Text>{valued(props.ezProcess?.logFile)}</Text>
            { (props.ezProcess === "") && (
                <Text>Pas de tâche en cours</Text>
              )}                        
              <Fragment>
                <ul id='1' className='br-tree'></ul>
              </Fragment>
        </Box>);
}