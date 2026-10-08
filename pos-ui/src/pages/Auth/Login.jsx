import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Sparkles, ShieldCheck, Store } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import LoginForm from "@/features/auth/components/LoginForm";

export default function Login() {
    const { login } = useAuthStore();
    const navigate = useNavigate();

    const handleQuickLogin = async (demoEmail, demoPassword) => {
        const success = await login(demoEmail, demoPassword);
        if (success) {
            navigate("/pos");
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-background via-muted/30 to-background">
            <div className="w-full max-w-md space-y-6">
                {/* Brand Header */}
                <div className="text-center space-y-2">
                    <div className="inline-flex size-14 rounded-2xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-violet-500 items-center justify-center text-white shadow-xl shadow-blue-500/25 mb-2">
                        <Sparkles className="size-7" />
                    </div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        SkyPOS System
                    </h1>
                    <p className="text-sm text-muted-foreground">
                        Hệ thống Quản lý Bán lẻ & Điểm bán hàng chuyên nghiệp
                    </p>
                </div>

                <Card className="border-border/80 shadow-xl backdrop-blur-md bg-card/90">
                    <CardHeader className="space-y-1 pb-4">
                        <CardTitle className="text-lg">Đăng nhập tài khoản</CardTitle>
                        <CardDescription>
                            Nhập thông tin xác thực để vào hệ thống POS
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {/* Validated Login Form */}
                        <LoginForm />

                        {/* Quick Demo Logins */}
                        <div className="pt-2 border-t border-border/60">
                            <p className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider mb-2 text-center">
                                Tài khoản mẫu thử nghiệm
                            </p>
                            <div className="grid grid-cols-2 gap-2">
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="sm"
                                    onClick={() => handleQuickLogin("admin@pos.com", "admin123")}
                                    className="text-xs h-8 gap-1.5 border-dashed cursor-pointer"
                                >
                                    <ShieldCheck className="size-3.5 text-blue-500" />
                                    Admin (Chủ shop)
                                </Button>
                                <Button
                                    type="button"
                                    variant="outline"
                                    size="sm"
                                    onClick={() => handleQuickLogin("cashier@pos.com", "cashier123")}
                                    className="text-xs h-8 gap-1.5 border-dashed cursor-pointer"
                                >
                                    <Store className="size-3.5 text-emerald-500" />
                                    Thu ngân POS
                                </Button>
                            </div>
                        </div>

                        <div className="text-center text-xs text-muted-foreground pt-1">
                            Chưa có cửa hàng?{" "}
                            <Link to="/register" className="text-blue-600 dark:text-blue-400 font-semibold hover:underline">
                                Đăng ký ngay
                            </Link>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
