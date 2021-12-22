import { useState  } from "react";
import { Calendar, Button, Box, Text } from "grommet";
import { BourseDirectEZAccountDeclaration } from '../../ez-api/gen-api/EZLoadApi';

export interface ConfigStartDateProps {    
    account: BourseDirectEZAccountDeclaration;    
    close: () => void;
    saveStartDate: (date: string, account: BourseDirectEZAccountDeclaration) => void;
}      

export function ConfigStartDate(props: ConfigStartDateProps){
    const [startDate, setStartDate] = useState<string>(new Date().toISOString());
    
    return (
            <Box margin="medium" border>
                <Text margin ="xsmall" alignSelf="center" textAlign="center">Cette date sera sauvegardé dans EzPortfolio</Text>
                <Text margin ="xsmall" alignSelf="center" textAlign="center">Tous les relevés d'opérations avant cette date seront ignorés pour ce compte</Text>
                <Text margin="small" alignSelf="center">{props.account?.name + " "+props.account?.number}</Text>
                <Calendar alignSelf="center" size="small"  date={startDate} margin="medium"
                    onSelect={(strDate) => setStartDate(strDate+"")}/>
                <Box direction="row" alignSelf="end">
                    <Button margin="small" alignSelf="center" size="small" label="Sauvegarder" onClick={(e) =>{ 
                            props.close(); 
                            props.saveStartDate(startDate, props.account);                            
                    } } />
                    <Button margin="small" alignSelf="center" size="small" label="close" onClick={(e) => props.close()} />
                </Box>
            </Box>
    )

}