import { Dispatch } from "react";
 import { removeUndefined } from "../function";
import { ICategory, IFriend, IFriendContentFromHim, IFriendContentFromMe, IFriendIdea, IIdea, IIdeaContent, IPage, PageView, Status } from "./types";


/************************************************************************************/
/*********************************** Layout *****************************************/
/************************************************************************************/
export const MAIN_MENU_VISIBILITY_CHANGE = "MAIN_MENU_VISIBILITY_CHANGE";
export const MAIN_MENU_VISIBILITY_TOGGLE = "MAIN_MENU_VISIBILITY_TOGGLE";
export const PAGE_STATUS_CHANGE = "PAGE_STATUS_CHANGE";
export interface IMainMenuVisibilityAction {
  type: typeof MAIN_MENU_VISIBILITY_CHANGE;
  isRightMainMenuOpened: boolean;
}
// call by the user with dispatch(changeRightMenuVisibility(true))
export function changeMainMenuVisibility(newMainMenuVisibility: boolean): LayoutActionTypes {
    // ici on retourne le message qui va contenir l'action
    // pas de traitement a faire ici
    return {
      type: MAIN_MENU_VISIBILITY_CHANGE,
      isRightMainMenuOpened: newMainMenuVisibility,
    };
}

export interface IPageChangeAction {
  type: typeof PAGE_STATUS_CHANGE;
  page: IPage;
}

export function pageChange(page: IPage): LayoutActionTypes { return { type: PAGE_STATUS_CHANGE, page }; }

export interface IMainMenuVisibilityToggleAction {
  type: typeof MAIN_MENU_VISIBILITY_TOGGLE;
}
// call by the user with dispatch(changeRightMenuVisibility(true))
export function toggleMainMenuVisibility(): LayoutActionTypes {
    // ici on retourne le message qui va contenir l'action
    // pas de traitement a faire ici
    return {
      type: MAIN_MENU_VISIBILITY_TOGGLE,
    };
  }



export type LayoutActionTypes = IMainMenuVisibilityAction | IMainMenuVisibilityToggleAction | IPageChangeAction;


/************************************************************************************/
/******************************* Categories *****************************************/
/************************************************************************************/

export const ADD_CATEGORY = "ADD_CATEGORY";
export const DELETE_CATEGORY = "DELETE_CATEGORY";
export const UPDATE_CATEGORY = "UPDATE_CATEGORY";
export const ADD_CATEGORIES = "ADD_CATEGORIES";

interface IAddCategoryAction {
  type: typeof ADD_CATEGORY;
  category: ICategory;
}
export const genAddCategoryAction = (category: ICategory): IAddCategoryAction => ({ type: ADD_CATEGORY, category });

interface IAddCategoriesAction {
  type: typeof ADD_CATEGORIES;
  categories: ICategory[];
}
export const genAddCategoriesAction = (categories: ICategory[]):
    IAddCategoriesAction => ({ type: ADD_CATEGORIES, categories });

interface IDeleteCategoryAction {
  type: typeof DELETE_CATEGORY;
  categoryId: string;
}
export const genDeleteCategoryAction = (categoryId: string): IDeleteCategoryAction =>
  ({ type: DELETE_CATEGORY, categoryId });

interface IUpdateCategoryAction {
  type: typeof UPDATE_CATEGORY;
  updatedCategory: ICategory;
}
export const genUpdateCategoryAction = (updatedCategory: ICategory): IUpdateCategoryAction =>
  ({ type: UPDATE_CATEGORY, updatedCategory });


export type CategoriesActionTypes = IAddCategoriesAction | IAddCategoryAction
                              | IDeleteCategoryAction | IUpdateCategoryAction;


/************************************************************************************/
/******************************* Ideas **********************************************/
/************************************************************************************/


export const ADD_IDEA = "ADD_IDEA";
export const DELETE_IDEA = "DELETE_IDEA";
export const UPDATE_IDEA = "UPDATE_IDEA";
export const ADD_IDEAS = "ADD_IDEAS";

interface IAddIdeaAction {
  type: typeof ADD_IDEA;
  idea: IIdea;
}
export const genAddIdeaAction = (idea: IIdea): IAddIdeaAction => ({ type: ADD_IDEA, idea });

interface IAddIdeasAction {
  type: typeof ADD_IDEAS;
  ideas: IIdea[];
}
export const genAddIdeasAction = (ideas: IIdea[]):
    IAddIdeasAction => ({ type: ADD_IDEAS, ideas });

interface IDeleteIdeaAction {
  type: typeof DELETE_IDEA;
  ideaId: string;
}
export const genDeleteIdeaAction = (ideaId: string): IDeleteIdeaAction =>
  ({ type: DELETE_IDEA, ideaId });

interface IUpdateIdeaAction {
  type: typeof UPDATE_IDEA;
  updatedIdea: IIdea;
}
export const genUpdateIdeaAction = (updatedIdea: IIdea): IUpdateIdeaAction =>
  ({ type: UPDATE_IDEA, updatedIdea });

export type IdeasActionTypes = IAddIdeasAction | IAddIdeaAction
                              | IDeleteIdeaAction | IUpdateIdeaAction;


/************************************************************************************/
/******************************* Friends *****************************************/
/************************************************************************************/

export const ADD_FRIEND = "ADD_FRIEND";
export const DELETE_FRIEND = "DELETE_FRIEND";
export const UPDATE_FRIEND = "UPDATE_FRIEND";
export const ADD_FRIENDS = "ADD_FRIENDS";
export const LOADING_IDEAS = "LOADING_IDEAS";
export const FRIEND_IDEAS_LOADED = "FRIEND_IDEAS_LOADED";

interface IAddFriendAction {
  type: typeof ADD_FRIEND;
  friend: IFriend;
}
export const genAddFriendAction = (friend: IFriend): IAddFriendAction => ({ type: ADD_FRIEND, friend });

interface IAddFriendsAction {
  type: typeof ADD_FRIENDS;
  friends: IFriend[];
}
export const genAddFriendsAction = (friends: IFriend[]):
    IAddFriendsAction => ({ type: ADD_FRIENDS, friends });

interface IDeleteFriendAction {
  type: typeof DELETE_FRIEND;
  encodedEmail: string;
}
export const genDeleteFriendAction = (encodedEmail: string): IDeleteFriendAction =>
  ({ type: DELETE_FRIEND, encodedEmail });

interface IUpdateFriendAction {
  type: typeof UPDATE_FRIEND;
  updatedFriend: IFriend;
}
export const genUpdateFriendAction = (updatedFriend: IFriend): IUpdateFriendAction =>
  ({ type: UPDATE_FRIEND, updatedFriend });

interface ILoadingFriendIdeasAction {
  type: typeof LOADING_IDEAS;
  friend: IFriend;
}
export const loadingIdeasOf = (friend: IFriend): ILoadingFriendIdeasAction =>
  ( {type: LOADING_IDEAS, friend} );

interface IFriendIdeasLoadedAction {
  type: typeof FRIEND_IDEAS_LOADED;
  friend: IFriend;
  friendIdeas: Array<IFriendIdea|undefined>;
}
export const ideasOfFriendLoaded = (friend: IFriend, friendIdeas: Array<IFriendIdea|undefined>): IFriendIdeasLoadedAction =>
  ( {type: FRIEND_IDEAS_LOADED, friend, friendIdeas} );


