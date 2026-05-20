import { useCallback, useEffect, useRef, useState } from "react";
import useChatbotStore from "../../../store/useChatbotStore";
import { FaCommentDots, FaPaperPlane, FaTimes } from "react-icons/fa";
import { endChat, sendChat } from "../api/chatbotApi";
import './KioskChatbot.css'
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

const INITIAL_MESSAGE = {
    role: "bot",
    text: "키오스크 이용 중 궁금한 점을 물어보세요.",
};

const RECOMMEND_QUESTIONS = [
    "주차 요금 정책 알려줘",
    "사전정산 화면으로 가고 싶어",
    "내 차 찾기 화면으로 가고 싶어"
];

function KioskChatbot() {
    const navigate=useNavigate();
    const screenId = useChatbotStore((state) => state.screenId);
    const [open,setOpen]=useState(false)
    const [messages,setmessages]=useState([INITIAL_MESSAGE])
    const [input,setInput]=useState("")
    const sessionIdRef=useRef(null)
    const bottomRef = useRef(null);
    const [pos,setPos]=useState(()=>({
        x: Math.max(0, window.innerWidth / 2 - 460),
        y: 160,
    }))
    const isDragging=useRef(false)
    const dragOffset=useRef({x:0,y:0})

    const chatMutation= useMutation({
        mutationFn:({sessionId,userQuestion,screenId})=> sendChat(sessionId,userQuestion,screenId),
        onMutate:(variables)=>{
            // 질문 대화이력에 저장 
            setmessages((prev)=>[
                ...prev,
                {role:"user",text:variables.userQuestion},
            ])
            setInput("");
        },
        onSuccess: (res)=>{
            sessionIdRef.current=res.sessionId
            setmessages((prev)=>[
                ...prev,
                {role:"bot",text:res.answer || "답변을 받지 못했습니다."}
            ]);
            const action=res.action;
            const targetPath=res.target_path;

            if(action=="navigate" && targetPath){
                navigate(targetPath)
            }
        },
        onError:(error)=>{
            console.log("챗봇 요청 실패",error)
            setmessages((prev)=>[
                ...prev,
                {role:"bot", text:"죄송합니다. 잠시 후 다시 시도해주세요."}
            ])
        },
    })

    const sendHandler=()=>{
        const text=input
        if(!text || chatMutation.isPending) return

        // 챗봇에 질문
        chatMutation.mutate({
            sessionId: sessionIdRef.current,
            userQuestion: text,
            screenId,
        });
    }

    const recommendSendHandler=(recommend_question)=>{
        if(chatMutation.isPending) return;

        // 챗봇에 질문
        chatMutation.mutate({
            sessionId: sessionIdRef.current,
            userQuestion: recommend_question,
            screenId,
        });
    }

    const closeHandler=async()=>{
        const currentSessionId = sessionIdRef.current;
        setOpen(false)
        sessionIdRef.current=null;
        setmessages([INITIAL_MESSAGE]);
        setInput("")

        if(!currentSessionId) return
        try{
            await endChat(currentSessionId)
        }catch(error){
            console.log("챗봇 종료 요청 실패")
        }
    }

    useEffect(() => {
        if (open) {
            bottomRef.current?.scrollIntoView({ behavior: "smooth" });
        }
    }, [messages, open]);

    const keyDownHandler = (e) => {
        if (e.key === "Enter" ) {
            e.preventDefault();
            sendHandler();
        }
    };

     //드래그 시작 — 마우스가 창의 어느 지점을 잡았는지 offset 기록
    const dragStartHandler=(e)=>{
        isDragging.current=true;
        dragOffset.current={
            x:e.clientX -pos.x,
            y:e.clientY -pos.y
        }
    }
    //드래그 중 — 마우스 움직임에 따라 창 위치 계산 후 업데이트
    const dragMoveHandler=useCallback((e)=>{
        if(!isDragging.current) return

        const windowWidth=360;
        const windowHeight=480;
        // 마우스가 이동했을 때 챗봇 창의 새로운 왼쪽 위 위치를 계산
        const nextX=e.clientX-dragOffset.current.x;
        const nextY=e.clientY-dragOffset.current.y;
        // 챗봇 창이 화면 밖으로 나가지 않게 제한
        const maxX=window.innerWidth-windowWidth
        const maxY=window.innerHeight-windowHeight
        // 계산된 위치를 저장
        setPos({
            x: Math.max(0, Math.min(nextX, maxX)),
            y: Math.max(0, Math.min(nextY, maxY)),
        });
    },[])

    // 드래그 종료 — isDragging 플래그를 false로 리셋
    const dragEndHandler = useCallback(() => {
        isDragging.current = false;
    }, []);

    useEffect(()=>{
        document.addEventListener("mousemove",dragMoveHandler)
        document.addEventListener("mouseup",dragEndHandler)

        return ()=>{
            document.removeEventListener("mousemove",dragMoveHandler)
            document.removeEventListener("mouseup",dragEndHandler)
        }
    },[dragMoveHandler,dragEndHandler])

    return (
        <div >
            <button className="chatbot-toggle-btn" onClick={()=>setOpen(!open)}>챗봇<FaCommentDots /></button>
            {open &&
                <div  className="chatbot-window" style={{left:`${pos.x}px`, top:`${pos.y}px`}}>
                    <div className="chatbot-header" onMouseDown={dragStartHandler}>
                        <span className="chatbot-header-title">키오스크 사용 도우미</span>
                        <button className="chatbot-close-btn" onClick={closeHandler} onMouseDown={(e) => e.stopPropagation()}><FaTimes /></button>
                    </div>
                    <div className="chatbot-messages">
                        {messages.map((message, index) => (
                            <div key={index} className={`chatbot-bubble ${message.role}`}>
                                {message.text}
                            </div>
                        ))}
                        {/* 챗봇 답변 응답 대기 */}
                        {chatMutation.isPending && (
                            <div className="chatbot-bubble bot">
                                <TypingIndicator />
                            </div>
                        )}
                        {/* 초기 추천 질문 */}
                        {messages.length==1 && (
                            <div className="chatbot-recommend-wrapper">
                                {RECOMMEND_QUESTIONS.map((q,index)=>(
                                    <button key={index} className="chatbot-recommend-chip" onClick={()=>recommendSendHandler(q)} disabled={chatMutation.isPending}>
                                        {q}
                                    </button>
                                ))}
                            </div>
                        )}
                        <div ref={bottomRef} />
                    </div>
                    <div className="chatbot-input-row">
                        <textarea className="chatbot-input" value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={keyDownHandler} placeholder="질문을 입력하세요."/>
                        <button className="chatbot-send-btn" onClick={sendHandler} disabled={!input || chatMutation.isPending}><FaPaperPlane /></button>
                    </div>
                </div>
            }
        </div>
    );
}

export default KioskChatbot;

function TypingIndicator(){
    return (
        <div className="typing-indicator-container">
            <div className="typing-dot"></div>
            <div className="typing-dot"></div>
            <div className="typing-dot"></div>
        </div>
    );
}