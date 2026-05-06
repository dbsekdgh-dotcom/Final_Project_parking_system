import kioskApi from "./kioskApi";

export const getParkingSummary = () =>
    kioskApi.get('/api/kiosk/parking-summary')
