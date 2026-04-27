import React from 'react'
import { Bar, CartesianGrid, ComposedChart, Legend, Line, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

const RevenueChart = ({dailyList}) => {
  return (
    <div style={{ width: '100%', height: '400px' }}>
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={dailyList}>
          <CartesianGrid strokeDasharray="3 3"/>
          <XAxis dataKey="date"/>
          <YAxis/>
          {/* 말풍선 표시 (항목이름 : 값) */}
          <Tooltip separator=": "/> 
          <Legend />
          {/* 누적 막대- 수익 */}
          <Bar dataKey="parkingRevenue" name="주차 수익" stackId="revenue" fill="#8884d8"/>
          <Bar dataKey="subscriptionRevenue" name="정기권 수익" stackId="revenue" fill="#82ca9d"/>
          <Bar dataKey="ticketRevenue" name="할인권 수익" stackId="revenue" fill="#ffc658"/>
          {/* 꺽은선 -순이익 */}
          <Line type="monotone" dataKey="net" name="순이익" stroke="#ff7300" strokeWidth={2} dot={{ r: 4 }} />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  )
}

export default RevenueChart