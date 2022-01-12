import { Box, Anchor, Text, Layer, Button, Table, TableHeader, TableRow, TableCell, TableBody, } from "grommet";
import { Add, Checkmark, Trash } from 'grommet-icons';
import { useState } from "react";
import { TextField } from '../Tools/TextField';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css

export interface ProfileSelectorProps {    
    currentProfile?: string;
    allProfiles: string[];
    readOnly: boolean;    
    newProfile: () => void;
    deleteProfile: (profile: string) => void;
    rename: (oldName: string, newName: string) => void;
    activate: (profile: string) => void;
}

export function ProfileSelector(props: ProfileSelectorProps){
    const [open, setOpen] = useState(false);
 
    function showActions(index: number, profile: string){
        return (
            <Box key={"actions"+index} >
                <Box direction="row" align="center" key={"operationAction"+index}>                    
                    { props.currentProfile !== profile &&
                        (<>
                            <Anchor margin="small" color="brand" disabled={props.readOnly || profile === ""} label="Activer" onClick={() => props.activate(profile) }/>
                            <Button key={"delBD"+index} size="small" 
                                        disabled={props.readOnly}
                                        icon={<Trash color='status-critical' size='medium'/>} onClick={() =>{
                                        confirmAlert({
                                            title: 'Etes vous sûr de vouloir supprimer ce profile?',
                                            message: 'Tous les fichiers déjà téléchargés seront supprimés.',
                                            buttons: [
                                              {
                                                label: 'Oui',
                                                onClick: () => { props.deleteProfile(profile) }
                                              },
                                              {
                                                label: 'Non',
                                                onClick: () => {}
                                              }
                                            ]
                                          });
                                    }}/>     
                        </>)
                    }                
                    { props.currentProfile === profile &&
                        (<Checkmark color="green" size='medium'/>)
                    }
                </Box>
            </Box>
        );
    }

    return ( <>
        {props.currentProfile &&
            <Anchor margin="xsmall" color="brand" onClick={() => { setOpen(true); } } label={props.currentProfile}/> }
        {props.currentProfile && open &&
            <Layer onEsc={() => setOpen(false)} onClickOutside={() => setOpen(false)} margin="large" >
                <Box border={{ color: 'brand', size: 'small' }} pad="medium" overflow="auto">
                    <Table margin="xsmall" cellPadding="none" cellSpacing="none">
                    <TableHeader>
                        <TableRow>
                            <TableCell scope="row" border="bottom"><Text size="large">Profils</Text></TableCell>
                            <TableCell scope="row" border="bottom"></TableCell>                            
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {                                                                
                            props.allProfiles.map((profile, index) => (
                                <TableRow key={"Profile"+index} >
                                    <TableCell scope="row" pad="xsmall" margin="none">     
                                        <TextField key={"profile"+index} isRequired={true} id={'profile'+index} value={profile}
                                            readOnly={props.readOnly}                             
                                            onChange={newValue =>  props.rename(profile, newValue) }/>                                         
                                    </TableCell>
                                    <TableCell scope="row" pad="xsmall" margin="none">     
                                        {showActions(index, profile)}                                        
                                    </TableCell>
                                </TableRow> ))
                        }
                    </TableBody>
                    </Table>
                    <Button alignSelf="start" size="small" icon={<Add size='small'/>}  disabled={props.readOnly || props.allProfiles.findIndex(p => p === "") !== -1} label="Nouveau" onClick={() => {props.newProfile()}} />
                    <Button alignSelf="end" size="small" label="Fermer" onClick={() => {setOpen(false);}} />
                </Box>
            </Layer>
        }
      </> );
}
