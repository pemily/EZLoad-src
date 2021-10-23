import { useState } from "react";
import { Box, Heading, Text, Button, Layer, DataTable, ColumnConfig } from "grommet";
import { CircleInformation, Tty } from 'grommet-icons';
import { EzData } from '../../../ez-api/gen-api/EZLoadApi';


export interface EzSingleData {
  name: string,
  value: string
}

export interface EzDataProps {
  value: EzData | undefined;
  iconInfo: boolean;
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
  

  const data = props.value && Object.keys(props.value.data!)
              .sort((key1, key2) => key1.localeCompare(key2))
              .map(key => { return { name: key, value: props.value!.data![key] }});

  return (          
      <>      
        <Button hoverIndicator="background" onClick={() => setOpen(!open)}
           icon={props.iconInfo ? <CircleInformation color="brand"/> : <Tty color="brand"/>}/>
        { open && 
        <Layer onEsc={() => setOpen(false)} onClickOutside={() => setOpen(false)} margin="large" >
          <Heading margin="small" level="4" color="brand">Données extraites</Heading>
          <Box overflow="auto">
            { !props.value && (<Text alignSelf="center">Aucune données</Text>)}
            { props.value && (<DataTable
              columns={columns}
              data={data}
              onClickRow={(event) => {                
                props.onSelect && props.onSelect(event.datum);
              }}/> )}
            </Box>   
            <Button margin="xsmall" alignSelf="center" size="small" label="Fermer" onClick={() => setOpen(false)} />            
        </Layer> }
      </>
  );
}