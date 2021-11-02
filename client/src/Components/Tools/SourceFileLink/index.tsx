
import { DocumentText } from 'grommet-icons';
import { Text, Anchor } from "grommet";
import { ezApi } from '../../../ez-api/tools';


export interface SourceFileLinkProps {
  sourceFile: string;
}


export function SourceFileLink(props: SourceFileLinkProps) {
    return (     
      <>
        <Text margin="xxsmall">{props.sourceFile}</Text>
        <Anchor style={{padding: 2, boxShadow: "none"}} target="source" 
                href={ezApi.baseUrl+"/explorer/file?source="+encodeURIComponent(props.sourceFile ? props.sourceFile : "")} 
                icon={<DocumentText size="small"/>} onClick={(e) => {                                                                     
                    e.stopPropagation();
                    } }/>
      </>
          );
}