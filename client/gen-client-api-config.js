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
// execute "../../../target/node gen-client-apis.js" to get result
// https://github.com/acacode/swagger-typescript-api

const { generateApi } = require('swagger-typescript-api');
const path = require("path");
var swaggerFile = "../server/target/swagger/swagger.json";

/* NOTE: all fields are optional expect one of `output`, `url`, `spec` */
generateApi({
  name: "EZLoadApi.ts",
  output:  path.resolve(process.cwd(), "src/ez-api/gen-api"),
  input: path.resolve(process.cwd(), swaggerFile),
  httpClientType: "fetch", // "axios" or "fetch"
  prettier: {
    printWidth: 120,
    tabWidth: 2,
    trailingComma: "all",
    parser: "typescript",
  },  
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
