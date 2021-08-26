// execute "../../../target/node gen-client-apis.js" to get result
// https://github.com/acacode/swagger-typescript-api

const { generateApi } = require('swagger-typescript-api');
const path = require("path");
const fs = require("fs");
var file = "../../../../server/target/swagger/swagger.json";

/* NOTE: all fields are optional expect one of `output`, `url`, `spec` */
generateApi({
  name: "BientotRentierApi.ts",
  output:  path.resolve(process.cwd(), "generated/client"),
  input: path.resolve(process.cwd(), file),
  httpClientType: "axios", // "axios" or "fetch"
  prettier: {
    printWidth: 120,
    tabWidth: 2,
    trailingComma: "all",
    parser: "typescript",
  },
  httpClientType: "axios", // or "fetch"
  defaultResponseAsSuccess: false,
  generateRouteTypes: true,
  generateResponses: true,
  toJS: true,
  extractRequestParams: true,
  extractRequestBody: true,
  defaultResponseType: "void",
  singleHttpClient: true,
  cleanOutput: true,
  enumNamesAsValues: true,
  moduleNameFirstTag: true,
  generateUnionEnums: true
});
