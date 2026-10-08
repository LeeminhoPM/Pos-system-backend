import React from "react";
import { Loader2 } from "lucide-react";

export function PageLoader({ text = "Đang tải dữ liệu..." }) {
    return (
        <div className="min-h-[60vh] flex flex-col items-center justify-center p-8 gap-4">
            <div className="relative flex items-center justify-center">
                <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 animate-pulse" />
                <Loader2 className="w-7 h-7 text-indigo-400 animate-spin absolute" />
            </div>
            <p className="text-sm font-medium text-slate-400 animate-pulse">{text}</p>
        </div>
    );
}

export function SkeletonBox({ className = "" }) {
    return (
        <div className={`bg-slate-800/60 rounded-xl animate-pulse ${className}`} />
    );
}

export default PageLoader;
