
import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import counterReducer from '../features/counter/counterSlice';

// chercher comment activer le redux devtools     (window as any).__REDUX_DEVTOOLS_EXTENSION__ &&  (window as any).__REDUX_DEVTOOLS_EXTENSION__(), // pour le debug redux

export const store = configureStore({
  reducer: {
    counter: counterReducer,
  },
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;

