import React from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import Swal from "sweetalert2";
import api from "../../auth/api/axios";
import { formatPhoneNumber, handlePasswordChange } from "../../auth/utils/accountUtils";
import { updateMyInfo } from "../api/mypageApi";
import "./SecuritySection.css";

const SecuritySection = ({ currentPhone, userName, userEmail }) => {
    const queryClient = useQueryClient();
    const email = userEmail || localStorage.getItem("userEmail");
    const name = userName || localStorage.getItem("userName");

    const updateMutation = useMutation({
        mutationFn: (phone) => updateMyInfo({ phone }),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["myInfo"] }),
    });

    const handlePhoneChange = async () => {
        // Step 0. 이메일 확인 후 전송 동의
        const { isConfirmed } = await Swal.fire({
            title: "전화번호 변경",
            html: `
                <p style="color:#444; margin-bottom:8px;">본인 확인을 위해 인증번호를 전송합니다.</p>
                <p style="font-size:0.95rem; color:#3085d6; font-weight:600; margin:0;">${email}</p>
                <p style="font-size:0.85rem; color:#888; margin-top:6px;">위 이메일로 인증번호가 전송됩니다.</p>
            `,
            showCancelButton: true,
            confirmButtonText: "인증번호 전송",
            cancelButtonText: "취소",
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#6c757d",
        });

        if (!isConfirmed) return;

        // Step 1. 인증번호 발송
        Swal.fire({
            title: "인증번호 발송 중...",
            allowOutsideClick: false,
            showConfirmButton: false,
            willOpen: () => Swal.showLoading(),
        });

        try {
            await api.post("/api/user/auth/local/send-code", {
                name,
                email,
                phone: currentPhone,
            });
        } catch (err) {
            Swal.close();
            Swal.fire({
                icon: "error",
                title: "발송 실패",
                text: err.response?.data?.message || "인증번호 발송에 실패했습니다.",
                confirmButtonColor: "#d33",
            });
            return;
        }

        // Step 2. 인증번호 입력 (타이머)
        let timerInterval;
        const { value: code } = await Swal.fire({
            title: "인증번호 입력",
            html: `
                <div style="margin-bottom:10px; color:#666;">
                    <b>${email}</b>로 발송된 6자리 번호를 입력하세요.
                </div>
                <input id="phone-code" class="swal2-input" placeholder="인증번호 6자리" maxlength="6"
                    style="width:60%; text-align:center;">
                <div id="phone-timer" style="color:#e74c3c; font-weight:bold; margin-top:10px;">남은 시간: 03:00</div>
            `,
            showCancelButton: true,
            confirmButtonText: "인증하기",
            cancelButtonText: "취소",
            confirmButtonColor: "#3085d6",
            didOpen: () => {
                let timeLeft = 180;
                timerInterval = setInterval(() => {
                    timeLeft -= 1;
                    const min = Math.floor(timeLeft / 60);
                    const sec = timeLeft % 60;
                    const el = document.getElementById("phone-timer");
                    if (el) el.textContent = `남은 시간: ${String(min).padStart(2, "0")}:${String(sec).padStart(2, "0")}`;
                    if (timeLeft <= 0) {
                        clearInterval(timerInterval);
                        if (el) el.textContent = "시간 만료";
                        const input = document.getElementById("phone-code");
                        if (input) input.disabled = true;
                    }
                }, 1000);
            },
            willClose: () => clearInterval(timerInterval),
            preConfirm: () => {
                const val = document.getElementById("phone-code")?.value;
                if (!val || val.length < 6) {
                    Swal.showValidationMessage("인증번호 6자리를 입력해주세요.");
                    return false;
                }
                return val;
            },
        });

        if (!code) return;

        // Step 3. 인증코드 서버 검증
        try {
            await api.post("/api/user/auth/local/verify-code", { email, code });
        } catch (err) {
            Swal.fire({
                icon: "error",
                title: "인증 실패",
                text: err.response?.data?.message || "인증번호가 올바르지 않습니다.",
                confirmButtonColor: "#d33",
            });
            return;
        }

        // Step 4. 새 전화번호 입력 + 저장
        const { value: newPhone } = await Swal.fire({
            title: "새 전화번호 입력",
            html: `
                <div style="color:#666; font-size:0.9rem; margin-bottom:10px;">본인 확인 완료! 변경할 전화번호를 입력해주세요.</div>
                <input id="new-phone" class="swal2-input" placeholder="010-0000-0000" maxlength="13" style="width:75%;">
            `,
            showCancelButton: true,
            confirmButtonText: "변경하기",
            cancelButtonText: "취소",
            confirmButtonColor: "#3085d6",
            showLoaderOnConfirm: true,
            allowOutsideClick: () => !Swal.isLoading(),
            didOpen: () => {
                const el = document.getElementById("new-phone");
                if (el) el.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
            },
            preConfirm: async () => {
                const phone = document.getElementById("new-phone")?.value;
                const phoneRegex = /^01[016789]-?\d{3,4}-?\d{4}$/;
                if (!phone) { Swal.showValidationMessage("전화번호를 입력해주세요."); return false; }
                if (!phoneRegex.test(phone)) { Swal.showValidationMessage("올바른 형식이 아닙니다. (예: 010-1234-5678)"); return false; }

                try {
                    await updateMutation.mutateAsync(phone);
                    return phone;
                } catch (err) {
                    Swal.showValidationMessage(err.response?.data?.message || "전화번호 변경에 실패했습니다.");
                    return false;
                }
            },
        });

        if (newPhone) {
            Swal.fire({ icon: "success", title: "전화번호가 변경되었습니다.", timer: 1500, showConfirmButton: false });
        }
    };

    return (
        <div className="security-card">
            <h3 className="security-card__title">보안 설정</h3>

            {/* 전화번호 변경 */}
            <div className="security-row">
                <div className="security-row__info">
                    <span className="security-label">전화번호</span>
                    <span className="security-value">{currentPhone ?? "-"}</span>
                </div>
                <button className="btn-change" onClick={handlePhoneChange}>
                    전화번호 변경
                </button>
            </div>

            <div className="security-divider" />

            {/* 비밀번호 변경 */}
            <div className="security-row">
                <div className="security-row__info">
                    <span className="security-label">비밀번호</span>
                    <span className="security-value security-value--masked">● ● ● ● ● ● ● ●</span>
                </div>
                <button
                    className="btn-change"
                    onClick={() => handlePasswordChange({ name, email, phone: currentPhone ?? "" })}
                >
                    비밀번호 변경
                </button>
            </div>

            <p className="security-desc">변경 시 등록된 이메일({email})로 인증번호가 발송됩니다.</p>
        </div>
    );
};

export default SecuritySection;
