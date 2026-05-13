import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { fetchAdminList } from '../../api/adminApi'
import { getOrCreateDirectRoom, createGroupRoom } from '../../api/chatApi'
import './AdminListModal.css'

export default function AdminListModal({ onClose, position, excludeRef, myAdminId, onOpenChat }) {
    const [admins, setAdmins] = useState([])
    const [loading, setLoading] = useState(true)
    const [groupMode, setGroupMode] = useState(false)
    const [selectedIds, setSelectedIds] = useState([])
    const [groupName, setGroupName] = useState('')
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

    const handleDirectChat = async (admin) => {
        try{
            const res = await getOrCreateDirectRoom(admin.adminId)
            onOpenChat({ roomId: res.data.roomId, roomName: res.data.roomName, members: res.data.members })
            onClose()
        }catch(e){
            console.error(e)
        }
    }

    const handleCreateGroup = async () =>{
        if (!groupName.trim() || selectedIds.length < 1) return
        try{
            const res = await createGroupRoom(groupName.trim(), selectedIds)
            onOpenChat({ roomId: res.data.roomId, roomName: res.data.roomName, members: res.data.members })
            onClose()
        } catch (e){
            console.error(e)
        }
    }

    const toggleSelect = (adminId) =>{
        setSelectedIds(prev =>
            prev.includes(adminId) ? prev.filter(id => id !== adminId) : [...prev,adminId]
        )
    }

    const listToShow = groupMode
        ? admins.filter(a => a.adminId !== myAdminId)
        : admins

    return createPortal(
          <div
              ref={popoverRef}
              className="admin-popover"
              style={{ left: position.left, bottom: position.bottom }}
          >
              <div className="admin-popover__header">
                  <span>관리자 목록</span>
                  <div style={{ display: 'flex', gap: 6 }}>
                      <button
                          className={`admin-popover__group-btn${groupMode ? ' active' : ''}`}
                          onClick={() => { setGroupMode(p => !p); setSelectedIds([]); setGroupName('') }}
                      >
                          그룹
                      </button>
                      <button className="admin-popover__close" onClick={onClose}>✕</button>
                  </div>
              </div>

              <div className="admin-popover__body">
                  {loading ? (
                      <p className="admin-popover__loading">불러오는 중...</p>
                  ) : listToShow.length === 0 ? (
                      <p className="admin-popover__empty">다른 관리자가 없습니다.</p>
                  ) : (
                      listToShow.map(admin => (
                          <div key={admin.adminId} className="admin-popover__item">
                              {groupMode && (
                                  <input
                                      type="checkbox"
                                      className="admin-popover__checkbox"
                                      checked={selectedIds.includes(admin.adminId)}
                                      onChange={() => toggleSelect(admin.adminId)}
                                  />
                              )}
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
                              {!groupMode && (
                                  <button
                                      className="admin-popover__chat-btn"
                                      onClick={() => handleDirectChat(admin)}
                                      title="1:1 채팅"
                                  >
                                      💬
                                  </button>
                              )}
                          </div>
                      ))
                  )}
              </div>

              {groupMode && (
                  <div className="admin-popover__group-footer">
                      <input
                          className="admin-popover__group-name"
                          placeholder="그룹 채팅방 이름"
                          value={groupName}
                          onChange={e => setGroupName(e.target.value)}
                      />
                      <button
                          className="admin-popover__group-create"
                          onClick={handleCreateGroup}
                          disabled={!groupName.trim() || selectedIds.length < 1}
                      >
                          만들기 ({selectedIds.length + 1}명)
                      </button>
                  </div>
              )}
          </div>,
          document.body
      )
}
