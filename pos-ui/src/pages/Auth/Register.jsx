import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Sparkles, ArrowRight, Lock, Mail, User, Phone, Store } from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { storeApi } from "@/services/api";

export default function Register() {
    const [formData, setFormData] = useState({
        fullName: "",
        email: "",
        phone: "",
        password: "",
        storeName: "",
    });
    const { signup, isLoading, error } = useAuthStore();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const success = await signup({
            fullName: formData.fullName,
            email: formData.email,
            phone: formData.phone,
            password: formData.password,
            roles: "ROLE_STORE_ADMIN",
        });

        if (success) {
            // Automatically initialize store if provided
            if (formData.storeName) {
                try {
                    await storeApi.create({
                        branch: formData.storeName,
                        description: "Hệ thống bán lẻ",
                        storeType: "Bán lẻ",
                    });
                } catch (ignored) {}
            }
            navigate("/");
        }
    };

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
                        Tạo tài khoản quản lý và bắt đầu bán hàng ngay hôm nay
                    </p>
                </div>

                <Card className="border-border/80 shadow-xl backdrop-blur-md bg-card/90">
                    <CardHeader className="space-y-1 pb-4">
                        <CardTitle className="text-lg">Thông tin tài khoản</CardTitle>
                        <CardDescription>
                            Đăng ký tài khoản Quản trị viên cửa hàng
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {error && (
                            <div className="p-3 text-xs rounded-lg bg-destructive/10 text-destructive border border-destructive/20 font-medium">
                                {error}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-3">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-foreground">Họ và tên</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        name="fullName"
                                        placeholder="Nguyễn Văn A"
                                        value={formData.fullName}
                                        onChange={handleChange}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-foreground">Tên cửa hàng</label>
                                <div className="relative">
                                    <Store className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        name="storeName"
                                        placeholder="Cửa hàng Tiện Lợi 247"
                                        value={formData.storeName}
                                        onChange={handleChange}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-foreground">Email</label>
                                <div className="relative">
                                    <Mail className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        type="email"
                                        name="email"
                                        placeholder="chucuahang@example.com"
                                        value={formData.email}
                                        onChange={handleChange}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-foreground">Số điện thoại</label>
                                <div className="relative">
                                    <Phone className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        name="phone"
                                        placeholder="0912345678"
                                        value={formData.phone}
                                        onChange={handleChange}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-foreground">Mật khẩu</label>
                                <div className="relative">
                                    <Lock className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                    <Input
                                        type="password"
                                        name="password"
                                        placeholder="••••••••"
                                        value={formData.password}
                                        onChange={handleChange}
                                        className="pl-9 text-sm"
                                        required
                                    />
                                </div>
                            </div>

                            <Button
                                type="submit"
                                disabled={isLoading}
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium cursor-pointer h-9 shadow-md shadow-blue-500/20 mt-3"
                            >
                                {isLoading ? "Đang tạo..." : "Tạo tài khoản & Cửa hàng"}
                                <ArrowRight className="size-4 ml-1.5" />
                            </Button>
                        </form>

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
