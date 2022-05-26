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
                    <Button margin="small" alignSelf="center" size="small" label="Fermer" onClick={(e) => props.close()} />
                </Box>
            </Box>
    )

}