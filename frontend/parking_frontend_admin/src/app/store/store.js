import { configureStore } from "@reduxjs/toolkit";
import parkingSpaceReducer from "../../features/parkingspace/slices/ParkingSpaceSlice";

export const store = configureStore({
    reducer: {
        parkingSpace: parkingSpaceReducer,
    },
})