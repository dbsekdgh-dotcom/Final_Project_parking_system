import { useMutation, useQueryClient } from "@tanstack/react-query"
import {changeFeePolicy} from './../api/feeApi'

export const usePolicy=()=>{
    const queryClient=useQueryClient();

    const changeFeePolicyHook=useMutation({
        mutationFn:async(payload)=>{
            const response=await changeFeePolicy(payload)
            return response
        },
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            return error.message;
        }
    })
}