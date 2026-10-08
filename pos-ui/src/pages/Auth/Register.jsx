import React from "react";
import { Link } from "react-router-dom";
import { Sparkles } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import RegisterForm from "@/features/auth/components/RegisterForm";

export default function Register() {
    return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-background via-muted/30 to-background">
            <div className="w-full max-w-md space-y-6">
                <div className="text-center space-y-2">
                    <div className="inline-flex size-14 rounded-2xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-violet-500 items-center justify-center text-white shadow-xl shadow-blue-500/25 mb-2">
                        <Sparkles className="size-7" />
                    </div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Đăng Ký SkyPOS
                    </h1>
                    <p className="text-sm text-muted-foreground">
                        Tạo tài khoản quản trị và thiết lập chuỗi bán lẻ của bạn
                    </p>
                </div>

                <Card className="border-border/80 shadow-xl backdrop-blur-md bg-card/90">
                    <CardHeader className="space-y-1 pb-3">
                        <CardTitle className="text-lg">Khởi tạo cửa hàng</CardTitle>
                        <CardDescription>
                            Điền thông tin doanh nghiệp để kích hoạt hệ thống
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <RegisterForm />

                        <div className="text-center text-xs text-muted-foreground pt-1">
                            Đã có tài khoản?{" "}
                            <Link to="/login" className="text-blue-600 dark:text-blue-400 font-semibold hover:underline">
                                Đăng nhập
                            </Link>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
