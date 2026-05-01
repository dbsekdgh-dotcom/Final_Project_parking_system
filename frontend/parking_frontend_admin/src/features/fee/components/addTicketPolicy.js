import Swal from "sweetalert2"
import './updatePolicy.css'
import {confirmAlert} from './confirmPolicy'
import './addTicketPolicy.css'

export const addTicketPolicy=async({updateMutateAsync})=>{

        const result=await Swal.fire({
            title: "할인권 정책 등록",
            width: '500px',
            html:`
            <div class="policy-form-container">
                <div class="policy-field">
                    <div class="field-label">할인권명</div>
                    <input type="text" id='name' placeholder="예: [상가] 1시간 할인권">
                </div>
                
                <div class="policy-field">
                    <div class="field-label">내용</div>
                    <textarea id='description' placeholder="할인권 설명을 입력하세요"></textarea>
                </div>

                <div class="policy-field">
                    <div class="field-label">판매가격(원)</div>
                    <input type="number" id='price' placeholder="0">
                </div>

                <div class="policy-field">
                    <div class="field-label">할인타입</div>
                    <select id='discountType' class='swal-select'>
                        <option value="TIME">시간(분)</option>
                        <option value="AMOUNT">금액(원)</option>
                        <option value="RATE">비율(%)</option>
                        <option value="FREE">전액무료</option>
                    </select>
                </div>

                <div class="policy-field">
                    <div class="field-label">할인값</div>
                    <input type="number" id='discountValue' placeholder="20">
                </div>

                <div class="policy-field">
                    <div class="field-label">사용자</div>
                    <select id='useType' class='swal-select'>
                        <option value="STORE">상가</option>
                        <option value="ADMIN">관리자</option>
                    </select>
                </div>

                <div class="policy-field">
                    <div class="field-label">유효기간(일)</div>
                    <input type="number" id='validDays' placeholder="30">
                </div>

                <div class="policy-field">
                    <div class="field-label">유효기간(분)</div>
                    <input type="number" id='validMinutes' placeholder="0">
                </div>

                <div class="policy-field">
                    <div class="group-label">중복사용 가능여부</div>
                    <div class="radio-options">
                        <label class="radio-label"><input type="radio" name="stackable" value="true" checked> 가능</label>
                        <label class="radio-label"><input type="radio" name="stackable" value="false"> 불가능</label>
                    </div>
                </div>

                <div class="policy-field" style="margin-top:10px">
                    <div class="group-label" style="margin-bottom:0">상가 무료지급권 여부</div>
                    <div class="radio-options" style="margin-top:0;padding:6px 12px">
                        <label class="radio-label"><input type="radio" name="isFreeTicket" value="false" checked> 유료 판매권</label>
                        <label class="radio-label"><input type="radio" name="isFreeTicket" value="true"> 무료 지급권</label>
                    </div>
                </div>
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
                    name:document.getElementById('name').value,
                    description:document.getElementById('description').value,
                    price:Number(document.getElementById('price').value),
                    discountType:document.getElementById('discountType').value,
                    discountValue:Number(document.getElementById('discountValue').value),
                    validMinutes:Number(document.getElementById('validMinutes').value),
                    validDays:Number(document.getElementById('validDays').value),
                    stackable:document.querySelector('input[name="stackable"]:checked').value==='true',
                    useType:document.getElementById('useType').value,
                    isFreeTicket:document.querySelector('input[name="isFreeTicket"]:checked').value==='true'
                }
                
                
                // 유효성 체크
                if(!data.name){
                     Swal.showValidationMessage("할인권명을 기재해주세요.");
                     return false;
                }
                if(!data.discountType){
                    Swal.showValidationMessage("할인타입을 지정해주세요.");
                    return false;
                }
                if(data.discountType!='FREE' && !data.discountValue){
                    Swal.showValidationMessage("할인값을 입력해주세요.");
                    return false;    
                }
                if(!data.useType){
                    Swal.showValidationMessage("할인권 사용자를 지정해주세요.")
                    return false
                }

                return data;
           
            }
        })
        if(result.isConfirmed){
            const type=result.value.discountType;
            const unit=type==='TIME'?'분':type==='AMOUNT'?'원':type ==='RATE'?'%':'무료';
            await confirmAlert({
                title:"할인권 등록 확인",
                label:"할인권 정보",
                value: `${result.value.name} (${result.value.discountValue}${unit})`,
                effectiveDate:"즉시 적용",
                resultTitle:"할인권 등록 완료",
                mutateAsync: updateMutateAsync,
                updatePolicy:result.value
            })
        }

    
}