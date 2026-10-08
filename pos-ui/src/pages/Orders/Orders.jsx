import React, { useState, useEffect } from "react";
import {
    Receipt,
    Search,
    Eye,
    RotateCcw,
    Printer,
    CheckCircle2,
    Clock,
    XCircle,
    Calendar,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { orderApi, refundApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Orders() {
    const { branch, store, user } = useAuthStore();
    const [orders, setOrders] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [isLoading, setIsLoading] = useState(true);

    // Modals
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [isDetailOpen, setIsDetailOpen] = useState(false);
    const [isRefundOpen, setIsRefundOpen] = useState(false);
    const [refundReason, setRefundReason] = useState("");
    const [refundAmount, setRefundAmount] = useState("");
    const [isSubmittingRefund, setIsSubmittingRefund] = useState(false);

    const loadOrders = async () => {
        if (!branch?.id) return;
        setIsLoading(true);
        try {
            const data = await orderApi.getByBranch(branch.id);
            setOrders(data || []);
        } catch (err) {
            console.error("Lỗi khi tải danh sách đơn hàng:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadOrders();
    }, [branch?.id]);

    const handleOpenDetail = (order) => {
        setSelectedOrder(order);
        setIsDetailOpen(true);
    };

    const handleOpenRefund = (order) => {
        setSelectedOrder(order);
        setRefundAmount(order.totalAmount?.toString() || "");
        setRefundReason("Khách yêu cầu trả hàng / đổi ý");
        setIsRefundOpen(true);
    };

    const handleConfirmRefund = async (e) => {
        e.preventDefault();
        if (!selectedOrder) return;
        setIsSubmittingRefund(true);
        try {
            await refundApi.create({
                orderId: selectedOrder.id,
                reason: refundReason,
                amount: Number(refundAmount) || selectedOrder.totalAmount,
            });
            alert("Hoàn tiền thành công! Hàng hóa đã được hoàn lại vào kho.");
            setIsRefundOpen(false);
            loadOrders();
        } catch (err) {
            alert(err.message || "Không thể thực hiện hoàn tiền");
        } finally {
            setIsSubmittingRefund(false);
        }
    };

    const filtered = orders.filter((o) => {
        const matchesStatus = statusFilter === "ALL" || o.status === statusFilter;
        const q = searchQuery.toLowerCase().trim();
        const matchesQuery =
            !q ||
            o.orderNumber?.toLowerCase().includes(q) ||
            o.customer?.fullName?.toLowerCase().includes(q) ||
            o.cashier?.fullName?.toLowerCase().includes(q);
        return matchesStatus && matchesQuery;
    });

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Lịch Sử Đơn Hàng & Giao Dịch
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Theo dõi toàn bộ đơn hàng bán ra và xử lý hoàn tiền
                    </p>
                </div>
            </div>

            {/* Filter Bar */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-4 flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                        <Input
                            placeholder="Tìm kiếm theo mã đơn, tên khách hàng hoặc thu ngân..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-9 h-9 text-sm"
                        />
                    </div>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="text-xs rounded-lg border border-border bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-1 focus:ring-primary min-w-[140px]"
                    >
                        <option value="ALL">Tất cả trạng thái</option>
                        <option value="COMPLETED">Hoàn thành</option>
                        <option value="REFUNDED">Đã hoàn tiền</option>
                        <option value="PENDING">Chờ xử lý</option>
                    </select>
                </CardContent>
            </Card>

            {/* Orders Table */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-0">
                    {isLoading ? (
                        <div className="py-12 text-center text-sm text-muted-foreground">Đang tải đơn hàng...</div>
                    ) : filtered.length === 0 ? (
                        <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                            <Receipt className="size-10 text-muted-foreground/40" />
                            <span>Không có đơn hàng nào khớp với tìm kiếm.</span>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-xs border-collapse">
                                <thead>
                                    <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                        <th className="py-3 px-4">Mã đơn</th>
                                        <th className="py-3 px-4">Thời gian</th>
                                        <th className="py-3 px-4">Khách hàng</th>
                                        <th className="py-3 px-4">Thu ngân</th>
                                        <th className="py-3 px-4">Thanh toán</th>
                                        <th className="py-3 px-4 text-right">Tổng tiền</th>
                                        <th className="py-3 px-4">Trạng thái</th>
                                        <th className="py-3 px-4 text-center">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {filtered.map((order) => (
                                        <tr key={order.id} className="hover:bg-muted/20 transition-colors">
                                            <td className="py-3 px-4 font-mono font-semibold text-foreground">
                                                {order.orderNumber || order.id?.substring(0, 8)}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {order.createdAt ? new Date(order.createdAt).toLocaleString("vi-VN") : "—"}
                                            </td>
                                            <td className="py-3 px-4 font-medium text-foreground">
                                                {order.customer?.fullName || "Khách lẻ"}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {order.cashier?.fullName || "—"}
                                            </td>
                                            <td className="py-3 px-4">
                                                <Badge variant="outline">
                                                    {order.paymentType === "CASH" ? "Tiền mặt" : "Thẻ / Chuyển khoản"}
                                                </Badge>
                                            </td>
                                            <td className="py-3 px-4 text-right font-bold text-foreground">
                                                {(order.totalAmount || 0).toLocaleString("vi-VN")} ₫
                                            </td>
                                            <td className="py-3 px-4">
                                                <Badge
                                                    variant={order.status === "REFUNDED" ? "destructive" : "success"}
                                                >
                                                    {order.status === "REFUNDED" ? "Đã hoàn trả" : "Hoàn thành"}
                                                </Badge>
                                            </td>
                                            <td className="py-3 px-4 text-center">
                                                <div className="flex items-center justify-center gap-1.5">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        title="Xem chi tiết"
                                                        onClick={() => handleOpenDetail(order)}
                                                        className="text-muted-foreground hover:text-foreground cursor-pointer"
                                                    >
                                                        <Eye className="size-3.5" />
                                                    </Button>
                                                    {order.status !== "REFUNDED" && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon-xs"
                                                            title="Hoàn tiền đơn này"
                                                            onClick={() => handleOpenRefund(order)}
                                                            className="text-muted-foreground hover:text-destructive cursor-pointer"
                                                        >
                                                            <RotateCcw className="size-3.5" />
                                                        </Button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Order Detail Modal */}
            <Modal
                isOpen={isDetailOpen}
                onClose={() => setIsDetailOpen(false)}
                title="Chi tiết đơn hàng"
                description={selectedOrder?.orderNumber}
                maxWidth="max-w-lg"
            >
                {selectedOrder && (
                    <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-2 text-xs p-3 rounded-lg bg-muted/40 border border-border/40">
                            <div>
                                <span className="text-muted-foreground">Khách hàng: </span>
                                <span className="font-semibold">{selectedOrder.customer?.fullName || "Khách lẻ"}</span>
                            </div>
                            <div>
                                <span className="text-muted-foreground">Thu ngân: </span>
                                <span className="font-semibold">{selectedOrder.cashier?.fullName || "—"}</span>
                            </div>
                            <div>
                                <span className="text-muted-foreground">Thời gian: </span>
                                <span>{new Date(selectedOrder.createdAt).toLocaleString("vi-VN")}</span>
                            </div>
                            <div>
                                <span className="text-muted-foreground">Hình thức: </span>
                                <span>{selectedOrder.paymentType === "CASH" ? "Tiền mặt" : "Thẻ / Chuyển khoản"}</span>
                            </div>
                        </div>

                        {/* Items list */}
                        <div className="border rounded-lg border-border/60 overflow-hidden">
                            <table className="w-full text-left text-xs">
                                <thead className="bg-muted/30 border-b border-border/60">
                                    <tr>
                                        <th className="p-2.5">Sản phẩm</th>
                                        <th className="p-2.5 text-center">SL</th>
                                        <th className="p-2.5 text-right">Đơn giá</th>
                                        <th className="p-2.5 text-right">Thành tiền</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {selectedOrder.items?.map((item, idx) => (
                                        <tr key={idx}>
                                            <td className="p-2.5 font-medium">{item.product?.name}</td>
                                            <td className="p-2.5 text-center">{item.quantity}</td>
                                            <td className="p-2.5 text-right">{(item.product?.sellingPrice || 0).toLocaleString("vi-VN")} ₫</td>
                                            <td className="p-2.5 text-right font-semibold">{(item.price || 0).toLocaleString("vi-VN")} ₫</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="space-y-1.5 text-xs pt-2 border-t border-border/60">
                            <div className="flex justify-between text-muted-foreground">
                                <span>Tạm tính:</span>
                                <span>{(selectedOrder.subtotal || selectedOrder.totalAmount || 0).toLocaleString("vi-VN")} ₫</span>
                            </div>
                            <div className="flex justify-between text-muted-foreground">
                                <span>Giảm giá:</span>
                                <span>-{(selectedOrder.discount || 0).toLocaleString("vi-VN")} ₫</span>
                            </div>
                            <div className="flex justify-between text-muted-foreground">
                                <span>Thuế:</span>
                                <span>{(selectedOrder.tax || 0).toLocaleString("vi-VN")} ₫</span>
                            </div>
                            <div className="flex justify-between font-bold text-sm text-foreground pt-1 border-t border-border">
                                <span>Tổng cộng:</span>
                                <span className="text-blue-600 dark:text-blue-400">
                                    {(selectedOrder.totalAmount || 0).toLocaleString("vi-VN")} ₫
                                </span>
                            </div>
                        </div>

                        <div className="flex justify-end gap-2 pt-2">
                            <Button variant="outline" size="sm" onClick={() => window.print()} className="gap-1.5 text-xs cursor-pointer">
                                <Printer className="size-3.5" /> In phiếu
                            </Button>
                        </div>
                    </div>
                )}
            </Modal>

            {/* Refund Order Modal */}
            <Modal
                isOpen={isRefundOpen}
                onClose={() => setIsRefundOpen(false)}
                title="Hoàn tiền đơn hàng"
                description={`Đơn: ${selectedOrder?.orderNumber}`}
                maxWidth="max-w-md"
            >
                <form onSubmit={handleConfirmRefund} className="space-y-4">
                    <div className="p-3 rounded-lg bg-amber-500/10 border border-amber-500/20 text-xs text-amber-700 dark:text-amber-400">
                        Lưu ý: Khi hoàn tiền, sản phẩm trong đơn sẽ được tự động cộng lại vào số lượng tồn kho của chi nhánh.
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Số tiền hoàn (₫)</label>
                        <Input
                            type="number"
                            value={refundAmount}
                            onChange={(e) => setRefundAmount(e.target.value)}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Lý do hoàn tiền</label>
                        <Input
                            placeholder="Khách đổi ý, hàng lỗi, v.v."
                            value={refundReason}
                            onChange={(e) => setRefundReason(e.target.value)}
                            required
                        />
                    </div>

                    <Button
                        type="submit"
                        disabled={isSubmittingRefund}
                        className="w-full bg-destructive text-white hover:bg-destructive/90 text-xs cursor-pointer"
                    >
                        {isSubmittingRefund ? "Đang xử lý hoàn..." : "Xác Nhận Hoàn Tiền & Nhập Lại Kho"}
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
