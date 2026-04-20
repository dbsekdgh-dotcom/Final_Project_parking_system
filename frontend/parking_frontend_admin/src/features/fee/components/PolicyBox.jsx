import React, { useEffect, useState } from 'react'
import { SlPencil,SlShareAlt  } from "react-icons/sl";
import './feePolicy.css'
import {confirmAlert} from './confirmPolicy'
import {usePolicyMutation} from './../hooks/usePolicyMutation'
import { updatePolicy } from './updatePolicy';

const PolicyBox = ({title,data,isUpcoming, isLatest}) => {
    const [policy,setPolicy]=useState(data)
    const [editField,setEditField]=useState(null)
    const [tempData,setTempData]=useState("")
    const {mutateAsync,updateMutateAsync}=usePolicyMutation()

    const fields=[
        {label:"회차시간(분)", value:data?.graceMinutes, name:"graceMinutes"},
        {label:"기본요금(원)", value:data?.baseFee, name:"baseFee"},
        {label:"추가 단위시간(분)", value:data?.unitMinutes, name:"unitMinutes"},
        {label:"추가 단위요금(원)", value:data?.unitFee, name:"unitFee"},
        {label:"일 최대요금(원)", value:data?.daliyMaxFee, name:"daliyMaxFee"},
    ]

    useEffect(()=>{
        setPolicy(data)
    },[data])

    const changeAllHandler=()=>{
        updatePolicy({
            title:title,
            type:data?.parkingType,
            updateMutateAsync:updateMutateAsync
        })
    }

    const changeHandler=async(label,name)=>{
        if(tempData==""|| tempData==0){
            setEditField(null)
            setTempData("")
            return;
        }
        //적용 날짜 
        const now=new Date();
        const y=now.getFullYear();
        const m=String(now.getMonth()+1).padStart(2,'0')
        const d=String(now.getDate()+1).padStart(2,'0')
        const effectiveDate=`${y}-${m}-${d} 00:00:00`

        //적용할 데이터
        const value=tempData;
        const changeLabel=label //화면에 보여줄 데이터
        const changeName=name
        const updatePolicy={
            ...policy,[changeName]:value,["effectiveFrom"]:effectiveDate,["version"]:Number(data.version)+1
        }
        confirmAlert({
            title:"정책 변경예약 확인",
            label:changeLabel,
            value:value.toLocaleString(),
            effectiveDate:effectiveDate,
            resultTitle:"정책 변경 완료",
            mutateAsync:mutateAsync,
            updatePolicy:updatePolicy
        })
        setEditField(null)
        setTempData("")

    }

  return (
    <div className={`section ${ isUpcoming ? isUpcoming : ''}`}>
        <div className='sectionHeader'>
            <span className='title'>{title} <span className='version'>v.{data?.version}</span></span>
            <span className='mainEdit' role='button' onClick={changeAllHandler}><SlPencil></SlPencil></span> 
        </div>
        <div className='gridContainer'>
        {
            fields.map((f)=>
                <div className='infoBox' key={f.name}>
                    <span className='label'>{f.label}</span>
                    {
                        editField==f.label ?
                        <div className="editContainer" >
                            <input className="editInput" type="number" value={tempData} 
                                onChange={(e)=>setTempData(Number(e.target.value))}
                                onKeyDown={(e) => {if(e.key==="Enter"){
                                    e.preventDefault();
                                    changeHandler(f.label,f.name);
                                }}}
                                onBlur={()=>{setEditField(null); setTempData("")}}
                                autoFocus>
                            </input>
                            <span role='button' className='saveBtn' onMouseDown={()=>changeHandler(f.label,f.name)}>저장</span>
                        </div>
                        :<span className='value'>{Number(f.value).toLocaleString()}</span>
                    }
                    {
                        isLatest && <span className='editIcon' role='button' onClick={()=>setEditField(editField==f.label?null:f.label)}>{editField==f.label?<SlShareAlt />:<SlPencil/>}</span>
                    }
                </div>
            )
        }

        <div className='infoBox'>
            <span className='label'>적용 시작 일시</span>
            <span className='value'>{data?.effectiveFrom.split('T')[0]}</span>
        </div>
        </div>
    </div>
  )
}

export default PolicyBox;