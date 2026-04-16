import adminApi from "../../../shared/api/adminApi";

export const fetchSystemSettingsStatus = () =>
    adminApi.get("/system-setting/status")

export const updateSystemSettingStatus = (settingKey,settingValue) =>
    adminApi.patch(`/system-setting/status/${settingKey}`,{settingValue});

