import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loginSchema } from "../schemas/authSchema";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";
import { useNavigate } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff, Loader2, ArrowRight } from "lucide-react";

export function LoginForm() {
    const [showPassword, setShowPassword] = useState(false);
    const { login, isLoading } = useAuthStore();
    const addToast = useUIStore((state) => state.addToast);
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    const onSubmit = async (values) => {
        const success = await login(values.email, values.password);
        if (success) {
            addToast({
                type: "success",
                title: "Thành công",
                message: "Đăng nhập thành công! Chào mừng trở lại.",
            });
            navigate("/pos");
        } else {
            addToast({
                type: "error",
                title: "Đăng nhập thất bại",
                message: "Email hoặc mật khẩu không chính xác.",
            });
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            {/* Email Field */}
            <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                    Địa chỉ Email
                </label>
                <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                    <input
                        {...register("email")}
                        type="email"
                        placeholder="admin@pos.com"
                        className={`w-full bg-slate-900/80 border ${
                            errors.email ? "border-rose-500/80 focus:ring-rose-500/20" : "border-slate-800 focus:border-indigo-500 focus:ring-indigo-500/20"
                        } rounded-xl pl-9 pr-3 py-2.5 text-sm text-white placeholder-slate-500 outline-none focus:ring-2 transition-all`}
                    />
                </div>
                {errors.email && (
                    <p className="text-xs text-rose-400 mt-1">{errors.email.message}</p>
                )}
            </div>

            {/* Password Field */}
            <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                    Mật khẩu
                </label>
                <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                    <input
                        {...register("password")}
                        type={showPassword ? "text" : "password"}
                        placeholder="••••••••"
                        className={`w-full bg-slate-900/80 border ${
                            errors.password ? "border-rose-500/80 focus:ring-rose-500/20" : "border-slate-800 focus:border-indigo-500 focus:ring-indigo-500/20"
                        } rounded-xl pl-9 pr-10 py-2.5 text-sm text-white placeholder-slate-500 outline-none focus:ring-2 transition-all`}
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200 transition-colors"
                    >
                        {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                </div>
                {errors.password && (
                    <p className="text-xs text-rose-400 mt-1">{errors.password.message}</p>
                )}
            </div>

            {/* Submit Button */}
            <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-medium py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-600/20 flex items-center justify-center gap-2 transition-all cursor-pointer text-sm"
            >
                {isLoading ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Đang xác thực...
                    </>
                ) : (
                    <>
                        Đăng nhập hệ thống
                        <ArrowRight className="w-4 h-4" />
                    </>
                )}
            </button>
        </form>
    );
}

export default LoginForm;
