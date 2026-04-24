export default function VehicleSelector({ vehicles, selectedId, onChange }) {
    return (
        <div className="sub-modal-section">
            <label className="sub-modal-label">차량 선택</label>
            <select
                className="sub-modal-select"
                value={selectedId ?? ''}
                onChange={e => onChange(Number(e.target.value))}
            >
                {vehicles.map(v => (
                    <option key={v.vehicleId} value={v.vehicleId}>
                        {v.carNumber} ({v.vehicleName})
                    </option>
                ))}
            </select>
        </div>
    );
}
