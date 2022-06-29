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
import { useState } from "react";
import { Accordion, AccordionPanel, Box, Heading, Text, Button, Layer, DataTable, ColumnConfig } from "grommet";
import { CircleInformation, Tty } from 'grommet-icons';
import { EzData } from '../../../ez-api/gen-api/EZLoadApi';


export interface EzSingleData {
  name: string,
  value: string
}

export interface EzDataProps {
  value: EzData | undefined;
  iconInfo: boolean;
  jsonText?: string;
  onSelect?: (d: EzSingleData) => void
} 

export function EzDataField(props: EzDataProps) {
  const [open, setOpen] = useState(false);
 

  const columns: ColumnConfig<EzSingleData>[] = [
    { property: 'name',
      header: <Text>Nom</Text>,
      primary: true
    },
    { property: 'value',
      header: <Text>Valeur</Text>,
      render: (row: EzSingleData) => row.value
    }
  ];
  
  const data = props.value && props.value?.data && Object.keys(props.value.data)
              .sort((key1, key2) => key1.localeCompare(key2))
              .map(key => { return { name: key, value: props.value!.data![key] }});

  return  (          
      <>  
      {props.value && props.value?.data && (    
        <>
          <Button hoverIndicator="background" onClick={() => setOpen(!open)}
             icon={props.iconInfo ? <CircleInformation color="brand"/> : <Tty color="brand"/>}/>
          { open && 
            <Layer onEsc={() => setOpen(false)} onClickOutside={() => setOpen(false)} margin="large" >
              <Heading margin="small" level="4" color="brand">Données extraites</Heading>
              <Box overflow="auto">
                { !props.value && (<Text alignSelf="center">Aucune données</Text>)}
                { props.value && 
                    (
                      <>
                        <DataTable
                          columns={columns}
                          data={data}
                          onClickRow={(event) => {                
                            if (props.onSelect){ 
                              props.onSelect(event.datum);
                              setOpen(false);
                            }}}
                        />
                        <Accordion margin="medium">
                          <AccordionPanel label="Json Operation">
                            <pre>
                              {props.jsonText}
                            </pre>
                          </AccordionPanel>
                        </Accordion>
                      </> 
                    )}
                </Box>   
                <Button margin="medium" alignSelf="center" size="small" label="Fermer" onClick={() => setOpen(false)} />            
            </Layer> 
          }
        </>)
      }
      </>
  );
}