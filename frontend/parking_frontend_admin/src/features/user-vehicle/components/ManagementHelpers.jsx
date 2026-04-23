export function Row({ label, value }) {
  return (
    <div className="mgmt-modal__row">
      <span className="mgmt-modal__label">{label}</span>
      <span className="mgmt-modal__value">{value ?? '-'}</span>
    </div>
  );
}

export function SearchIcon() {
  return (
    <svg className="arp__search-icon" width="14" height="14" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
    </svg>
  );
}

export function formatDate(isoStr) {
  if (!isoStr) return '-';
  const d = new Date(isoStr);
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`;
}

export function formatDateTime(isoStr) {
  if (!isoStr) return '-';
  const d = new Date(isoStr);
  return `${formatDate(isoStr)} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}
