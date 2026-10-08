import React from "react";
import { useUIStore } from "@/store/useUIStore";
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from "lucide-react";

export function ToastContainer() {
    const { toasts, removeToast } = useUIStore();

    if (!toasts || toasts.length === 0) return null;

    const icons = {
        success: <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />,
        error: <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />,
        warning: <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />,
        info: <Info className="w-5 h-5 text-sky-400 shrink-0" />,
    };

    const borders = {
        success: "border-emerald-500/30 bg-emerald-950/80 shadow-emerald-950/20",
        error: "border-rose-500/30 bg-rose-950/80 shadow-rose-950/20",
        warning: "border-amber-500/30 bg-amber-950/80 shadow-amber-950/20",
        info: "border-sky-500/30 bg-sky-950/80 shadow-sky-950/20",
    };

    return (
        <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-2.5 max-w-sm w-full pointer-events-none">
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    className={`pointer-events-auto flex items-start gap-3 p-3.5 rounded-xl border backdrop-blur-md shadow-xl transition-all duration-300 transform translate-y-0 ${
                        borders[toast.type] || borders.info
                    }`}
                >
                    {icons[toast.type] || icons.info}
                    <div className="flex-1 min-w-0">
                        {toast.title && (
                            <h4 className="text-xs font-semibold text-white mb-0.5">{toast.title}</h4>
                        )}
                        <p className="text-xs text-slate-200 leading-relaxed break-words">{toast.message}</p>
                    </div>
                    <button
                        onClick={() => removeToast(toast.id)}
                        className="text-slate-400 hover:text-white p-0.5 rounded transition-colors"
                        aria-label="Đóng thông báo"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>
            ))}
        </div>
    );
}

export default ToastContainer;
