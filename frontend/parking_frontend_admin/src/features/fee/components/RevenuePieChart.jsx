import React from 'react'
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

const RevenuePieChart = ({data}) => {
    const chartData =[
        {name:'카드', value:data.card},
        {name:'포인트', value:data.point},
        {name:'할인권', value:data.ticket}
    ]
    const colors = ['#2563eb', '#f97316', '#10b981'];

  return (
    <ResponsiveContainer width="100%" height={350}>
        <PieChart>
            <Pie
                data={chartData}
                dataKey="value"
                stroke="none"
            >
                {chartData.map((_, index) => (
                    <Cell key={index} fill={colors[index]} />
                ))}
            </Pie>
            <Tooltip formatter={(val) => `${val.toLocaleString()}원`}/>
            <Legend />
        </PieChart>
    </ResponsiveContainer>
  )
}

export default RevenuePieChart;