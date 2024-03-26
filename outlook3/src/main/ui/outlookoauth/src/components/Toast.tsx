import React, {useEffect, useState} from "react";

interface ToastProps {
    message: string;
    type: string;
    duration?: number;
    onClose?: () => void;
}

const Toast: React.FC<ToastProps> = ({message, type, duration, onClose}) => {
    const [showToast, setShowToast] = useState<boolean>(false);

    useEffect(() => {
        setShowToast(true);
        const timeoutId = setTimeout(() => {
            setShowToast(false);
            onClose && onClose();
        }, duration);
        return () => {
            clearTimeout(timeoutId);
        };
    }, [duration, onClose]);

    return (
        <>
            {showToast && (
                <div className={`toast ${type}`}>
                    <div className="toast-message">{message}</div>
                </div>
            )}
        </>
    );
};

export default Toast;
