import { configureStore } from "@reduxjs/toolkit";
import parkingSpaceReducer from "../../features/parkingspace/slices/parkingSpaceSlice";

export const store = configureStore({
    reducer: {
        parkingSpace: parkingSpaceReducer,
    },
})