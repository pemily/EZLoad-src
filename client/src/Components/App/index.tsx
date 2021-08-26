import React, { Component } from "react";
import { Counter } from '../../features/counter/Counter';
/*
import { Box, Collapsible } from "grommet";
import { Spinning } from "grommet-controls";
import { ButtonLnk } from "../Tools/ButtonLnk";*/
/*
const loadingImg = <Box align="center" justify="center" fill pad="xlarge">
  <Spinning kind="cube-grid" size="xlarge" />
</Box>; */


interface IAppProps {
  mainMenuVisible: boolean;
}

type Props = IAppProps;

class App extends Component<Props> {
  /*constructor(props: Props) {
    super(props);

    // this.state = { isUserConnected: false };
    // this.toggleMainMenu = this.toggleMainMenu.bind(this);
  }*/

      /*    <Box flex direction="row">
            <Box flex align="center" justify="center">
            </Box>
                <Collapsible direction="horizontal" open={true}>
                  <Box
                    flex
                    width="small"
                    pad="xsmall"
                    gap="xsmall"
                    align="start"
                    justify="start"
                  >

       <ButtonLnk margin="xsmall" alignSelf="start"
                        text="Account" />
                  </Box>
                </Collapsible>
          </Box> */

  public render() {
    return (
              <div> Bonjour  <Counter /></div>

    );
  }

}




export default App;