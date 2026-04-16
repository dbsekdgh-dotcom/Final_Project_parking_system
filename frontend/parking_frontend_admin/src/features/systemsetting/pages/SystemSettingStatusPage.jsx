import { useState, useEffect } from "react";
import SettingCard from "../components/SettingCard";
import { fetchSystemSettingsStatus, updateSystemSettingStatus } from "../api/systemSettingApi";
import "./SystemSettingStatusPage.css";



export default function SystemSettingStatusPage() {
  const [settings, setSettings] = useState([]);
  const [saving, setSaving] = useState(null); // key of item being saved

  useEffect(()=>{
    fetchSystemSettingsStatus()
    .then(res => setSettings(res.data))
    .catch(err => console.error(err));
  },[])

  const handleSave = async (settingKey, settingValue) => {
    setSaving(settingKey);
    try {
      await updateSystemSettingStatus(settingKey,settingValue);
      setSettings(prev =>
        prev.map(s => s.settingKey === settingKey ? {...s,settingValue} :s)
      );
    } catch (err) {
      console.error("설정 저장 실패:", err);
    } finally {
      setSaving(null);
    }
  };

  return (
    <div className="system-setting-page">
      <div className="system-setting-page__title-row">
        <span className="system-setting-page__title-icon">🔧</span>
        <span className="system-setting-page__title">상태 값 변경</span>
      </div>

      <div className="system-setting-page__grid">
        {settings.map(({ settingKey, settingValue, description }) => (
          <SettingCard
            key={settingKey}
            label={description}
            fieldKey={settingKey}
            value={settingValue}
            onSave={handleSave}
            isSaving={saving === settingKey}
          />
        ))}
      </div>
    </div>
  );
}
