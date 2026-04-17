import axios from 'axios';

const BASE_URL = '/api';

export const loginAdmin = async (loginData) => {
    const params = new URLSearchParams();
    params.append('loginId',loginData.loginId);
    params.append('password',loginData.password);

    try {
        console.log("요청 주소 확인: ",`${BASE_URL}/admin/login`)

        //백엔드의 Spring Security가 기다리는 /admin/login으로 요청
        //loginData = {loginId:'..',password:'..'} / params = 'loginId=admin0000&password=11' 문자열
        const response = await axios.post(`${BASE_URL}/admin/login`, params,{
            headers: {
                //시큐리티가 좋아하는 Content-Type로 명시해줌
                'Content-Type':'application/x-www-form-urlencoded'
            },
            withCredentials: true
        });
        return response.data; // {accessToken,refreshToken,adminName ...} 반환
    }catch(error){
        throw error.response?.data || error.message;
    }
};