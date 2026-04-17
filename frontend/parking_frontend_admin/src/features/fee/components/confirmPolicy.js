import Swal from 'sweetalert2';
export const confirmAlert=async({title,label,value,effectiveDate,resultTitle,mutateAsync,updatePolicy})=>{

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
        try{
            const res= await mutateAsync(updatePolicy)
            if(res.status==201){
                await Swal.fire({
                    title:`${resultTitle}`,
                    icon:'success',
                    timer:1500,
                    showConfirmButton:false,
                    background: '#1e1e1e',
                    color: '#ffffff',
                    backdrop:'rgba(0,0,0,0.6)'
                })
            }
        }catch(error){
            await Swal.fire({
                title:"정책 수정 실패",
                text:error.response?.data?.message ||  "정책 수정 중 오류가 발생하였습니다.",
                icon:'error',
                timer:1500,
                showConfirmButton:false,
                background: '#1e1e1e',
                color: '#ffffff',
                backdrop:'rgba(0,0,0,0.6)'
            })
        }
    }
}
