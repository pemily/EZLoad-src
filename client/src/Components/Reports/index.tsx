import { Box, List, Accordion, AccordionPanel, Text } from "grommet";
import { BorderType } from "grommet/utils";
import { SourceFileLink } from '../Tools/SourceFileLink';
import { Operations } from '../Operations';
import { EzProcess, EzEdition, EzReport } from '../../ez-api/gen-api/EZLoadApi';

export interface ReportsProps {
    reports: EzReport[];
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    showRules: boolean;
    createRule: (from: EzEdition) => void;
    viewRule: (from: EzEdition) => void;
    isOperationIgnored: (fom: EzEdition) => boolean;
    ignoreOperation: (from: EzEdition, ignore: boolean) => void;
}      

function getAccordionBorder(status: "OK" | "WARNING" | "ERROR" | undefined) : BorderType {
    // if there is an error or not
    if (status === undefined || status === "OK") return  {color: "background", side: "start", size: "large"};
    if (status === "ERROR") return {color: "status-error", side: "start", size: "large"};            
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

function getListBorder(status: "OK" | "WARNING" | "ERROR" | undefined) : ListBorderType{
    // if there is an error or not    
    if (status === undefined || status === "OK") return  { size: "none" };
    if (status === "ERROR") return {color: "status-error", side: "left", size: "large"};            
    return {color: "status-warning", side: "left", size: "large"};    
}

function getReportError(index: number, error: string){
    return (<Text key={index} margin={{ horizontal: 'medium'}}>{error}</Text>);      
}

export function Reports(props: ReportsProps){
    return (
        <Box margin="small" >
            <Accordion animate={true} multiple>            
             { props.reports.map((report, index) => {       
                     return (
                        <AccordionPanel key={index} label={(<Box direction="row" border={getAccordionBorder(report.status)} >
                                                    { report.reportType === "IS_SHARE_UPDATE" && (<SourceFileLink sourceFile={report.sourceFile!}/>) }
                                                    { report.reportType === "IS_DIVIDEND_UPDATE" && (<Text margin="xxsmall">Mise à jour des dividendes</Text>) }                                                    
                                                </Box>)}>
                            {report.errors!.length > 0 && (                                    
                                <List data={report.errors} margin="none" pad="none" 
                                    border={getListBorder(report.status)}>
                                    {(error: string, index: number) => getReportError(index, error)}
                                </List>
                            )}
                            {report?.ezEditions && (<Operations id={index}
                                processRunning={props.processRunning}
                                followProcess={props.followProcess}
                                showRules={props.showRules}
                                createRule={props.createRule}
                                viewRule={props.viewRule}
                                operations={report.ezEditions}
                                isIgnored={props.isOperationIgnored}
                                setIgnored={props.ignoreOperation}
                                isDividendUpdate={report.reportType === "IS_SHARE_UPDATE"}/>)}
                        </AccordionPanel>
                     );
                 })}          
            </Accordion>
        </Box>
    );
}
