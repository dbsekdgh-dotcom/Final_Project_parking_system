import { useMutation } from "@tanstack/react-query";
import { requestPlateOcr } from "../api/PlateOcrApi";

export const usePlateOCRMutation = () =>{
    return useMutation({
        mutationFn: requestPlateOcr,
    });
};