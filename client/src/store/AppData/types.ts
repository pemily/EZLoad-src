import { number } from "prop-types";

export interface IAppData {
    ideas: IIdea[];
    friends: IFriend[];
    categories: ICategory[];
    layout: ILayoutState;
}

export interface IIdea {
    id: string;
    content: IIdeaContent;
}

export interface IIdeaContent {
    name: string;
    description?: string;
    stars?: number;
}


export interface ICategory {
    id: string;
    content: ICategoryContent;
}
export interface ICategoryContent {
    name: string;
    expire?: string; // format is dd mm yyyy
    selectedIdeas?: string[];
}
export interface IHisIdeas {
    status: Status;
    errorMessage?: string;
    ideas?: IFriendIdea[];
}
export interface ISearchFriendIdea {
    category: ICategory;
    ideas: IIdea[];
}
export interface IFriendIdea {
    idea: IIdea;
    categories: ICategory[];
}

export interface IFriend {
    encodedEmail: string;
    email: string; // will be computed when reading firebase (decoded from the encodedEmail)
    fromMe?: IFriendContentFromMe; // when I update the info for this user (exemple: selectedCategories)
    fromHim?: IFriendContentFromHim; // when the friend wants to update its profile info on my account
    hisIdeas?: IHisIdeas;
}

export interface IFriendContentFromMe {
    answerToDo: boolean; // if I have to give an answer to my friend
    name?: string;
    photoUrl?: string;
    selectedCategories?: string[];
}

export interface IFriendContentFromHim {
    accepted: boolean; // if my friend accepted me
    name?: string;
    photoUrl?: string;
}

/************************************************************************************/
/*********************************** Layout *****************************************/
/************************************************************************************/
export enum Status {
    Loading,
    Error,
    Ready,
}

export enum PageView {
  CATEGORIES_VIEW,
  FRIENDS_VIEW,
  IDEAS_VIEW,
}

export interface IPage {
    status: Status;
    errorMessage?: string;
    view: PageView;
}

export interface ILayoutState {
    rightMainMenuVisible: boolean;
    page: IPage;
}