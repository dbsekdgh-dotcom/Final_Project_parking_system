import { useEffect, useRef, useState } from 'react'
import { fetchAdminList } from '../../api/adminApi'
import './AdminListModal.css'

export default function AdminListModal({ onClose, position, excludeRef }) {
    const [admins, setAdmins] = useState([])
    const [loading, setLoading] = useState(true)
    const popoverRef = useRef(null)

    useEffect(() => {
        fetchAdminList()
            .then(res => setAdmins(res.data))
            .catch(err => console.error(err))
            .finally(() => setLoading(false))
    }, [])

    useEffect(() => {
        const handler = (e) => {
            if (
                popoverRef.current && !popoverRef.current.contains(e.target) &&
                excludeRef.current && !excludeRef.current.contains(e.target)
            ) {
                onClose()
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [onClose, excludeRef])

    return (
        <div
            ref={popoverRef}
            className="admin-popover"
            style={{ left: position.left, bottom: position.bottom }}
        >
            <div className="admin-popover__header">
                <span>관리자 목록</span>
                <button className="admin-popover__close" onClick={onClose}>✕</button>
            </div>

            <div className="admin-popover__body">
                {loading ? (
                    <p className="admin-popover__loading">불러오는 중...</p>
                ) : admins.length === 0 ? (
                    <p className="admin-popover__empty">등록된 관리자가 없습니다.</p>
                ) : (
                    admins.map(admin => (
                        <div key={admin.adminId} className="admin-popover__item">
                            <div className="admin-popover__avatar">
                                {admin.name.charAt(0)}
                            </div>
                            <div className="admin-popover__info">
                                <span className="admin-popover__name">{admin.name}</span>
                                <span className="admin-popover__id">{admin.loginId}</span>
                            </div>
                            {admin.loggedIn && (
                                <span className="admin-popover__badge">활동중</span>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}
