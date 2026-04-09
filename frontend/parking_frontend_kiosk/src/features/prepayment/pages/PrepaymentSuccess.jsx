import React from 'react'
import SuccessView from '../../../shared/components/successView/SuccessView';
import { useLocation } from 'react-router-dom';


// title={} subTitle={}
const PrepaymentSuccess = () => {
    const location=useLocation()
    const {title,subTitle}=location.state || {}
    console.log("location=>",location)
    console.log("state=>",location.state)
  return (
    <div>
        <SuccessView title={title} subTitle={subTitle} />
    </div>
  )
}

export default PrepaymentSuccess;