import { Box, Text } from "grommet";
import { EzProcess } from '../../../ez-api/gen-api/EZLoadApi';
import { stream, ezApi } from '../../../ez-api/tools';

export interface ViewLogProps {
  process: EzProcess | undefined;
}

export function ViewLog(props: ViewLogProps) {    
    if (props.process !== undefined){
        console.log("READ STREAM");
        stream(ezApi.home.viewLogProcess(), (update) => console.log("upd", update), () => {});
        return (<Box id="ProcessOutput"></Box>);
    }
    else{
        return (<Box><Text>Aucune tâche en cours</Text></Box>);
    }
        
}