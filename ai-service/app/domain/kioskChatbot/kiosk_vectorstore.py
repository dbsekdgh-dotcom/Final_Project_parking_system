import os
from dotenv import load_dotenv
from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma
import re

try:
    CURRENT_DIR=os.path.dirname(os.path.abspath(__file__))
except NameError:
    CURRENT_DIR=os.getcwd()
    
BASE_DIR=os.path.abspath(os.path.join(CURRENT_DIR,"..","..",".."))

# local용 env파일
LOCAL_ENV_FILE =os.path.abspath(os.path.join(BASE_DIR, "..", ".env.local"))
# 배포용 env파일
SERVER_ENV_PATH = os.path.abspath(os.path.join(BASE_DIR, "..", ".env"))

def load_env():
    if os.getenv("OPENAI_API_KEY"):
        return
    if os.path.exists(LOCAL_ENV_FILE):
        load_dotenv(dotenv_path=LOCAL_ENV_FILE,override=True)
        return
    if os.path.exists(SERVER_ENV_PATH):
        load_dotenv(dotenv_path=SERVER_ENV_PATH,override=True)
        return
load_env()

KNOWLEDGE_PATH=os.path.join(BASE_DIR,"knowledge")
PDF_PATH = os.path.join(KNOWLEDGE_PATH, "주차장 키오스크 사용 매뉴얼.pdf")
STORAGE_PATH=os.path.join(BASE_DIR,"storage")
CHROMA_PATH = os.path.join(STORAGE_PATH, "kiosk_db")
COLLECTION_NAME = "kiosk_manual"

def get_embedding():
    return OpenAIEmbeddings(model="text-embedding-3-small")

# pdf에서 추출된 텍스트의 줄바꿈, 공백 정리
def clean_text(text):
    # 줄바꿈, 탭, 여러 공백을 하나의 공백으로 변경
    text=re.sub(r"\s+"," ",text)
    # 문장부호 앞의 불필요한 공백 제거
    text=re.sub(r"\s+([.,!?])",r"\1",text)
    # 문자열의 맨 앞과 맨 뒤에 있는 공백 제거
    return text.strip()

# pdf 파일 읽어서 백터화 및 db저장
def load_pdf():
    # pdf 파일 읽어오기
    if not os.path.exists(PDF_PATH):
        print(f"매뉴얼을 찾을 수 없습니다. {PDF_PATH}")
        return None
    
    # storage 폴더 생성
    os.makedirs(CHROMA_PATH,exist_ok=True)
    
    # mode="page"를 지정하면 페이지 단위 Document로 로드됨
    loader=PyPDFLoader(PDF_PATH, mode="page")
    pages=loader.load()
    for p in pages:
        p.page_content=clean_text(p.page_content)
    
    # 청크 단위로 쪼개기
    splitter=RecursiveCharacterTextSplitter(
        chunk_size=500,
        chunk_overlap=100
    )
    chunks=splitter.split_documents(pages)
    
    # 임베딩 및 db 저장
    embedding=get_embedding()
    vectorscore=Chroma.from_documents(
        documents=chunks,
        embedding=embedding,
        collection_name=COLLECTION_NAME,
        persist_directory=CHROMA_PATH
    )
    return {
        "message": "임베딩 및 DB 저장 완료",
        "pdf_path": PDF_PATH,
        "storage_path": CHROMA_PATH,
        "page_count": len(pages),
        "chunk_count": len(chunks)
    }
    

# db를 다시 불러오는 함수 
def get_vectorstore():
    return Chroma(
        collection_name=COLLECTION_NAME,
        # 사용자의 질문 임베딩헤사 db안의 벡터들과 비교
        embedding_function=get_embedding(),
        persist_directory=CHROMA_PATH
    )
    
# 질문과 관련된 pdf 내용을 검색
def get_retriever():
    vectorstore=get_vectorstore()
    return vectorstore.as_retriever(
        search_kwargs={"k":4}
    )
    
if __name__=="__main__":
    result=load_pdf()
    print(result)
    