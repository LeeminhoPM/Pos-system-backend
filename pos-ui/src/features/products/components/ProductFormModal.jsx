import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { productSchema } from "../schemas/productSchema";
import { productApi } from "@/services/api";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";
import { X, Loader2, Save } from "lucide-react";

export function ProductFormModal({ isOpen, onClose, product = null, categories = [], onSuccess }) {
    const { store } = useAuthStore();
    const addToast = useUIStore((state) => state.addToast);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: zodResolver(productSchema),
        defaultValues: {
            name: "",
            sku: "",
            barcode: "",
            brand: "",
            categoryId: "",
            costPrice: 0,
            sellingPrice: 0,
            minStockLevel: 5,
            description: "",
            image: "",
        },
    });

    useEffect(() => {
        if (product) {
            reset({
                name: product.name || "",
                sku: product.sku || "",
                barcode: product.barcode || "",
                brand: product.brand || "",
                categoryId: product.categoryId || product.category?.id || "",
                costPrice: product.costPrice || 0,
                sellingPrice: product.sellingPrice || 0,
                minStockLevel: product.minStockLevel || 5,
                description: product.description || "",
                image: product.image || "",
            });
        } else {
            reset({
                name: "",
                sku: "",
                barcode: "",
                brand: "",
                categoryId: categories[0]?.id || "",
                costPrice: 0,
                sellingPrice: 0,
                minStockLevel: 5,
                description: "",
                image: "",
            });
        }
    }, [product, categories, reset, isOpen]);

    if (!isOpen) return null;

    const onSubmit = async (values) => {
        try {
            const payload = {
                ...values,
                storeId: store?.id,
            };

            if (product?.id) {
                await productApi.update(product.id, payload);
                addToast({
                    type: "success",
                    title: "Thành công",
                    message: "Đã cập nhật thông tin sản phẩm!",
                });
            } else {
                await productApi.create(payload);
                addToast({
                    type: "success",
                    title: "Thành công",
                    message: "Đã thêm mới sản phẩm thành công!",
                });
            }

            if (onSuccess) onSuccess();
            onClose();
        } catch (err) {
            addToast({
                type: "error",
                title: "Thất bại",
                message: err.message || "Không thể lưu sản phẩm",
            });
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 shadow-2xl overflow-y-auto max-h-[90vh]">
                {/* Header */}
                <div className="flex items-center justify-between pb-4 mb-4 border-b border-slate-800">
                    <h2 className="text-lg font-bold text-white">
                        {product ? "Cập nhật sản phẩm" : "Thêm mới sản phẩm"}
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    {/* Name */}
                    <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                            Tên sản phẩm <span className="text-rose-400">*</span>
                        </label>
                        <input
                            {...register("name")}
                            type="text"
                            placeholder="Cà phê sữa đá size L..."
                            className={`w-full bg-slate-950 border ${
                                errors.name ? "border-rose-500" : "border-slate-800 focus:border-indigo-500"
                            } rounded-xl px-3 py-2 text-sm text-white outline-none`}
                        />
                        {errors.name && <p className="text-xs text-rose-400 mt-1">{errors.name.message}</p>}
                    </div>

                    {/* Category & Brand */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Danh mục <span className="text-rose-400">*</span>
                            </label>
                            <select
                                {...register("categoryId")}
                                className={`w-full bg-slate-950 border ${
                                    errors.categoryId ? "border-rose-500" : "border-slate-800 focus:border-indigo-500"
                                } rounded-xl px-3 py-2 text-sm text-white outline-none`}
                            >
                                <option value="">-- Chọn danh mục --</option>
                                {categories.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {c.name}
                                    </option>
                                ))}
                            </select>
                            {errors.categoryId && (
                                <p className="text-xs text-rose-400 mt-1">{errors.categoryId.message}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Thương hiệu / Nhãn hàng
                            </label>
                            <input
                                {...register("brand")}
                                type="text"
                                placeholder="Highlands, Nestle..."
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                        </div>
                    </div>

                    {/* SKU & Barcode */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Mã SKU (Mã nội bộ)
                            </label>
                            <input
                                {...register("sku")}
                                type="text"
                                placeholder="SKU-001 (Tự động nếu trống)"
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Mã Barcode / Quét mã
                            </label>
                            <input
                                {...register("barcode")}
                                type="text"
                                placeholder="893..."
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                        </div>
                    </div>

                    {/* Cost & Selling Prices */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Giá vốn (VNĐ) <span className="text-rose-400">*</span>
                            </label>
                            <input
                                {...register("costPrice")}
                                type="number"
                                min="0"
                                step="1000"
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                            {errors.costPrice && (
                                <p className="text-xs text-rose-400 mt-1">{errors.costPrice.message}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Giá bán (VNĐ) <span className="text-rose-400">*</span>
                            </label>
                            <input
                                {...register("sellingPrice")}
                                type="number"
                                min="0"
                                step="1000"
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                            {errors.sellingPrice && (
                                <p className="text-xs text-rose-400 mt-1">{errors.sellingPrice.message}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-slate-300 mb-1">
                                Tồn kho tối thiểu
                            </label>
                            <input
                                {...register("minStockLevel")}
                                type="number"
                                min="0"
                                className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                            />
                        </div>
                    </div>

                    {/* Image URL */}
                    <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                            URL Hình ảnh sản phẩm
                        </label>
                        <input
                            {...register("image")}
                            type="url"
                            placeholder="https://..."
                            className="w-full bg-slate-950 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-sm text-white outline-none"
                        />
                    </div>

                    {/* Actions */}
                    <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
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
                                    Đang lưu...
                                </>
                            ) : (
                                <>
                                    <Save className="w-4 h-4" />
                                    Lưu sản phẩm
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default ProductFormModal;
