import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { registerSchema } from "../schemas/authSchema";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";
import { useNavigate } from "react-router-dom";
import { User, Mail, Lock, Phone, Store, Eye, EyeOff, Loader2, ArrowRight } from "lucide-react";

export function RegisterForm() {
    const [showPassword, setShowPassword] = useState(false);
    const { signup, isLoading } = useAuthStore();
    const addToast = useUIStore((state) => state.addToast);
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            fullName: "",
            email: "",
            password: "",
            confirmPassword: "",
            phone: "",
            storeName: "",
        },
    });

    const onSubmit = async (values) => {
        const payload = {
            fullName: values.fullName,
            email: values.email,
            password: values.password,
            phone: values.phone,
            storeName: values.storeName,
            roles: "ROLE_STORE_ADMIN",
        };

        const success = await signup(payload);
        if (success) {
            addToast({
                type: "success",
                title: "Thành công",
                message: "Tạo tài khoản và cửa hàng thành công! Đang chuyển hướng...",
            });
            navigate("/pos");
        } else {
            addToast({
                type: "error",
                title: "Đăng ký thất bại",
                message: "Không thể tạo tài khoản. Vui lòng kiểm tra lại thông tin.",
            });
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-3.5">
            {/* Full Name */}
            <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                    Họ và tên quản trị
                </label>
                <div className="relative">
                    <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                    <input
                        {...register("fullName")}
                        type="text"
                        placeholder="Nguyễn Văn A"
                        className={`w-full bg-slate-900/80 border ${
                            errors.fullName ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                        } rounded-xl pl-9 pr-3 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                    />
                </div>
                {errors.fullName && (
                    <p className="text-xs text-rose-400 mt-0.5">{errors.fullName.message}</p>
                )}
            </div>

            {/* Store Name */}
            <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                    Tên cửa hàng / Doanh nghiệp
                </label>
                <div className="relative">
                    <Store className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                    <input
                        {...register("storeName")}
                        type="text"
                        placeholder="Sky Coffee & Tea"
                        className={`w-full bg-slate-900/80 border ${
                            errors.storeName ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                        } rounded-xl pl-9 pr-3 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                    />
                </div>
                {errors.storeName && (
                    <p className="text-xs text-rose-400 mt-0.5">{errors.storeName.message}</p>
                )}
            </div>

            {/* Email & Phone grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1">Email</label>
                    <div className="relative">
                        <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                        <input
                            {...register("email")}
                            type="email"
                            placeholder="admin@pos.vn"
                            className={`w-full bg-slate-900/80 border ${
                                errors.email ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl pl-9 pr-3 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                        />
                    </div>
                    {errors.email && (
                        <p className="text-xs text-rose-400 mt-0.5">{errors.email.message}</p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1">Số điện thoại</label>
                    <div className="relative">
                        <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                        <input
                            {...register("phone")}
                            type="text"
                            placeholder="0901234567"
                            className={`w-full bg-slate-900/80 border ${
                                errors.phone ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl pl-9 pr-3 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                        />
                    </div>
                    {errors.phone && (
                        <p className="text-xs text-rose-400 mt-0.5">{errors.phone.message}</p>
                    )}
                </div>
            </div>

            {/* Password & Confirm Password */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1">Mật khẩu</label>
                    <div className="relative">
                        <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                        <input
                            {...register("password")}
                            type={showPassword ? "text" : "password"}
                            placeholder="••••••••"
                            className={`w-full bg-slate-900/80 border ${
                                errors.password ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl pl-9 pr-10 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200"
                        >
                            {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                        </button>
                    </div>
                    {errors.password && (
                        <p className="text-xs text-rose-400 mt-0.5">{errors.password.message}</p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-medium text-slate-300 mb-1">Nhập lại MK</label>
                    <div className="relative">
                        <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                        <input
                            {...register("confirmPassword")}
                            type={showPassword ? "text" : "password"}
                            placeholder="••••••••"
                            className={`w-full bg-slate-900/80 border ${
                                errors.confirmPassword ? "border-rose-500/80" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl pl-9 pr-3 py-2 text-sm text-white placeholder-slate-500 outline-none transition-all`}
                        />
                    </div>
                    {errors.confirmPassword && (
                        <p className="text-xs text-rose-400 mt-0.5">{errors.confirmPassword.message}</p>
                    )}
                </div>
            </div>

            {/* Submit */}
            <button
                type="submit"
                disabled={isLoading}
                className="w-full mt-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-medium py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-600/20 flex items-center justify-center gap-2 transition-all cursor-pointer text-sm"
            >
                {isLoading ? (
                    <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Đang tạo cửa hàng...
                    </>
                ) : (
                    <>
                        Khởi tạo tài khoản & Cửa hàng
                        <ArrowRight className="w-4 h-4" />
                    </>
                )}
            </button>
        </form>
    );
}

export default RegisterForm;
