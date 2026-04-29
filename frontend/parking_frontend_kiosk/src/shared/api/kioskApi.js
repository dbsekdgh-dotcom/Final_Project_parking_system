import axios from "axios";

const kioskApi = axios.create({
    baseURL: '',
    timeout: 30000,
});

export default kioskApi;