import { Api, MainSettings, EzProfil, AuthInfo, HttpResponse, RuleDefinitionSummary, RuleDefinition } from '../gen-api/EZLoadApi';

console.log("API Url is: http://localhost:"+window.location.port+"/api");
export const ezApi = new Api({baseUrl: "http://localhost:"+window.location.port+"/api"});

export interface SelectedRule {
  ruleDefinition: RuleDefinition;
  oldName: string|undefined;
}


export function valued(v: string|undefined|null) : string {
  return v ? v : "";
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