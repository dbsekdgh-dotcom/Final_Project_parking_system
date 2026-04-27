import kioskApi from "../../../shared/api/KioskApi";


export const findCar = async (query) =>{
    const res = await kioskApi.get(`/api/kiosk/find-car`,{ params:{query}});
    return res.data;
}