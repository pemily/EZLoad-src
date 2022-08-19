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
import { Box, List, Text, Anchor } from "grommet";
import { FormSearch } from 'grommet-icons';
import { TextField } from '../Tools/TextField';
import { EZShare } from '../../ez-api/gen-api/EZLoadApi';

export interface NewShareValuesProps {
    newShareValues: EZShare[]|undefined;
    processRunning: boolean;
    saveShareValue: (newVal: EZShare) => void;
}      

export function NewShareValues(props: NewShareValuesProps){
    return (
        <Box margin="medium" >            
            {props.newShareValues && props.newShareValues.length > 0 
                && (<><Text>Nouvelle(s) valeur(s) detectée(s)!</Text>
                <Text>Vous pouvez le(s) renommer maintenant (il sera plus difficile de le faire dans EZPortfolio)</Text>
                <Text size="small">Puis cliquez sur "Générer les opérations" pour prendre en compte vos modifications</Text></>)}
            <List data={props.newShareValues} margin="none" pad="none"
             background={['light-2', 'light-4']}>
                {(shareValue: EZShare, index: number) => (
                    <Box direction="row-responsive" margin="xsmall">                        
                        <TextField 
                        label="Nom"
                        onChange={newVal => {
                                    if (newVal !== shareValue.ezName) 
                                        props.saveShareValue({...shareValue, ezName:newVal})
                                    }}
                        value={shareValue.ezName}
                        id={"shareValue"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />
                        
                        <TextField                        
                        label='ISIN'
                        onChange={newVal => {
                                    if (newVal !== shareValue.isin) 
                                        props.saveShareValue({...shareValue, isin:newVal})
                                    }}
                        value={shareValue.isin}
                        id={"isin"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />                         
                        <Anchor alignSelf="center" target={"yahoo"+shareValue.isin} color="brand" href={"https://www.boursedirect.fr/fr/marches/recherche?q="+shareValue.isin} icon={<FormSearch size="medium"/>}/>

                        <TextField
                        label="Google Code"
                        onChange={newVal => {
                                    if (newVal !== shareValue.googleCode) 
                                        props.saveShareValue({...shareValue, googleCode:newVal})
                                    }}
                        value={shareValue.googleCode}
                        id={"googleCode"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />    
                        <Anchor alignSelf="center" target={"yahoo"+shareValue.googleCode} color="brand" href={"https://www.google.com/finance/quote/"+shareValue.googleCode} icon={<FormSearch size="medium"/>}/>

                        <TextField
                        label="SeekingAlpha Code"
                        onChange={newVal => {
                                    if (newVal !== shareValue.seekingAlphaCode) 
                                        props.saveShareValue({...shareValue, seekingAlphaCode:newVal})
                                    }}
                        value={shareValue.seekingAlphaCode}
                        id={"seekingAlphaCode"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />                                   
                        <Anchor alignSelf="center" target={"seeking"+shareValue.seekingAlphaCode} color="brand" href={"https://seekingalpha.com/symbol/"+shareValue.seekingAlphaCode} icon={<FormSearch size="medium"/>}/>                        
    
                        <TextField           
                        label="Yahoo Code"             
                        onChange={newVal => {
                                    if (newVal !== shareValue.yahooCode) 
                                        props.saveShareValue({...shareValue, yahooCode:newVal})
                                    }}
                        value={shareValue.yahooCode}
                        id={"yahooSymbol"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />            
                        <Anchor alignSelf="center" target={"yahoo"+shareValue.yahooCode} color="brand" href={"https://finance.yahoo.com/quote/"+shareValue.yahooCode} icon={<FormSearch size="medium"/>}/>
                    
                        <TextField 
                        label="Type"
                        onChange={newVal => {
                                    if (newVal !== shareValue.type) 
                                        props.saveShareValue({...shareValue, type:newVal})
                                    }}
                        value={shareValue.type}
                        id={"shareType"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />
                        
                        <TextField
                        label="Industrie"
                        onChange={newVal => {
                                    if (newVal !== shareValue.industry) 
                                        props.saveShareValue({...shareValue, industry:newVal})
                                    }}
                        value={shareValue.industry}
                        id={"industry"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />                         
                    </Box>)} 
            </List>            
        </Box>
    );
}

