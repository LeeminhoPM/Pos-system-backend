import * as React from "react";
import { cn } from "@/lib/utils";
import { X } from "lucide-react";

export function Modal({ isOpen, onClose, title, description, children, maxWidth = "max-w-lg" }) {
    React.useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === "Escape" && isOpen) {
                onClose();
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Backdrop */}
            <div 
                className="fixed inset-0 bg-black/60 backdrop-blur-xs transition-opacity animate-in fade-in"
                onClick={onClose}
            />

            {/* Modal Dialog Content */}
            <div 
                className={cn(
                    "relative z-50 w-full rounded-xl border border-border bg-card p-6 text-card-foreground shadow-2xl transition-all animate-in zoom-in-95 duration-150 max-h-[90vh] flex flex-col",
                    maxWidth
                )}
            >
                {/* Header */}
                <div className="flex items-start justify-between pb-4 border-b border-border/60">
                    <div>
                        {title && <h3 className="text-lg font-semibold leading-none tracking-tight">{title}</h3>}
                        {description && <p className="text-sm text-muted-foreground mt-1.5">{description}</p>}
                    </div>
                    <button
                        onClick={onClose}
                        className="rounded-md p-1.5 text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
                    >
                        <X className="size-4" />
                    </button>
                </div>

                {/* Body */}
                <div className="flex-1 overflow-y-auto py-4">
                    {children}
                </div>
            </div>
        </div>
    );
}
