import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Clock, ShoppingCart, CheckCircle2, AlertCircle, PlayCircle, StopCircle } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { shiftApi } from "@/services/api";

export default function Navbar() {
    const { branch, activeShift, checkActiveShift } = useAuthStore();
    const [currentTime, setCurrentTime] = useState(new Date());
    const [isStartingShift, setIsStartingShift] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const timer = setInterval(() => setCurrentTime(new Date()), 1000);
        return () => clearInterval(timer);
    }, []);

    const handleStartShift = async () => {
        setIsStartingShift(true);
        try {
            await shiftApi.start();
            await checkActiveShift();
            navigate("/shifts");
        } catch (err) {
            alert(err.message || "Không thể bắt đầu ca làm");
        } finally {
            setIsStartingShift(false);
        }
    };

    return (
        <header className="h-16 border-b border-border/80 bg-background/80 backdrop-blur-md px-6 flex items-center justify-between sticky top-0 z-30">
            {/* Left: Active Branch Info */}
            <div className="flex items-center gap-3">
                <span className="font-semibold text-sm text-foreground">
                    {branch ? branch.name : "Đang chọn chi nhánh..."}
                </span>
                {branch && (
                    <span className="text-xs text-muted-foreground hidden md:inline">
                        • {branch.address}
                    </span>
                )}
            </div>

            {/* Right: Shift Status, Clock & Actions */}
            <div className="flex items-center gap-4">
                {/* Clock */}
                <div className="hidden lg:flex items-center gap-1.5 text-xs text-muted-foreground font-mono bg-muted/40 px-2.5 py-1 rounded-md border border-border/40">
                    <Clock className="size-3.5 text-blue-500" />
                    <span>
                        {currentTime.toLocaleDateString("vi-VN")} - {currentTime.toLocaleTimeString("vi-VN")}
                    </span>
                </div>

                {/* Shift Indicator */}
                {activeShift ? (
                    <Link to="/shifts" className="flex items-center gap-1.5">
                        <Badge variant="success" className="gap-1 py-1 px-2 cursor-pointer hover:opacity-90">
                            <CheckCircle2 className="size-3" />
                            <span>Ca làm: Đang mở</span>
                        </Badge>
                    </Link>
                ) : (
                    <div className="flex items-center gap-2">
                        <Badge variant="warning" className="gap-1 py-1 px-2">
                            <AlertCircle className="size-3" />
                            <span>Chưa mở ca</span>
                        </Badge>
                        <Button
                            size="sm"
                            variant="outline"
                            onClick={handleStartShift}
                            disabled={isStartingShift}
                            className="text-xs h-7 gap-1 border-blue-500/30 text-blue-600 hover:bg-blue-500/10 cursor-pointer"
                        >
                            <PlayCircle className="size-3.5" />
                            {isStartingShift ? "Đang mở..." : "Mở ca"}
                        </Button>
                    </div>
                )}

                {/* Direct POS Launch Button */}
                <Link to="/pos">
                    <Button size="sm" className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 h-8 font-medium cursor-pointer shadow-sm shadow-blue-500/20">
                        <ShoppingCart className="size-4" />
                        <span>Bán Hàng</span>
                    </Button>
                </Link>
            </div>
        </header>
    );
}
