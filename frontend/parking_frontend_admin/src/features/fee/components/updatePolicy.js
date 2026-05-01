import Swal from "sweetalert2"
import './updatePolicy.css'
import {confirmAlert} from './confirmPolicy'

export const updatePolicy=async({title,type,updateMutateAsync})=>{
        const now=new Date();
        //utc 시차 보정
        const adjustDate=new Date(now.getTime()-now.getTimezoneOffset()*60000)
        const minDate=new Date(adjustDate)
        minDate.setDate(minDate.getDate()+1)
        minDate.setHours(0,0,0,0)
        const minDateString=minDate.toISOString().split('T')[0]+ " 00:00:00";

        const result=await Swal.fire({
            title: `${title}`,
            html:`
                <div class="policy-form-container">
                    <div class="policy-field"><label>회차시간(분) <input type="number" id='graceMinutes' placeholder="예: 30"></label></div>
                    <div class="policy-field"><label>기본요금(원) <input type="number" id='baseFee' placeholder="예: 1000"></label></div>
                    <div class="policy-field"><label>추가 단위시간(분) <input type="number" id='unitMinutes' placeholder="예: 10"></label></div>
                    <div class="policy-field"><label>추가 단위요금(원) <input type="number" id='unitFee' placeholder="예: 500"></label></div>
                    <div class="policy-field"><label>일 최대요금(원) <input type="number" id='daliyMaxFee' placeholder="예: 20000"></label></div>
                    <div class="policy-field"><label>적용 시작일시 <input type="datetime-local" id='effectiveFrom' min="${minDateString}"></label></div>
                </div>
            `,
            focusCancel:true,
            showCancelButton:true,
            showConfirmButton:true,
            confirmButtonColor:'#3085d6',
            cancelButtonColor: '#aaa',
            confirmButtonText: '등록',
            cancelButtonText: '취소',
            background: getComputedStyle(document.documentElement).getPropertyValue('--bg-card').trim() || '#1e1e1e',
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary').trim() || '#ffffff',
            backdrop:'rgba(0,0,0,0.6)',
            customClass:{
                popup: 'custom-policy-popup'
            },
            preConfirm:()=>{
                const data={
                    parkingType:type,
                    graceMinutes:Number(document.getElementById('graceMinutes').value),
                    baseFee:Number(document.getElementById('baseFee').value),
                    unitMinutes:Number(document.getElementById('unitMinutes').value),
                    unitFee:Number(document.getElementById('unitFee').value),
                    daliyMaxFee:Number(document.getElementById('daliyMaxFee').value),
                    effectiveFrom:document.getElementById('effectiveFrom').value+":00"
                }
                
                // 유효성 체크
                if(Object.values(data).some(v=>v===""|| (typeof v==='number' && isNaN(v)))){
                     Swal.showValidationMessage("모든 항목을 정확하게 기재해주세요.");
                     return false;
                }
                if(Object.values(data).some(v=> typeof v==='number' && v <= 0)){
                    Swal.showValidationMessage("단위/요금 항목은 0보다 커야합니다.");
                    return false;
                }
                if(!data.effectiveFrom){
                    Swal.showValidationMessage("적용 시점은 내일 자정부터 설정 가능합니다.")
                    return false
                }

                return data;
           
            }
        })
        if(result.isConfirmed){
            await confirmAlert({
                title:"정책 예약 확인",
                label:"기본요금/단위요금",
                value: `${result.value.baseFee}원/${result.value.unitFee}`,
                effectiveDate:`${result.value.effectiveFrom.replace('T',' ')}`,
                resultTitle:"정책 예약 완료",
                mutateAsync: updateMutateAsync,
                updatePolicy:result.value
            })
        }

    
}