const fmt = (d) => new Date(d).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });

export default function SubscriptionPeriodInfo({ days, startDate }) {
    const start = startDate ? new Date(startDate) : new Date();
    const end = new Date(start);
    end.setDate(end.getDate() + (days ?? 30));

    return (
        <div className="sub-modal-section">
            <label className="sub-modal-label">구독 기간</label>
            <div className="sub-modal-period">
                {fmt(start)} ~ {fmt(end)} ({days}일)
            </div>
        </div>
    );
}
