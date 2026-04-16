import { useState } from "react";
import "./SettingCard.css";

export default function SettingCard({ label, fieldKey, value, onSave }) {
  const [editing, setEditing] = useState(false);
  const [inputVal, setInputVal] = useState(value);

  const handleEdit = () => {
    setInputVal(value);
    setEditing(true);
  };

  const handleCancel = () => {
    setInputVal(value);
    setEditing(false);
  };

  const handleSave = () => {
    onSave?.(fieldKey, Number(inputVal));
    setEditing(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") handleSave();
    if (e.key === "Escape") handleCancel();
  };

  return (
    <div className={`setting-card ${editing ? "setting-card--editing" : ""}`}>
      <div className="setting-card__header">
        <span className="setting-card__label">{label}</span>
        {editing ? (
          <button
            className="setting-card__icon-btn setting-card__icon-btn--close"
            onClick={handleCancel}
            aria-label="취소"
          >
            ✕
          </button>
        ) : (
          <button
            className="setting-card__icon-btn"
            onClick={handleEdit}
            aria-label="수정"
          >
            <EditIcon />
          </button>
        )}
      </div>

      <div className="setting-card__key">{fieldKey}</div>

      {editing ? (
        <div className="setting-card__edit-row">
          <input
            className="setting-card__input"
            type="number"
            value={inputVal}
            onChange={(e) => setInputVal(e.target.value)}
            onKeyDown={handleKeyDown}
            autoFocus
          />
          <button className="setting-card__save-btn" onClick={handleSave}>
            <span className="setting-card__save-icon">💾</span> 저장
          </button>
        </div>
      ) : (
        <div className="setting-card__value">{value}</div>
      )}
    </div>
  );
}

function EditIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M11.333 2a1.886 1.886 0 0 1 2.667 2.667L4.833 13.833l-3.5.667.667-3.5L11.333 2Z"
        stroke="currentColor"
        strokeWidth="1.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}
