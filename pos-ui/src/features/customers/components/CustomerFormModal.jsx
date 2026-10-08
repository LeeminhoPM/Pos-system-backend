import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { customerSchema } from "../schemas/customerSchema";
import { customerApi } from "@/services/api";
import { useUIStore } from "@/store/useUIStore";
import { X, Loader2, UserPlus } from "lucide-react";

export function CustomerFormModal({ isOpen, onClose, onSuccess }) {
    const addToast = useUIStore((state) => state.addToast);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: zodResolver(customerSchema),
        defaultValues: {
            fullName: "",
            phone: "",
            email: "",
            address: "",
            notes: "",
        },
    });

    if (!isOpen) return null;

    const onSubmit = async (values) => {
        try {
            const result = await customerApi.create(values);
            addToast({
                type: "success",
                title: "Thành công",
                message: `Đã thêm khách hàng "${values.fullName}"`,
            });
            reset();
            if (onSuccess) onSuccess(result);
            onClose();
        } catch (err) {
            addToast({
                type: "error",
                title: "Lỗi",
                message: err.message || "Không thể tạo khách hàng",
            });
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl">
                <div className="flex items-center justify-between pb-3 mb-4 border-b border-slate-800">
                    <h2 className="text-base font-bold text-white flex items-center gap-2">
                        <UserPlus className="w-5 h-5 text-indigo-400" />
                        Thêm khách hàng thành viên
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-3.5">
                    <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                            Tên khách hàng <span className="text-rose-400">*</span>
                        </label>
                        <input
                            {...register("fullName")}
                            type="text"
                            placeholder="Nguyễn Văn B..."
                            className={`w-full bg-slate-950 border ${
                                errors.fullName ? "border-rose-500" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl px-3 py-2 text-sm text-white outline-none`}
                        />
                        {errors.fullName && (
                            <p className="text-xs text-rose-400 mt-1">{errors.fullName.message}</p>
                        )}
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Số điện thoại <span className="text-rose-400">*</span>
                            </label>
                            <input
                                {...register("phone")}
                                type="text"
                                placeholder="0987654321..."
                                className={`w-full bg-slate-950 border ${
                                    errors.phone ? "border-rose-500" : "border-slate-800 focus:border-indigo-500"
                                } rounded-xl px-3 py-2 text-sm text-white outline-none`}
                            />
                            {errors.phone && (
                                <p className="text-xs text-rose-400 mt-1">{errors.phone.message}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Email liên hệ
                            </label>
                            <input
                                {...register("email")}
                                type="email"
                                placeholder="khach@example.com"
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                            {errors.email && (
                                <p className="text-xs text-rose-400 mt-1">{errors.email.message}</p>
                            )}
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                            Địa chỉ giao hàng / Nhà riêng
                        </label>
                        <input
                            {...register("address")}
                            type="text"
                            placeholder="123 Lê Lợi, Phường Bến Nghé, Quận 1..."
                            className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">Ghi chú</label>
                        <textarea
                            {...register("notes")}
                            rows="2"
                            placeholder="Sở thích, lưu ý dị ứng..."
                            className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                        />
                    </div>

                    <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-sm font-medium rounded-xl transition-colors cursor-pointer"
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="inline-flex items-center gap-2 px-5 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-sm font-medium rounded-xl shadow-lg shadow-indigo-600/20 transition-all cursor-pointer"
                        >
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                    Đang tạo...
                                </>
                            ) : (
                                "Tạo khách hàng"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default CustomerFormModal;
