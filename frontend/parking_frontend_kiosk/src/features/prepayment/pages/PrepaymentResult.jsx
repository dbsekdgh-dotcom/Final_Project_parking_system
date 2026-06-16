import React from 'react'
import ResultView from '../../../shared/components/resultView/ResultView';
import { useLocation } from 'react-router-dom';


// title={} subTitle={}
const PrepaymentResult = () => {
    const location=useLocation()
    const {title,subTitle,type,showBackButton}=location.state || {
        title: "알 수 없는 상태",
        subTitle: "정보를 불러올 수 없습니다.",
        type:"error"
    }
    console.log("location=>",location)
    console.log("state=>",location.state)
  return (
    <div>
        <ResultView title={title} subTitle={subTitle} type={type} showBackButton={showBackButton} />
    </div>
  )
}

export default PrepaymentResult;