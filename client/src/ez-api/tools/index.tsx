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
import { Api, MainSettings, EzProfil, AuthInfo, HttpResponse, RuleDefinitionSummary, RuleDefinition, DashboardPageTimeLineChartSettings, Chart, TimeLineChartSettings } from '../gen-api/EZLoadApi';

console.log("API Url is: http://localhost:"+window.location.port+"/api");
export const ezApi = new Api({baseUrl: "http://localhost:"+window.location.port+"/api"});

export interface SelectedRule {
  ruleDefinition: RuleDefinition;
  oldName: string|undefined;
}


export function valued(v: string|undefined|null) : string {
  return v ? v : "";
}

export function isDefined(v: any) : boolean {
  return v !== undefined && v !== null;
}

export function jsonCall(promise: Promise<HttpResponse<any, any>>):  Promise<any> {
    return promise.then(httpResponse => {
      if (httpResponse.status === 204) return undefined; // no content for 204
      return httpResponse.json();
    } )    
}

// For method that returns string
export function textCall(promise: Promise<HttpResponse<any, any>>):  Promise<string|undefined> {
  return promise.then(httpResponse => {
    if (httpResponse.status === 204) return undefined; // no content for 204
    return httpResponse.text();
  } )    
}


// return true if the text contains the EZLoad signature => the user did not touch it
export function isTextContainsEZLoadSignature(text: string | undefined | null) : boolean{
  if (text === undefined || text === null || text.trim().length === 0){
      return true;
  }
  return text.startsWith(" ") && text.endsWith("\t ");
}

export function applyEZLoadTextSignature(text: string) : string {
  var result = text;
  if (!text.startsWith(" ")){
      result = " "+result;
  }
  if (!text.endsWith("\t ")){
      result = result  + "\t ";
  }
  return result;
}


// Pour ne pas risquer de supprimer la signature, si il y en a une
export function updateEZLoadTextWithSignature(oldValue: string | undefined | null, newValue: string) : string {
  return newValue.trim() === oldValue?.trim() ? oldValue : newValue
}
 
// onText return true if it wants to stop the streaming
export async function stream(promise: Promise<HttpResponse<any, any>>, onText: (value: string) => boolean, onDone: () => void): Promise<any>{    
    return promise
    .then(response => response.body)
    .then(body => {
        const reader = body?.getReader();
        return new ReadableStream({
            start() {
              // The following function handles each data chunk
              function push() {
                reader?.read().then( ({done, value}) => {
                  // If there is no more data to read
                  if (done) onDone();                        
                  else{
                    if (!onText(new TextDecoder().decode(value))){
                      push();
                    }
                    else{
                      reader.cancel();                      
                    }
                  }
                })
              }        
              push();
            }
          });
    });
}

function chart2TimeLineChartSettings(chart: Chart|TimeLineChartSettings) : TimeLineChartSettings {
  var c: Chart = {
      ...chart,
      lines: undefined,
      labels: undefined,
      axisId2titleX: undefined
  };
  delete c.lines;
  delete c.labels;
  delete c.axisId2titleX;
  delete c.axisId2titleY;
  return c;
}

export function saveDashboardConfig(dashConfig: DashboardPageTimeLineChartSettings[], keepLines: boolean, updModel: (dashConfig: DashboardPageTimeLineChartSettings[]) => void){
  jsonCall(ezApi.dashboard.saveDashboardConfig(dashConfig.map(page => { return {...page, charts: page.charts?.map(chart2TimeLineChartSettings)}})))
    .then(r => updModel(keepLines ? dashConfig : r))
    .catch(e => console.error("Save Dashboard Error: ", e));
}

export function saveEzProfile(settings: EzProfil, updModel: (settings: EzProfil) => void){      
  jsonCall(ezApi.home.saveEzProfile(settings))
    .then(r => updModel(r))
    .catch(e => console.error("Save Profil Error: ", e));
}

export function saveMainSettings(settings: MainSettings, updModel: (settings: MainSettings) => void){      
  jsonCall(ezApi.home.saveMainSettings(settings))
    .then(r => updModel(r))
    .catch(e => console.error("Save Main Settings Error: ", e));
}

export function savePassword(courtier: 'BourseDirect', username: string|undefined, password: string|undefined, updModel: (authInfo: AuthInfo) => void){
    const newAuth = {username, password};
    jsonCall(ezApi.security.createUserPassword({courtier}, newAuth))
    .then(r => updModel(newAuth))
    .catch(e => console.error("Save Password Error: ", e));
}

export function genUUID(){
  return crypto.randomUUID();
}

export function getChromeVersion() : string {
  let pieces = navigator.userAgent.match(/Chrom(?:e|ium)\/([0-9]+)\.([0-9]+)\.([0-9]+)\.([0-9]+)/);
  if (pieces == null || pieces.length !== 5) {
      return "";
  }
  let pieces2 = pieces.map(piece => parseInt(piece, 10));
  return pieces2[1]+"."+ pieces2[2]+"."+ pieces2[3]+ "."+ pieces2[4];  
}


export function ruleTitle(rule: RuleDefinitionSummary|undefined): string{
  if (rule === undefined) return "";
  return rule.broker+" v"+rule.brokerFileVersion+" - "+rule.name;
}

export function strToBroker(brokerName: string | undefined) : 'BourseDirect'|undefined{
  if (brokerName === undefined) return undefined;
  if (brokerName === 'Bourse Direct') return 'BourseDirect';
  return undefined;
}

export function ruleToFilePath(filename: string, broker: | "Autre"
| "Axa"
| "Binck"
| "BNP"
| "BourseDirect"
| "Boursorama"
| "CreditAgricole"
| "CreditDuNord"
| "CreditMutuel"
| "DeGiro"
| "eToro"
| "Fortuneo"
| "Freetrade"
| "GFX"
| "INGDirect"
| "InteractiveBroker"
| "LCL"
| "LynxBroker"
| "NominatifPur"
| "SaxoBanque"
| "SocieteGenerale"
| "TradeRepublic"
| "Trading212"
| "Revolut", /** @format int32 */ brokerFileVersion: number){
  return broker+"_v"+brokerFileVersion+"/"+filename;
}