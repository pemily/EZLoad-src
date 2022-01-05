import { Box, List, Text } from "grommet";
import { TextField } from '../Tools/TextField';
import { ShareValue } from '../../ez-api/gen-api/EZLoadApi';

export interface NewShareValuesProps {
    newShareValues: ShareValue[]|undefined;
    processRunning: boolean;
    saveShareValue: (newVal: ShareValue) => void;
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
                {(shareValue: ShareValue, index: number) => (
                    <Box direction="row" margin="xsmall">
                        <Text size="small" alignSelf="center">{shareValue.tickerCode}</Text>
                        <TextField 
                        onChange={newVal => {
                                    if (newVal !== shareValue.userShareName) 
                                        props.saveShareValue({...shareValue, userShareName:newVal, dirty:true})
                                    }}
                        value={shareValue.userShareName}
                        id={"shareValue"+index}                                 
                        isRequired={true}                  
                        readOnly={props.processRunning}
                        />
                    </Box>)} 
            </List>            
        </Box>
    );
}

