import { Api, MainSettings } from './gen-api/EZLoadApi';

export const ezApi = new Api({baseURL:"http://localhost:8080/EZLoad/api"});

export function saveSettings(settings: MainSettings, updModel: (settings: MainSettings) => void){
    alert('get saveSettings4');
    ezApi.home.saveSettings4(settings);
}