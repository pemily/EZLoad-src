import { useState, useEffect } from "react";
import { Anchor, Layer, Box, List, Button, Text } from "grommet";
import { ezApi, jsonCall } from '../../../ez-api/tools';
import { Item} from '../../../ez-api/gen-api/EZLoadApi';
import { SourceFileLink } from "../SourceFileLink";


export interface FileBrowserProps {
  visible: boolean;
  close: () => void;
}

export function FileBrowser(props: FileBrowserProps) {
    const [currentDir, setCurrrentDir] = useState<string|undefined>(undefined);    
    const [items, setItems] = useState<Item[]>([]);

    useEffect(() => {
        loadDir(undefined)
    }, []);

    function loadDir(dir: string|undefined) : Promise<any> {
        return jsonCall(ezApi.explorer.list({dirpath: dir === undefined ? "": dir}))
        .then(items => {
            console.log("Items: ", items)
            setCurrrentDir(dir);
            setItems(items);
        })
        .catch(e => console.error(e));
    };

    function showItemActions(index: number, item: Item){
        return (<></>); // no actions for the moment, later???
    };
 
    function buildPath(dir: string|undefined, itemName: string|undefined){
        if (itemName === "..") {
            if (dir === undefined || dir === "") return "";
            return dir+"/..";
        }
        if (dir === undefined || dir === "")
            return itemName;                   
        return dir+"/"+itemName;
    }

    return (
      <> 
        { props.visible &&  (
            <Layer full position="center" margin="large" onEsc={props.close} onClickOutside={props.close} >
                <Box margin="medium" align="center" >
                    <Text>{"Dossier "+(currentDir === undefined? "" : currentDir)}</Text>
                </Box>
                <Box align="start" height="xxlarge" overflow='auto' fill>
                    {
                        currentDir !== undefined && currentDir !== "" && 
                            (<Anchor margin={{start: "medium", bottom: "none"}} key={"dirParent"}
                             onClick={e => loadDir(currentDir.split('/').slice(0, -1).join('/'))}>[Parent]</Anchor>)
                    }
                    <Box direction="row"  alignContent="stretch" fill>
                        { items.length === 0 && ( <Text margin="medium">Aucun fichier</Text>)}
                        { items.length > 0 && (
                        <List data={items} margin={{top: "none", start:"medium"}} pad="xsmall" background={['light-2', 'light-4']} style={{width: "90%"}}
                        action={(item, index) => showItemActions(index, item) }>
                            {(item: Item, index: number) => {
                                if (item.dir){
                                    return (<Anchor key={"dir"+index} onClick={e => loadDir(buildPath(currentDir, item.name))}>{item.name}</Anchor>)
                                }
                                else{
                                    return (<SourceFileLink key={"file"+index} sourceFile={buildPath(currentDir, item.name)}/>)
                                }
                            }} 
                        </List>  )}
                    </Box>
                </Box>
                <Button margin="large" alignSelf="center" label="Fermer" onClick={(e) => props.close()}/> 
            </Layer>
        )}
      </>
    );
}