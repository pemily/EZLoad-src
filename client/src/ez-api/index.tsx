import axios from "axios";
import { Api, MainSettings, AuthInfo, HttpResponse, WebData } from './gen-api/EZLoadApi';
const Transform = require('stream').Transform;

export const ezApi = new Api({baseUrl:"http://localhost:8080/EZLoad/api"});

export function jsonCall(promise: Promise<HttpResponse<any, any>>) {
    return promise.then(httpResponse => httpResponse.json())
    .catch(e => {console.log(e); throw e});
}

 
export async function stream(promise: Promise<HttpResponse<any, any>>, onText: (value: string) => void, onDone: () => void){    
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
                    onText(new TextDecoder().decode(value));
                    push();
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

export function searchAccounts(courtier: 'BourseDirect'){    
    // ezApi.home.searchAccounts({courtier})
    stream(ezApi.home.test2(), t => console.log(t), () => console.log('DONE'));
si le retour est true => aller voir le process en cours
avec l'appel viewLogProcess si c'est false => dire désolé un autre process est en cours:
https://storybook.grommet.io/?path=/story/layout-layer-notification--notification-layer
}

 