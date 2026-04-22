import axios from "axios";

const host = (import.meta.env.VITE_API_BASE_URL || '') + '/api/kiosk';

export const findCar = async (query) =>{
    const res = await axios.get(`${host}/find-car`,{ params:{query}});
    return res.data;
}