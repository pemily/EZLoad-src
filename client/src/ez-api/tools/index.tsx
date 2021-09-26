import { Api, MainSettings, AuthInfo, HttpResponse } from '../gen-api/EZLoadApi';

export const ezApi = new Api({baseUrl:"http://localhost:8080/EZLoad/api"}); // TODO update => remove the 8080 before the production, the port is dynamically computed

export function jsonCall(promise: Promise<HttpResponse<any, any>>) {
    return promise.then(httpResponse => httpResponse.json())
    .catch(e => {console.log(e); throw e});
}

 
export async function stream(promise: Promise<HttpResponse<any, any>>, onText: (value: string) => void, onDone: () => void){    
    promise
    .then(response => response.body)
    .then(body => {
        const reader = body?.getReader();
        console.log("aaaaaaaa");
        return new ReadableStream({
            start() {
              console.log("bbbbbb");
              // The following function handles each data chunk
              function push() {
                console.log("ddddddd");
                reader?.read().then( ({done, value}) => {
                  console.log("value:", value);
                  // If there is no more data to read
                  if (done) onDone();                        
                  else{
                    onText(new TextDecoder().decode(value));
                    push();
                  }
                })
              }        
              push();
              console.log("ccccc");
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


 