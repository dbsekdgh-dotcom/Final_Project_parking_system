import redis;
import os;
from fastapi import HTTPException

# 1. Redis 연결 (도커로 띄운 Redis 서버와 통신)
# decode_responses=True: 데이터를 가져올 때 바이트가 아닌 '문자열'로 받기 위해 필수
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")                                                                                                                                                                                                                                                  
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))          
                                                                                                                                                                                                                                        
redis_client=redis.Redis(host=REDIS_HOST,port=REDIS_PORT,db=0,decode_responses=True) 
class Lock_service:
    @staticmethod
    def aquire_lock(car_number: str):
        """
            [중복 요청 방지]
            - 차량 번호를 '키(Key)'로 사용
            - nx=True: Redis에 해당 키가 없을 때만 저장 (이미 있으면 실패)
            - ex=15: 15초 뒤에 자동으로 삭제 (락이 무한히 유지되는 것 방지)
        """
        lock_key=f"payment:lock:{car_number}"
        is_success=redis_client.set(lock_key,"processing",nx=True,ex=180)
        
        if not is_success:
            raise HTTPException(
                status_code=409,
                detail="현재 해당 차량의 정산이 진행 중입니다."
            )
        return True
    
    @staticmethod
    def lock_release(car_number:str):
        """
        [락 해제]
          - 결제 처리가 끝났다면(성공이든 실패든) 수동으로 락을 지워주기
        """
        lock_key=f"payment:lock:{car_number}"
        redis_client.delete(lock_key)
        