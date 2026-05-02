import { useMutation, useQueryClient } from "@tanstack/react-query"
import {changeFeePolicy} from './../api/feeApi'
import { updateFeePolicy,deleteTicketPolicy,insertTicketPolicy,inactivateTicketPolicy} from "./../api/feeApi";

export const usePolicyMutation=()=>{
    const queryClient=useQueryClient();

    // 기존 정책 수정, 자정부터 반영
    const useChangePolicy=useMutation({
        mutationFn: changeFeePolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("정책 수정 중 오류 발생==>",error.message)
        }
    });

    // 신규 정책 
    const useUpdateMutation=useMutation({
        mutationFn: updateFeePolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("정책 업데이트 중 오류 발생==>",error.message)
        }
    });

    const usedeleteTicketMutation=useMutation({
        mutationFn:deleteTicketPolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("할인권 삭제중 오류 발생==>",error.message)
        }
    })

    const useInsertTicketMutation=useMutation({
        mutationFn:insertTicketPolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("할인권 등록중 오류 발생==>",error.message)
        }
    })

    const useInactivateTicketMutation=useMutation({
        mutationFn:inactivateTicketPolicy,
        onSuccess:()=>{
            queryClient.invalidateQueries({queryKey:['feePolicy']})
        },
        onError:(error)=>{
            console.log("할인권 비활성화 중 오류 발생==>",error.message)
        }        
    })

    //await mutateAsync(data)
    return {
        mutateAsync:useChangePolicy.mutateAsync,
        updateMutateAsync:useUpdateMutation.mutateAsync,
        deleteTicketMutationAsync:usedeleteTicketMutation.mutateAsync,
        insertTicketMutationAsync:useInsertTicketMutation.mutateAsync,
        inactivateTicketMutationAsync:useInactivateTicketMutation.mutateAsync
    }
}