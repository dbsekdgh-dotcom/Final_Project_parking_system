import axios from "axios";
import { useMutation } from "@tanstack/react-query";

export const createEntry =  () =>{
   return useMutation({
    mutationFn: async (file) =>{
        const formData=new FormData();
        formData.append("file",file);

        const res = await axios.post(
            "http://localhost:8000/api/v1/parking/entryexit",
            formData
        );
        return res.data;
     },
   });
};