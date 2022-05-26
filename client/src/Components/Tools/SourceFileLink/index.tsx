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