import axios from "axios"

export const host=(import.meta.env.VITE_API_BASE_URL || '')+'/api/prepays'

export const searchCar=async(searchKeyword)=>{
    const res=await axios.post(`${host}/search-car`,searchKeyword,{
        headers:{'Content-Type':'text/plain'}
    })
    console.log("res==>"+res.data);
    return res.data;
}