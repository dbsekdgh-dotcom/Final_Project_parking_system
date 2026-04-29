import axios from "axios";

const kioskApi = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 30000,
});

export default kioskApi;