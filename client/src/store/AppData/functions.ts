import { IAppData, ICategory, IFriend, IIdea } from ".";

export function mapCategory(appData: IAppData,
                            f: (appData: IAppData, categories: ICategory[]) => ICategory[],
        ): IAppData {
    return {
        ...appData,
        categories: f(appData, appData.categories),
    };
}

export function mapIdea(appData: IAppData,
                        f: (appData: IAppData, ideas: IIdea[]) => IIdea[],
        ): IAppData {
    return {
        ...appData,
        ideas: f(appData, appData.ideas),
    };
}


export function mapFriend(appData: IAppData,
                          f: (appData: IAppData, friends: IFriend[]) => IFriend[],
    ): IAppData {
    return {
        ...appData,
        friends: f(appData, appData.friends),
    };
}
