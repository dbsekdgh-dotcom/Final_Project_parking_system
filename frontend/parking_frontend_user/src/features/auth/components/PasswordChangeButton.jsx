import React from "react";
import { handlePasswordChange } from "../utils/accountUtils";

export default function PasswordChangeButton({ name = "", email = "", phone = "" }) {
  return (
    <button 
      type="button" 
      className="aalw__account-link-btn" 
      onClick={() => handlePasswordChange({ name, email, phone })}
    >
      비밀번호 변경
    </button>
  );
}