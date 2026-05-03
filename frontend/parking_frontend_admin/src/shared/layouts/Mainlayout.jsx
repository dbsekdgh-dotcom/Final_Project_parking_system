import React, { useCallback, useEffect, useState } from 'react'
import Header from '../components/header/Header'
import Sidebar from '../components/sidebar/Sidebar'
import './Mainlayout.css'
import { Outlet } from 'react-router-dom'
import { getApprovalPendingCount } from '../../features/approval/approval-request/api/approvalRequestApi'
import { getReportPendingCount } from '../../features/approval/report/api/reportApi'
import { PendingCountContext } from '../context/PendingCountContext'

const Mainlayout = () => {
  const [pendingApproval, setPendingApproval] = useState(0)
  const [pendingReport, setPendingReport]   = useState(0)

  const fetchCounts = useCallback(async () => {
    try {
      const [approval, report] = await Promise.all([
        getApprovalPendingCount(),
        getReportPendingCount(),
      ])
      setPendingApproval(approval)
      setPendingReport(report)
    } catch {
      // 실패 시 이전 값 유지
    }
  }, [])

  useEffect(() => {
    fetchCounts()
    const id = setInterval(fetchCounts, 30000)
    return () => clearInterval(id)
  }, [fetchCounts])

  return (
    <PendingCountContext.Provider value={fetchCounts}>
      <div className="layout">
        <Sidebar pendingApproval={pendingApproval} />
        <div className="layout__main">
          <Header pendingApproval={pendingApproval} pendingReport={pendingReport} />
          <main className="layout__content" aria-label="콘텐츠 영역">
            <Outlet />
          </main>
        </div>
      </div>
    </PendingCountContext.Provider>
  )
}

export default Mainlayout
