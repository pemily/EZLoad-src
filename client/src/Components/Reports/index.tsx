import { Box, Button, Accordion, AccordionPanel, Text, Anchor } from "grommet";
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
             { props.reports.map(report => {                 
                     return (
                        <Accordion animate={true} multiple background={report.error? "status-warning" : ""}>
                            <AccordionPanel label={(<Box direction="row">
                                                        <Text margin="xxsmall">{report.sourceFile}</Text>
                                                        <Anchor style={{padding: 2, boxShadow: "none"}} target="source" 
                                                             href={ezApi.baseUrl+"/explorer/file?source="+encodeURIComponent(report.sourceFile ? report.sourceFile : "")} 
                                                             icon={<View size="small"/>} onClick={(e) => {                                                                     
                                                                    e.stopPropagation();
                                                                 } }/>
                                                    </Box>)}>
                                {report?.ezEditions && (<Operations
                                    processRunning={props.processRunning}
                                    followProcess={props.followProcess}
                                    operations={report.ezEditions}/>)}
                            </AccordionPanel>
                        </Accordion>
                     );
                 })}          
        </Box>
    );
}
