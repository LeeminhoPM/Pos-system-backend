import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Sparkles, ArrowRight, Lock, Mail, Store, ShieldCheck } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";

export default function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const { login, isLoading, error } = useAuthStore();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        const success = await login(email, password);
        if (success) {
            navigate("/");
        }
    };

    const handleQuickLogin = async (demoEmail, demoPassword) => {
        setEmail(demoEmail);
        setPassword(demoPassword);
        const success = await login(demoEmail, demoPassword);
        if (success) {
            navigate("/");
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
                        {error && (
                            <div className="p-3 text-xs rounded-lg bg-destructive/10 text-destructive border border-destructive/20 font-medium">
                                {error}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-3.5">
                            <div className="space-y-1.5">
                                <label className="text-xs font-semibold text-foreground">Email</label>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        type="email"
                                        placeholder="admin@pos.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <div className="flex items-center justify-between">
                                    <label className="text-xs font-semibold text-foreground">Mật khẩu</label>
                                </div>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        type="password"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <Button
                                type="submit"
                                disabled={isLoading}
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium cursor-pointer h-9 shadow-md shadow-blue-500/20 mt-2"
                            >
                                {isLoading ? "Đang xử lý..." : "Đăng nhập"}
                                <ArrowRight className="size-4 ml-1.5" />
                            </Button>
                        </form>

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
