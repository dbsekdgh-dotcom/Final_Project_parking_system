import { useMutation, useQueryClient } from "@tanstack/react-query"
import {changeFeePolicy} from './../api/feeApi'

export const usePolicyMutation=()=>{
    const queryClient=useQueryClient();

    const useChangePolicy=useMutation({
        mutationFn: changeFeePolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("정책 수정 중 오류 발생==>",error.message)
        }
    })
    //await mutateAsync(data)
    return {mutateAsync:useChangePolicy.mutateAsync}
}