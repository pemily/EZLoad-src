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
import { ezApi, jsonCall, textCall, ruleTitle, SelectedRule } from '../../../ez-api/tools';
import { EzData, RuleDefinitionSummary, RuleDefinition, FileStatus } from '../../../ez-api/gen-api/EZLoadApi';
import { Trash, Revert, Configure, Clipboard, DocumentStore, Command, Services, ClearOption, History } from 'grommet-icons';

import { useState } from "react";

import { Box, Anchor, Button, Layer, Text, TextArea, Heading } from "grommet";
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css

export interface GitStatusProps {
    readOnly : boolean;    
}      

export function GitStatus(props: GitStatusProps){
    const [gitVisible, setGitVisible] = useState(false);
    const [changes, setChanges] = useState<FileStatus[]>([]);      
    const [selectedFile, selectFile] = useState<FileStatus|undefined>(undefined);      
    const [selectedChange, selectChange] = useState<string|undefined>(undefined);      

    const getState = (st: undefined | "NO_CHANGE" | "NEW" | "UPDATED" | "DELETED" | "CONFLICT") => {
        if (st === "NEW") return "(Nouveau)";
        if (st === "UPDATED") return "";
        if (st === "DELETED") return "(Supprimé)";
        if (st === "CONFLICT") return "(Conflit)";
        return "";
    }

    const onOpen = () => {        
        setGitVisible(true);
        // load the git diff
        jsonCall(ezApi.git.getChanges())
        .then(r => setChanges(r))
        .catch(e => console.error(e));        
    }
    const onClose = () => { setGitVisible(false); setChanges([]); selectChange(undefined); selectFile(undefined)}

    return (
            <Box alignSelf="center">
                <Anchor onClick={onOpen} icon={<History size="medium"/>}/>
                { gitVisible &&
                    (
                    <Layer full position="center" margin="large" animation="slide" onEsc={onClose} onClickOutside={onClose} >                        
                        { changes.length == 0 && ( 
                                <Box direction="column" fill > 
                                    <Heading alignSelf="center" level="5">Aucun changement dans les rêgles</Heading> 
                                    <Anchor alignSelf='center' margin="small" label='Fermer' onClick={() => onClose()}/>
                                </Box>
                            )
                        }

                        { changes.length > 0 && ( <Heading alignSelf="center" level="5">Vos changements</Heading> ) }

                        { changes.length > 0 && 
                            ( <Box direction="row" fill height="xxlarge">
                                <Box direction="column" margin="small">
                                    {
                                        changes.length > 0 && (
                                            <Button key="push" size="small" margin="small" alignSelf="end"
                                                label="Envoyer" 
                                                disabled={props.readOnly}
                                                onClick={() =>                                                                     
                                                                jsonCall(ezApi.git.push({message: "Message"}))
                                                                .then(r => onOpen)
                                                                .then(r => {
                                                                    setChanges([]);
                                                                    alert("Merci d'avoir partagé vos modifications. Elles seront analysé et intégré dans une prochaine version");
                                                                })
                                                                .catch(e => console.error(e))
                                                            }
                                            />
                                        )
                                    }
                                    {
                                        changes.map(c =>
                                          (<Box direction="row" border="top" key={"gitDiff_"+c.filepath}>
                                                        <Anchor label={c.filepath} 
                                                                onClick={() =>                                                                     
                                                                    textCall(ezApi.git.getChange({file: c.filepath!}))
                                                                                .then(r => { selectChange(r); selectFile(c)})
                                                                                .catch(e => console.error(e))
                                                                }/>
                                                        <Text size="small" margin="xsmall">{getState(c.fileState)}</Text>
                                                    </Box>)
                                        )
                                    }
                                </Box>
                                <Box fill margin="small">
                                    <TextArea resize={false} fill readOnly={true} value={selectedChange?.replaceAll('\t', '    ')}/>       

                                    <Box alignSelf='end' direction="row">
                                    { selectedFile && (
                                        <Button key={"delBD"} size="small" alignSelf="end"
                                                title="Revenir à la version d'origine" 
                                                disabled={props.readOnly}
                                                icon={selectedFile.fileState === "NEW" ? <Trash color='status-critical' size='medium'/> : <Revert color='status-critical' size='medium'/>}
                                                onClick={() =>{
                                                    if (selectedFile.fileState === 'NEW'){
                                                        confirmAlert({
                                                            title: 'Etes vous sûr de vouloir supprimer cette règle?',
                                                            message: 'Elles ne pourra plus être utilisée pour créer des opérations.',                            
                                                            buttons: [
                                                                {
                                                                    label: 'Supprimer',
                                                                    onClick: () =>  jsonCall(ezApi.git.revert({file: selectedFile.filepath!}))
                                                                    .then(onOpen)
                                                                    .catch(e => console.error(e))
                                                                },
                                                                {
                                                                label: 'Annuler',
                                                                    onClick: () => {}
                                                                }
                                                            ]
                                                            });    
                                                    }
                                                    else{
                                                    confirmAlert({
                                                        title: 'Etes vous sûr de vouloir revenir à la version d\'origine?',
                                                        message: 'Vous allez restaurer la version originale de cette règle, et vous allez perdre vos modifications.',
                                                        buttons: [
                                                            {
                                                                label: 'Restaurer',
                                                                onClick: () =>  jsonCall(ezApi.git.revert({file: selectedFile.filepath!}))
                                                                .then(onOpen)
                                                                .catch(e => console.error(e))
                                                            },
                                                            {
                                                            label: 'Annuler',
                                                                onClick: () => {}
                                                            }
                                                        ]
                                                        });
                                                    }
                                        }}/> 
                                        )}
                                        <Anchor alignSelf='center' margin="small" label='Fermer' onClick={() => onClose()}/>
                                    </Box>                                                                    
                                </Box>
                            </Box> ) }
                    </Layer>
                    )
                }
            </Box>
    );
}

