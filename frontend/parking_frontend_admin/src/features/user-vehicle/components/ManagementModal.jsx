import '../pages/management.css';

export default function ManagementModal({ title, onClose, children }) {
  return (
    <div className="mgmt-modal-overlay" onClick={onClose}>
      <div className="mgmt-modal" onClick={(e) => e.stopPropagation()}>
        <div className="mgmt-modal__header">
          <span className="mgmt-modal__title">{title}</span>
          <button className="mgmt-modal__close" onClick={onClose}>×</button>
        </div>
        {children}
      </div>
    </div>
  );
}
