import { Box, List, Accordion, AccordionPanel, Text, Anchor } from "grommet";
import { BorderType } from "grommet/utils";
import { Upload, View } from 'grommet-icons';
import { SourceFileLink } from '../Tools/SourceFileLink';
import { Operations } from '../Operations';
import { ezApi, jsonCall } from '../../ez-api/tools';
import { EzProcess, EzEdition, EzReport } from '../../ez-api/gen-api/EZLoadApi';

export interface Reports {
    reports: EzReport[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    showRules: boolean;
    createRule: (from: EzEdition) => void;
    viewRule: (from: EzEdition) => void;
}      

function getAccordionBorder(errors: string[]) : BorderType {
    // if there is an error or not
    if (errors.length === 0) return  {color: "background", side: "start", size: "large"};
    if (errors.findIndex(e => e !== 'NO_RULE_FOUND') >= 0) return {color: "status-error", side: "start", size: "large"};            
    return {color: "status-warning", side: "start", size: "large"};    
}


type SizeType =
  | 'xxsmall'
  | 'xsmall'
  | 'small'
  | 'medium'
  | 'large'
  | 'xlarge'
  | string;
type SideType =
  | 'top'
  | 'left'
  | 'bottom'
  | 'right'
  | 'horizontal'
  | 'vertical'
  | 'all';
type ListBorderType =
  | boolean
  | SideType
  | {
      color?: string | { dark?: string; light?: string };
      side?: SideType;
      size?: SizeType;
    };

function getListBorder(errors: string[]) : ListBorderType{
    // if there is an error or not
    if (errors.length === 0) return  { size: "none" };
    if (errors.findIndex(e => e !== 'NO_RULE_FOUND') >= 0) return {color: "status-error", side: "left", size: "large"};            
    return {color: "status-warning", side: "left", size: "large"};    
}

function getReportError(index: number, error: string){
    if (error === 'NO_RULE_FOUND') return (<></>);
    return (<Text key={index} margin={{ horizontal: 'medium'}}>{error}</Text>);      
}

export function Reports(props: Reports){
    return (
        <Box margin="small" >
            <Accordion animate={true} multiple>            
             { props.reports.map((report, index) => {                 
                     return (
                        <AccordionPanel key={index} label={(<Box direction="row" border={getAccordionBorder(report.errors!)} >
                                                    <SourceFileLink sourceFile={report.sourceFile!}/>
                                                </Box>)}>
                            {report.errors!.length > 0 && (                                    
                                <List data={report.errors} margin="none" pad="none" 
                                    border={getListBorder(report.errors!)}>
                                    {(error: string, index: number) => getReportError(index, error)}
                                </List>
                            )}
                            {report?.ezEditions && (<Operations id={index}
                                processRunning={props.processRunning}
                                followProcess={props.followProcess}
                                showRules={props.showRules}
                                createRule={props.createRule}
                                viewRule={props.viewRule}
                                operations={report.ezEditions}/>)}
                        </AccordionPanel>
                     );
                 })}          
            </Accordion>
        </Box>
    );
}
