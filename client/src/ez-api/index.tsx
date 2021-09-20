import { Api, MainSettings, AuthInfo } from './gen-api/EZLoadApi';

export const ezApi = new Api({baseURL:"http://localhost:8080/EZLoad/api"});

export function saveSettings(settings: MainSettings, updModel: (settings: MainSettings) => void){    
    ezApi.home.saveSettings(settings)
    .then(r => {        
        updModel(r.data);
    })
    .catch(e => console.log(e));
}

export function savePassword(courtier: 'BourseDirect', username: string|undefined, password: string|undefined, updModel: (authInfo: AuthInfo) => void){
    const newAuth = {username, password};
    ezApi.security.createUserPassword({courtier}, newAuth)
    .then(r => updModel(newAuth))
    .catch(e => console.log(e));
}

export function searchAccounts(courtier: 'BourseDirect'){
    ezApi.home.searchAccounts({courtier})
        .then()
}