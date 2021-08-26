import React from "react";

import { Box, Image, Stack, Text } from "grommet";

import { ButtonLnk } from "../Tools/ButtonLnk";


const HomePage = () => (
      <div>
        <Box
          justify="center"
          align="center"
          pad="large"
          wrap={true}
          direction="row-responsive"
          width="100%"
        >
          <Box animation="fadeIn" alignContent="center" alignSelf="center" align="center"
            background={{ color: "brand", opacity: "strong" }}
            round="large" elevation="large" pad="medium" responsive={true}>
              <Text color="white" size="medium" alignSelf="center">

                  // tslint:disable-next-line: max-line-length
                  <p>You never know what to offer to your family, children, spouse, friends?</p><p>This site is for you!!!</p>
                  // tslint:disable-next-line: max-line-length


                It's FREE and HERE
                <ButtonLnk alignSelf="center"  size="xxlarge"    text="Go"/>

              </Text>
          </Box>
          <Stack anchor="center" alignSelf="center">
              <Image fit="cover" src="./img/cadeaux640.png" alignSelf="center" width="100%"/>


          </Stack>

        </Box>

      </div>
);

export default HomePage;