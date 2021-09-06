// execute "../../../target/node gen-client-apis.js" to get result
// https://github.com/acacode/swagger-typescript-api

const { generateApi } = require('swagger-typescript-api');
const path = require("path");
const fs = require("fs");
var swaggerFile = "../server/target/swagger/swagger.json";

/* NOTE: all fields are optional expect one of `output`, `url`, `spec` */
generateApi({
  name: "EZLoadApi.ts",
  output:  path.resolve(process.cwd(), "src/ez-api/gen-api"),
  input: path.resolve(process.cwd(), swaggerFile),
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
  singleHttpClient: false,
  cleanOutput: true,
  enumNamesAsValues: true,
  moduleNameFirstTag: true,
  generateUnionEnums: true
});
