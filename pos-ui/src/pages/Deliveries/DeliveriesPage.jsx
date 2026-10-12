import React, { useState, useEffect } from "react";
import { deliveryApi } from "@/services/api";
import { Truck, Search, CheckCircle2, Clock, AlertTriangle, RefreshCw } from "lucide-react";

export default function DeliveriesPage() {
    const [deliveries, setDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState("");
    const [searchTracking, setSearchTracking] = useState("");

    // Giả định branchId từ localStorage hoặc default
    const currentBranchId = localStorage.getItem("currentBranchId") || "11111111-1111-1111-1111-111111111111";

    useEffect(() => {
        loadDeliveries();
    }, [statusFilter]);

    const loadDeliveries = async () => {
        setLoading(true);
        try {
            const res = await deliveryApi.getByBranch(currentBranchId, {
                status: statusFilter || undefined,
                page: 0,
                size: 20
            });
            setDeliveries(res?.data?.data?.content || []);
        } catch (err) {
            console.error("Lỗi tải danh sách vận chuyển:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateStatus = async (id, newStatus) => {
        try {
            await deliveryApi.updateStatus(id, { status: newStatus });
            await loadDeliveries();
        } catch (err) {
            alert(err?.response?.data?.message || err.message || "Cập nhật thất bại");
        }
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case "DELIVERED":
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-700">Đã giao hàng</span>;
            case "SHIPPING":
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-100 text-sky-700">Đang giao</span>;
            case "PREPARING":
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">Đang đóng gói</span>;
            case "CONFIRMED":
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-700">Đã xác nhận</span>;
            case "CANCELLED":
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-700">Đã hủy</span>;
            default:
                return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">Chờ xử lý</span>;
        }
    };

    return (
        <div className="space-y-6 max-w-7xl mx-auto p-6">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
                        <Truck className="w-7 h-7 text-indigo-600 dark:text-indigo-400" />
                        Quản Lý Giao Hàng & Đơn Trực Tuyến
                    </h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                        Theo dõi vận đơn, cập nhật tiến độ shipper và quản lý đơn online đa kênh
                    </p>
                </div>

                <button
                    onClick={loadDeliveries}
                    className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 text-slate-700 dark:text-slate-300 font-medium transition"
                >
                    <RefreshCw className="w-4 h-4" />
                    Làm mới
                </button>
            </div>

            {/* Filter Bar */}
            <div className="flex flex-wrap gap-3 bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
                <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="text-sm rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900 p-2 text-slate-800 dark:text-slate-200"
                >
                    <option value="">Tất cả trạng thái</option>
                    <option value="PENDING">Chờ xử lý</option>
                    <option value="CONFIRMED">Đã xác nhận</option>
                    <option value="PREPARING">Đang đóng gói</option>
                    <option value="SHIPPING">Đang giao hàng</option>
                    <option value="DELIVERED">Đã hoàn thành</option>
                    <option value="CANCELLED">Đã hủy</option>
                </select>
            </div>

            {/* Delivery List */}
            <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-slate-600 dark:text-slate-300">
                        <thead className="bg-slate-50 dark:bg-slate-900/50 text-xs uppercase text-slate-500 border-b border-slate-200 dark:border-slate-700">
                            <tr>
                                <th className="py-3.5 px-4">Mã Vận Đơn</th>
                                <th className="py-3.5 px-4">Đơn Hàng</th>
                                <th className="py-3.5 px-4">Người Nhận</th>
                                <th className="py-3.5 px-4">Địa Chỉ Giao</th>
                                <th className="py-3.5 px-4">Tổng Tiền</th>
                                <th className="py-3.5 px-4">Trạng Thái</th>
                                <th className="py-3.5 px-4 text-right">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
                            {deliveries.length > 0 ? (
                                deliveries.map((d) => (
                                    <tr key={d.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/30">
                                        <td className="py-3.5 px-4 font-mono font-semibold text-indigo-600 dark:text-indigo-400">
                                            {d.trackingNumber}
                                        </td>
                                        <td className="py-3.5 px-4 font-medium text-slate-900 dark:text-slate-100">
                                            {d.orderNumber}
                                        </td>
                                        <td className="py-3.5 px-4">
                                            <div className="font-medium text-slate-900 dark:text-slate-100">{d.recipientName}</div>
                                            <div className="text-xs text-slate-400">{d.recipientPhone}</div>
                                        </td>
                                        <td className="py-3.5 px-4 max-w-xs truncate text-xs" title={d.shippingAddress}>
                                            {d.shippingAddress}
                                        </td>
                                        <td className="py-3.5 px-4 font-semibold text-slate-900 dark:text-slate-100">
                                            {Number(d.totalAmount).toLocaleString("vi-VN")} đ
                                        </td>
                                        <td className="py-3.5 px-4">{getStatusBadge(d.deliveryStatus)}</td>
                                        <td className="py-3.5 px-4 text-right">
                                            {d.deliveryStatus === "PENDING" && (
                                                <button
                                                    onClick={() => handleUpdateStatus(d.id, "CONFIRMED")}
                                                    className="text-xs font-semibold px-2.5 py-1 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-lg mr-1"
                                                >
                                                    Nhận đơn
                                                </button>
                                            )}
                                            {d.deliveryStatus === "CONFIRMED" && (
                                                <button
                                                    onClick={() => handleUpdateStatus(d.id, "SHIPPING")}
                                                    className="text-xs font-semibold px-2.5 py-1 bg-sky-50 hover:bg-sky-100 text-sky-700 rounded-lg mr-1"
                                                >
                                                    Giao hàng
                                                </button>
                                            )}
                                            {d.deliveryStatus === "SHIPPING" && (
                                                <button
                                                    onClick={() => handleUpdateStatus(d.id, "DELIVERED")}
                                                    className="text-xs font-semibold px-2.5 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg"
                                                >
                                                    Đã giao
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={7} className="text-center py-10 text-slate-400">
                                        Chưa có đơn hàng giao vận
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
