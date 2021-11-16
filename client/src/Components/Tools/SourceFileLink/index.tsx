
import { DocumentText } from 'grommet-icons';
import { Text, Anchor, Box } from "grommet";
import { ezApi } from '../../../ez-api/tools';


export interface SourceFileLinkProps {
  sourceFile: string|undefined;
}


export function SourceFileLink(props: SourceFileLinkProps) {
    return (     
      <> {props.sourceFile && (
          <Box margin="none" direction="row">
              <Text margin="xxsmall">{props.sourceFile}</Text>
              <Anchor style={{padding: 2, boxShadow: "none"}} target={props.sourceFile} 
                      href={ezApi.baseUrl+"/explorer/file?source="+encodeURIComponent(props.sourceFile ? props.sourceFile : "")} 
                      icon={<DocumentText size="small"/>} onClick={(e) => 
                          e.stopPropagation()}
                           />
          </Box>
        )}
      </>
    );
}