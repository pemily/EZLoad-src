import { useState, useCallback, useEffect, ReactElement } from "react";
import { Box, Heading, Text, Button, Layer, DataTable, ColumnConfig } from "grommet";
import { FormDown, FormNext, Grow, CircleInformation, Tty } from 'grommet-icons';
import { valued } from '../../../ez-api/tools';
import { MainSettings, AuthInfo, EzProcess, EzData } from '../../../ez-api/gen-api/EZLoadApi';


interface EzSingleData {
  name: string,
  value: string
}

export interface EzDataProps {
  value: EzData;
  iconInfo: boolean;
  onSelect?: (d: EzSingleData) => void
} 

export function EzDataField(props: EzDataProps) {
  const [expandData, setExpandData] = useState(false);
 

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
  

  const data = Object.keys(props.value!.data!)
              .sort((key1, key2) => key1.localeCompare(key2))
              .map(key => { return { name: key, value: props.value!.data![key] }});

  return (          
      <>      
        <Button hoverIndicator="background" onClick={() => setExpandData(!expandData)}
           icon={props.iconInfo ? <CircleInformation color="brand"/> : <Tty color="brand"/>}/>
        { expandData && 
        <Layer onEsc={() => setExpandData(false)} onClickOutside={() => setExpandData(false)} margin="large" >
          <Heading margin="small" level="4" color="brand">Données extraites</Heading>
          <Box overflow="auto">
            <DataTable
              columns={columns}
              data={data}
              onClickRow={(event) => {                
                props.onSelect && props.onSelect(event.datum);
              }}/>                     
            </Box>   
            <Button margin="xsmall" alignSelf="center" size="small" label="Fermer" onClick={() => setExpandData(false)} />            
        </Layer> }
      </>
  );
}