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
import { Box, List, Text, Anchor, Button } from "grommet";
import { FormSearch, Trash, Target } from 'grommet-icons';
import { TextField } from '../Tools/TextField';
import { useState, useEffect } from "react";
import { ezApi, jsonCall } from '../../ez-api/tools';
import { EZShare, ActionWithMsg, EzProcess } from '../../ez-api/gen-api/EZLoadApi';
import { confirmAlert } from 'react-confirm-alert'; // Import
import 'react-confirm-alert/src/react-confirm-alert.css'; // Import css


export interface NewShareValuesProps {
    showNewSharesDetectedWarning: boolean,
    actionWithMsg: ActionWithMsg|undefined;
    processRunning: boolean;
    followProcess: (process: EzProcess|undefined) => void;
    readOnly: boolean;
    saveShareValue: (index: number, newVal: EZShare) => void;
    deleteShareValue?: (index: number) => void;
}      

function shareSearchUrl(field: string|undefined, share: EZShare) : string | undefined {    
    if (field === undefined || field === "" || field === null)
        return share.ezName ? encodeURIComponent(share.ezName) : share.googleCode;        
    return field;
}

function googleCodeReversed(share: EZShare) : string|undefined {    
    // googleCodeFromEzPortfolio EPA:FDJ => return FDJ:EPA 
    var googleCodeFromEzPortfolio = share.googleCode;
    if (googleCodeFromEzPortfolio === undefined) return undefined;    
    var code = googleCodeFromEzPortfolio?.split(":")    
    if (code === undefined ||code?.length <= 1) return googleCodeFromEzPortfolio;    
    var r = code[1]+":"+code[0];    
    return r;
}

export function ShareValues(props: NewShareValuesProps){
    const [readOnly, setReadOnly] = useState<boolean>(props.processRunning);

    useEffect(() => {
        // will be executed when props.processRunning will become true 
        setReadOnly(props.processRunning);
    }, [ props.processRunning, props.actionWithMsg ]);

    return (
        <Box margin="medium" >   
         { !props.showNewSharesDetectedWarning && (<Button
            fill="vertical"
            alignSelf="end"                    
            disabled={props.readOnly} onClick={() =>
                jsonCall(ezApi.home.checkAllShares())
                .then(props.followProcess)               
                .catch(e => console.error(e))
            }
            size="small" icon={<Target size='small'/>} label="Rechercher les erreurs"/> ) }
            {props.showNewSharesDetectedWarning && props.actionWithMsg?.actions && props.actionWithMsg.actions.length > 0 
                && (<><Text>Nouvelle(s) valeur(s) detectée(s)!</Text>
                <Text>Vous pouvez le(s) renommer maintenant (il sera plus difficile de le faire dans EZPortfolio)</Text>
                <Text size="small">Puis cliquez sur "Générer les opérations" pour prendre en compte vos modifications</Text></>)}
            <List data={props.actionWithMsg?.errors} margin="small" pad="none">
                {(error: string) => (<Text size="small" color='status-critical'>{error}</Text>)}
            </List>
            <List data={props.actionWithMsg?.actions} margin="none" pad="none"
             background={['light-2', 'light-4']}>
                {(shareValue: EZShare, index: number) => (
                    <Box direction="row-responsive" margin="xsmall">                        
                        <TextField 
                        label="Nom"
                        onChange={newVal => {
                                    if (newVal !== shareValue.ezName) {
                                        props.saveShareValue(index, {...shareValue, ezName:newVal})
                                    }}}
                        value={shareValue.ezName}
                        id={"shareValue"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />
                        
                        <TextField                        
                        label='ISIN'
                        onChange={newVal => {
                                    if (newVal !== shareValue.isin) {
                                        props.saveShareValue(index, {...shareValue, isin:newVal})
                                    }}}
                        value={shareValue.isin}
                        id={"isin"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />                         
                        <Anchor alignSelf="center" target={"boursedirect"+shareValue.isin} color="brand" href={"https://www.google.fr/search?q=ISIN+%22"+shareSearchUrl(shareValue.isin, shareValue)+"%22 "+shareValue.googleCode} icon={<FormSearch size="medium"/>}/>

                        <TextField
                        label="Google Code"
                        onChange={newVal => {
                                    if (newVal !== shareValue.googleCode) {
                                        props.saveShareValue(index, {...shareValue, googleCode:newVal})
                                    }}}
                        value={shareValue.googleCode}
                        id={"googleCode"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />    
                        <Anchor alignSelf="center" target={"google"+shareValue.googleCode} color="brand" href={"https://www.google.com/finance/quote/"+shareSearchUrl(googleCodeReversed(shareValue), shareValue)} icon={<FormSearch size="medium"/>}/>

                        <TextField
                        label="SeekingAlpha Code"
                        onChange={newVal => {
                                    if (newVal !== shareValue.seekingAlphaCode) {
                                        props.saveShareValue(index, {...shareValue, seekingAlphaCode:newVal})
                                    }}}
                        value={shareValue.seekingAlphaCode}
                        id={"seekingAlphaCode"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />                                   
                        <Anchor alignSelf="center" target={"seeking"+shareValue.seekingAlphaCode} color="brand" href={"https://seekingalpha.com/symbol/"+shareSearchUrl(shareValue.seekingAlphaCode, shareValue)} icon={<FormSearch size="medium"/>}/>                        
    
                        <TextField           
                        label="Yahoo Code"             
                        onChange={newVal => {
                                    if (newVal !== shareValue.yahooCode) {
                                        props.saveShareValue(index, {...shareValue, yahooCode:newVal})
                                    }}}
                        value={shareValue.yahooCode}
                        id={"yahooSymbol"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />            
                        <Anchor alignSelf="center" target={"yahoo"+shareValue.yahooCode} color="brand" href={"https://finance.yahoo.com/quote/"+shareSearchUrl(shareValue.yahooCode, shareValue)} icon={<FormSearch size="medium"/>}/>

                        <TextField 
                        label="Pays"
                        onChange={newVal => {
                                    if (newVal !== shareValue.countryCode) {
                                        props.saveShareValue(index, {...shareValue, countryCode:newVal})
                                    }}}
                        value={shareValue.countryCode}
                        id={"countryCode"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />

                        <TextField 
                        label="Type"
                        onChange={newVal => {
                                    if (newVal !== shareValue.type) {
                                        props.saveShareValue(index, {...shareValue, type:newVal})
                                    }}}
                        value={shareValue.type}
                        id={"shareType"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />

                        <TextField
                        label="Industrie"
                        onChange={newVal => {
                                    if (newVal !== shareValue.industry) {
                                        props.saveShareValue(index, {...shareValue, industry:newVal})
                                    }}}
                        value={shareValue.industry}
                        id={"industry"+index}                                 
                        isRequired={true}                  
                        readOnly={readOnly}
                        />    

                        {   props.deleteShareValue && 
                                        <Button key={"delBD"+index} size="small"        
                                            disabled={readOnly}                                 
                                            icon={<Trash color='status-critical' size='medium'/>} onClick={() =>{
                                                        confirmAlert({
                                                            title: 'Etes vous sûr de vouloir supprimer cette action?',                                                            
                                                            buttons: [
                                                            {
                                                                label: 'Oui',
                                                                onClick: () => { if (props.deleteShareValue){
                                                                                        setReadOnly(true);
                                                                                        props.deleteShareValue(index);
                                                                                }}
                                                            },
                                                            {
                                                                label: 'Non',
                                                                onClick: () => {}
                                                            }
                                                            ]
                                                        })
                                            }}/>        
                        }
                    </Box>)} 
            </List>            
        </Box>
    );
}

