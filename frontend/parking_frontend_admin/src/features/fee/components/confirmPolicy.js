import Swal from 'sweetalert2';
import {changeFeePolicyHook} from './../hooks/usePolicyMutation'
export const confirmAlert=async({title,label,value,effectiveDate,resultTitle,mutateFn,updatePolicy})=>{

    const result=await Swal.fire({
        title: `${title}`,
        html:`
            <div>
                <p><strong>${label}: </strong>${value}</p>
                <p><strong>적용시점: </strong>${effectiveDate}</p>
            </div>
        `,
        icon:'question',

        showCancelButton:true,
        showConfirmButton:true,
        confirmButtonColor:'#3085d6',
        cancelButtonColor: '#aaa',
        confirmButtonText: '예약 저장',
        cancelButtonText: '취소',
        reverseButtons: true,
        
        background: '#1e1e1e',
        color: '#ffffff',
        backdrop:'rgba(0,0,0,0.6)'
    })

    if(result.isConfirmed){
        const res=await changeFeePolicyHook(updatePolicy)
        //if문달기
        await Swal.fire({
            title:`${resultTitle}`,
            icon:'success',
            timer:1500,
            showConfirmButton:false,
            background: '#1e1e1e',
            color: '#ffffff',
            backdrop:'rgba(0,0,0,0.6)'
        })
        return true;
    }
    return false;
}
