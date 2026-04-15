import React, { useEffect, useState } from 'react'
import './fee.css'
import FeePolicy from '../components/FeePolicy'
import FeeHistory from '../components/FeeHistory'
import Stats from '../components/Stats'

const Fee = () => {
  const [activeTab,setActiveTab]=useState('policy')

  const tabs=[
    {id: 'policy', label: '요금 정책',icon:'🛍️'},
    {id: 'history', label: '과거 이력',icon:'📋'},
    {id: 'stats',label: '통계',icon: '📈'}
  ]
   useEffect(()=>{
    console.log(activeTab)
   },[])

  return (
    <div className='admin-container'>
      <div className='tabs-header'>
        {tabs.map((tab)=> {
          return (<button
            key={tab.id}
            className={`tab-btn ${activeTab==tab.id? 'active':''}`}
            onClick={()=>setActiveTab(tab.id)}
          >
            <span>{tab.icon}</span>{tab.label}
          </button>
      )})}
      </div>
      <div className='tabs-content'>
        {activeTab ==='policy' && <FeePolicy/>}
        {activeTab ==='history' && <FeeHistory/>}
        {activeTab === 'stats' && <Stats/>}
      </div>
    </div>
  )
}

export default Fee;