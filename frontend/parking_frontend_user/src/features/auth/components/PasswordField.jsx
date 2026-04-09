import React, { useState } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa";

const PasswordField = ({ id, label, placeholder, value, onChange }) => {
    const [isVisible, setIsVisible] = useState(false);

    return (
        <div className="passwordFieldContainer" style={{ position: "relative", display: "flex", flexDirection: "column" }}>
            <label className="fieldLabel" htmlFor={id}>{label}</label>
            <div style={{ position: "relative" }}>
                <input
                    id={id}
                    type={isVisible ? "text" : "password"}
                    placeholder={placeholder}
                    className="fieldInput"
                    value={value}
                    onChange={onChange}
                    required
                    style={{ width: "100%", paddingRight: "40px", boxSizing: "border-box" }}
                />
                <button
                    type="button"
                    onClick={() => setIsVisible(!isVisible)}
                    style={{
                        position: "absolute",
                        right: "10px",
                        top: "50%",
                        transform: "translateY(-50%)",
                        background: "none",
                        border: "none",
                        cursor: "pointer",
                        fontSize: "18px"
                    }}
                >
                    {isVisible ? <FaEye /> : <FaEyeSlash />}
                </button>
            </div>
        </div>
    );
};

export default PasswordField;