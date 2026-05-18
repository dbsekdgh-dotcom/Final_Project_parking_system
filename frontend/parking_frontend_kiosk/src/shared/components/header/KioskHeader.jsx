import { useState, useEffect } from 'react'
import '../../styles/tokens.css'
import './KioskHeader.css'

export default function KioskHeader() {
    const [time, setTime] = useState('')
    const [date, setDate] = useState('')

    useEffect(() => {
        const update = () => {
            const now = new Date()
            const h = String(now.getHours()).padStart(2, '0')
            const m = String(now.getMinutes()).padStart(2, '0')
            setTime(`${h}:${m}`)
            setDate(now.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }))
        }
        update()
        const timer = setInterval(update, 1000)
        return () => clearInterval(timer)
    }, [])

    return (
        <header className="kiosk-header">
            <div className="brand-title">PARKING CENTER</div>
            <div className="time-display">
                <div className="clock">{time}</div>
                <div className="date">{date}</div>
            </div>
        </header>
    )
}
