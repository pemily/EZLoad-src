import { Api, MainSettings, AuthInfo } from './gen-api/EZLoadApi';
const Transform = require('stream').Transform;

export const ezApi = new Api({baseURL:"http://localhost:8080/EZLoad/api"});

export function saveSettings(settings: MainSettings, updModel: (settings: MainSettings) => void){    
    ezApi.home.saveSettings(settings)
    .then(r => {        
        updModel(r.data);
    })
    .catch(e => console.log("Save Settings Error: ", e));
}

export function savePassword(courtier: 'BourseDirect', username: string|undefined, password: string|undefined, updModel: (authInfo: AuthInfo) => void){
    const newAuth = {username, password};
    ezApi.security.createUserPassword({courtier}, newAuth)
    .then(r => updModel(newAuth))
    .catch(e => console.log("Save Password Error: ", e));
}


export function searchAccounts(courtier: 'BourseDirect'){    
    const htmlReportingStream = new Transform({        
        transform(chunk: any, encoding: any, callback: () => void) {
            console.log('DDDDDDDDDDDDDDDDDDDDDDDD');
            this.push(chunk);
            callback();
        },
    });
    
    ezApi.home.searchAccounts({courtier}, {format: 'stream'})
    .then(r => {
        console.log('AAAAAAAAAAAAAAAAAAAAAAA');
        r.data.pipe(htmlReportingStream);
        console.log('BBBBBBBBBBBBBBBBBBB');
        //https://stackoverflow.com/questions/65407349/streaming-axios-response-from-a-get-request-in-nodejs
        console.log("SearchAccounts", r);
    })
    .catch((e) => console.log('Search Accounts Error: ', e));      
    
    htmlReportingStream.on('data', (chunk: any) => {
        console.log('CCCCCCCCCCCCCCCCCCC');
        console.log(chunk);
    })
}