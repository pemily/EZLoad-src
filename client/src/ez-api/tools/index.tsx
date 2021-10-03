import { Api, MainSettings, AuthInfo, HttpResponse } from '../gen-api/EZLoadApi';

export const ezApi = new Api({baseUrl:"http://localhost:8080/EZLoad/api"}); // TODO update => remove the 8080 before the production, the port is dynamically computed

export function valued(v: string|undefined|null) : string {
  return v ? v : "";
}


export function jsonCall(promise: Promise<HttpResponse<any, any>>) {
    console.log("jsonCall");
    return promise.then(httpResponse => {
      if (httpResponse.status === 204) return null; // no content for 204
      return httpResponse.json();
    } )
    .catch(e => {console.log("Promise error: ", e); throw e});
}

 
// onText return true if it wants to stop the streaming
export async function stream(promise: Promise<HttpResponse<any, any>>, onText: (value: string) => boolean, onDone: () => void){    
    promise
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
    })
    .catch(e => console.log("Stream error: ", e));      
}


export function saveSettings(settings: MainSettings, updModel: (settings: MainSettings) => void){    
  console.log("Saving settings", settings);
  jsonCall(ezApi.home.saveSettings(settings))
    .then(r => updModel(r))
    .catch(e => console.log("Save Settings Error: ", e));
}

export function savePassword(courtier: 'BourseDirect', username: string|undefined, password: string|undefined, updModel: (authInfo: AuthInfo) => void){
    const newAuth = {username, password};
    jsonCall(ezApi.security.createUserPassword({courtier}, newAuth))
    .then(r => updModel(newAuth))
    .catch(e => console.log("Save Password Error: ", e));
}


export function getChromeVersion() : string {
  let pieces = navigator.userAgent.match(/Chrom(?:e|ium)\/([0-9]+)\.([0-9]+)\.([0-9]+)\.([0-9]+)/);
  if (pieces == null || pieces.length !== 5) {
      return "";
  }
  let pieces2 = pieces.map(piece => parseInt(piece, 10));
  return pieces2[1]+"."+ pieces2[2]+"."+ pieces2[3]+ "."+ pieces2[4];  
}



