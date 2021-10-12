import { Box, List, Accordion, AccordionPanel, Text, Anchor } from "grommet";
import { Upload, View } from 'grommet-icons';
import { Operations } from '../Operations';
import { ezApi, jsonCall } from '../../ez-api/tools';
import { EzProcess, EzEdition, EzReport } from '../../ez-api/gen-api/EZLoadApi';

export interface Reports {
    reports: EzReport[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
}      

export function Reports(props: Reports){
    return (
        <Box margin="small" >
            <Accordion animate={true} multiple>            
             { props.reports.map(report => {                 
                     return (
                        <AccordionPanel label={(<Box direction="row"
                                            border={report.errors!.length > 0 ?
                                                     {color: "status-warning", side: "start", size: "large"}
                                                    :  {color: "background", side: "start", size: "large"}}>
                                                    <Text margin="xxsmall">{report.sourceFile}</Text>
                                                    <Anchor style={{padding: 2, boxShadow: "none"}} target="source" 
                                                            href={ezApi.baseUrl+"/explorer/file?source="+encodeURIComponent(report.sourceFile ? report.sourceFile : "")} 
                                                            icon={<View size="small"/>} onClick={(e) => {                                                                     
                                                                e.stopPropagation();
                                                                } }/>
                                                </Box>)}>
                            {report.errors!.length > 0 && (                                    
                                <List data={report.errors} margin="none" pad="none" 
                                    border={report.errors!.length > 0 ? {color: "status-warning", side: "left", size: "large"} : {size: "none"}}>
                                    {(error: string) => (<Text margin="xsmall">{error}</Text>)}      
                                </List>
                            )}
                            {report?.ezEditions && (<Operations
                                processRunning={props.processRunning}
                                followProcess={props.followProcess}
                                operations={report.ezEditions}/>)}
                        </AccordionPanel>
                     );
                 })}          
            </Accordion>
        </Box>
    );
}
