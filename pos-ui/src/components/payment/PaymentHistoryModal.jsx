import React, { useState, useEffect } from "react";
import { Modal } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { paymentApi } from "@/services/api";
import {
    History,
    RefreshCw,
    Search,
    CreditCard,
    Banknote,
    RotateCcw,
    CheckCircle2,
    XCircle,
    Clock,
    AlertTriangle,
    FileText,
    ExternalLink,
    Loader2,
} from "lucide-react";

export default function PaymentHistoryModal({ isOpen, onClose }) {
    const [transactions, setTransactions] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [searchQuery, setSearchQuery] = useState("");

    // Refund Modal States
    const [selectedTxnForRefund, setSelectedTxnForRefund] = useState(null);
    const [refundAmount, setRefundAmount] = useState("");
    const [refundReason, setRefundReason] = useState("requested_by_customer");
    const [isRefunding, setIsRefunding] = useState(false);
    const [refundError, setRefundError] = useState(null);

    const loadHistory = async () => {
        setIsLoading(true);
        try {
            const params = { size: 50 };
            if (statusFilter !== "ALL") {
                params.status = statusFilter;
            }
            const res = await paymentApi.getHistory(params);
            const content = res?.data?.content || res?.content || [];
            setTransactions(content);
        } catch (err) {
            console.error("Lỗi khi tải lịch sử thanh toán:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (isOpen) {
            loadHistory();
        }
    }, [isOpen, statusFilter]);

    const handleOpenRefund = (txn) => {
        const remaining = txn.amount - (txn.refundedAmount || 0);
        setSelectedTxnForRefund(txn);
        setRefundAmount(remaining.toString());
        setRefundReason("requested_by_customer");
        setRefundError(null);
    };

    const handleConfirmRefund = async () => {
        if (!selectedTxnForRefund) return;
        setIsRefunding(true);
        setRefundError(null);
        try {
            const amountNum = refundAmount ? Number(refundAmount) : null;
            await paymentApi.refund({
                transactionId: selectedTxnForRefund.id,
                amount: amountNum,
                reason: refundReason,
            });
            alert("Hoàn tiền thành công!");
            setSelectedTxnForRefund(null);
            loadHistory();
        } catch (err) {
            const msg = err.response?.data?.message || err.message || "Lỗi xử lý hoàn tiền";
            setRefundError(msg);
        } finally {
            setIsRefunding(false);
        }
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case "SUCCESS":
                return (
                    <Badge className="bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 gap-1">
                        <CheckCircle2 className="size-3" /> Thành công
                    </Badge>
                );
            case "PENDING":
            case "PROCESSING":
                return (
                    <Badge className="bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30 gap-1">
                        <Clock className="size-3" /> Chờ xử lý
                    </Badge>
                );
            case "FAILED":
                return (
                    <Badge className="bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30 gap-1">
                        <XCircle className="size-3" /> Thất bại
                    </Badge>
                );
            case "REFUNDED":
                return (
                    <Badge className="bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-500/30 gap-1">
                        <RotateCcw className="size-3" /> Đã hoàn tiền
                    </Badge>
                );
            case "PARTIALLY_REFUNDED":
                return (
                    <Badge className="bg-indigo-500/15 text-indigo-600 dark:text-indigo-400 border-indigo-500/30 gap-1">
                        <RotateCcw className="size-3" /> Hoàn một phần
                    </Badge>
                );
            default:
                return <Badge variant="outline">{status}</Badge>;
        }
    };

    const filteredTransactions = transactions.filter((t) => {
        const q = searchQuery.toLowerCase().trim();
        if (!q) return true;
        return (
            t.transactionId?.toLowerCase().includes(q) ||
            t.orderNumber?.toLowerCase().includes(q) ||
            t.gatewayReference?.toLowerCase().includes(q) ||
            t.cardLast4?.includes(q)
        );
    });

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title="Lịch Sử Giao Dịch & Quản Lý Hoàn Tiền"
                description="Theo dõi toàn bộ các phiên thanh toán tiền mặt và thẻ Stripe"
                maxWidth="max-w-4xl"
            >
                {/* Filter and Search Bar */}
                <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mb-4">
                    <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto">
                        {[
                            { id: "ALL", label: "Tất cả" },
                            { id: "SUCCESS", label: "Thành công" },
                            { id: "REFUNDED", label: "Đã hoàn" },
                            { id: "FAILED", label: "Thất bại" },
                        ].map((tab) => (
                            <button
                                key={tab.id}
                                onClick={() => setStatusFilter(tab.id)}
                                className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                                    statusFilter === tab.id
                                        ? "bg-blue-600 text-white shadow-2xs"
                                        : "bg-muted/60 text-muted-foreground hover:bg-muted"
                                }`}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    <div className="flex items-center gap-2 w-full sm:w-auto">
                        <div className="relative flex-1 sm:w-60">
                            <Search className="size-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                placeholder="Tìm mã GD, đơn hàng..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="pl-8 text-xs h-8"
                            />
                        </div>
                        <Button
                            variant="outline"
                            size="icon"
                            onClick={loadHistory}
                            disabled={isLoading}
                            className="size-8 cursor-pointer shrink-0"
                            title="Làm mới"
                        >
                            <RefreshCw className={`size-3.5 ${isLoading ? "animate-spin" : ""}`} />
                        </Button>
                    </div>
                </div>

                {/* Transactions Table */}
                <div className="rounded-xl border border-border/80 overflow-hidden bg-card/60">
                    <div className="overflow-x-auto max-h-[50vh]">
                        <table className="w-full text-xs text-left">
                            <thead className="bg-muted/50 border-b border-border/60 text-muted-foreground font-semibold uppercase sticky top-0 backdrop-blur-xs">
                                <tr>
                                    <th className="px-3.5 py-2.5">Mã GD / Đơn</th>
                                    <th className="px-3 py-2.5">Phương thức</th>
                                    <th className="px-3 py-2.5">Số tiền</th>
                                    <th className="px-3 py-2.5">Thông tin thẻ</th>
                                    <th className="px-3 py-2.5">Trạng thái</th>
                                    <th className="px-3 py-2.5">Thời gian</th>
                                    <th className="px-3 py-2.5 text-right">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-border/40 font-medium">
                                {isLoading ? (
                                    <tr>
                                        <td colSpan="7" className="py-12 text-center text-muted-foreground">
                                            <Loader2 className="size-6 animate-spin mx-auto text-blue-600 mb-2" />
                                            Đang tải lịch sử giao dịch...
                                        </td>
                                    </tr>
                                ) : filteredTransactions.length === 0 ? (
                                    <tr>
                                        <td colSpan="7" className="py-12 text-center text-muted-foreground">
                                            Không có giao dịch nào phù hợp.
                                        </td>
                                    </tr>
                                ) : (
                                    filteredTransactions.map((txn) => {
                                        const remaining = txn.amount - (txn.refundedAmount || 0);
                                        const canRefund =
                                            (txn.status === "SUCCESS" || txn.status === "PARTIALLY_REFUNDED") &&
                                            remaining > 0;

                                        return (
                                            <tr key={txn.id} className="hover:bg-muted/30 transition-colors">
                                                <td className="px-3.5 py-3">
                                                    <div className="font-mono font-bold text-foreground">
                                                        {txn.transactionId}
                                                    </div>
                                                    <div className="text-[11px] text-muted-foreground">
                                                        {txn.orderNumber ? `#${txn.orderNumber}` : "—"}
                                                    </div>
                                                </td>
                                                <td className="px-3 py-3">
                                                    <div className="flex items-center gap-1.5">
                                                        {txn.paymentMethod === "STRIPE" ? (
                                                            <CreditCard className="size-3.5 text-blue-500" />
                                                        ) : (
                                                            <Banknote className="size-3.5 text-emerald-500" />
                                                        )}
                                                        <span>{txn.paymentMethod}</span>
                                                    </div>
                                                </td>
                                                <td className="px-3 py-3">
                                                    <div className="font-bold text-foreground">
                                                        {txn.amount?.toLocaleString("vi-VN")} {txn.currency?.toUpperCase()}
                                                    </div>
                                                    {txn.refundedAmount > 0 && (
                                                        <div className="text-[10px] text-purple-600 dark:text-purple-400">
                                                            Đã hoàn: {txn.refundedAmount?.toLocaleString("vi-VN")}
                                                        </div>
                                                    )}
                                                </td>
                                                <td className="px-3 py-3">
                                                    {txn.cardBrand || txn.cardLast4 ? (
                                                        <span className="font-mono text-[11px] capitalize">
                                                            {txn.cardBrand} •••• {txn.cardLast4}
                                                        </span>
                                                    ) : (
                                                        <span className="text-muted-foreground">—</span>
                                                    )}
                                                </td>
                                                <td className="px-3 py-3">
                                                    {getStatusBadge(txn.status)}
                                                </td>
                                                <td className="px-3 py-3 text-muted-foreground text-[11px]">
                                                    {txn.createdAt ? new Date(txn.createdAt).toLocaleString("vi-VN") : "—"}
                                                </td>
                                                <td className="px-3 py-3 text-right">
                                                    {canRefund && (
                                                        <Button
                                                            size="sm"
                                                            variant="outline"
                                                            onClick={() => handleOpenRefund(txn)}
                                                            className="text-xs h-7 px-2.5 text-purple-600 hover:text-purple-700 hover:bg-purple-50 dark:hover:bg-purple-950/40 border-purple-200 dark:border-purple-800 cursor-pointer"
                                                        >
                                                            <RotateCcw className="size-3 mr-1" />
                                                            Hoàn tiền
                                                        </Button>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </Modal>

            {/* Refund Sub-Modal */}
            {selectedTxnForRefund && (
                <Modal
                    isOpen={!!selectedTxnForRefund}
                    onClose={() => setSelectedTxnForRefund(null)}
                    title="Xác Nhận Hoàn Tiền Giao Dịch"
                    description={`Mã GD: ${selectedTxnForRefund.transactionId} - Đơn #${selectedTxnForRefund.orderNumber}`}
                    maxWidth="max-w-md"
                >
                    <div className="space-y-4">
                        <div className="p-3.5 rounded-xl bg-purple-50 dark:bg-purple-950/30 border border-purple-200 dark:border-purple-800/40 text-xs">
                            <div className="flex justify-between py-0.5">
                                <span className="text-muted-foreground">Số tiền giao dịch ban đầu:</span>
                                <span className="font-bold">
                                    {selectedTxnForRefund.amount?.toLocaleString("vi-VN")} {selectedTxnForRefund.currency?.toUpperCase()}
                                </span>
                            </div>
                            <div className="flex justify-between py-0.5">
                                <span className="text-muted-foreground">Đã hoàn trước đó:</span>
                                <span className="font-bold text-purple-600">
                                    {(selectedTxnForRefund.refundedAmount || 0).toLocaleString("vi-VN")} {selectedTxnForRefund.currency?.toUpperCase()}
                                </span>
                            </div>
                            <div className="flex justify-between py-0.5 border-t border-purple-200/60 dark:border-purple-800/60 pt-1 mt-1">
                                <span className="font-semibold text-foreground">Số tiền tối đa có thể hoàn:</span>
                                <span className="font-black text-sm text-emerald-600 dark:text-emerald-400">
                                    {(selectedTxnForRefund.amount - (selectedTxnForRefund.refundedAmount || 0)).toLocaleString("vi-VN")} {selectedTxnForRefund.currency?.toUpperCase()}
                                </span>
                            </div>
                        </div>

                        <div>
                            <label className="text-xs font-semibold text-muted-foreground block mb-1">
                                Số tiền hoàn đợt này ({selectedTxnForRefund.currency?.toUpperCase()})
                            </label>
                            <Input
                                type="number"
                                value={refundAmount}
                                onChange={(e) => setRefundAmount(e.target.value)}
                                className="font-bold text-sm"
                            />
                        </div>

                        <div>
                            <label className="text-xs font-semibold text-muted-foreground block mb-1">
                                Lý do hoàn tiền
                            </label>
                            <select
                                value={refundReason}
                                onChange={(e) => setRefundReason(e.target.value)}
                                className="w-full text-xs h-9 rounded-md border border-input bg-background px-3 py-1 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                            >
                                <option value="requested_by_customer">Khách hàng yêu cầu trả hàng / đổi ý</option>
                                <option value="duplicate">Giao dịch thanh toán bị trùng</option>
                                <option value="fraudulent">Nghi ngờ gian lận thẻ</option>
                            </select>
                        </div>

                        {refundError && (
                            <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
                                <AlertTriangle className="size-4 shrink-0 text-rose-500" />
                                <span>{refundError}</span>
                            </div>
                        )}

                        <div className="flex gap-2 pt-2">
                            <Button
                                variant="outline"
                                onClick={() => setSelectedTxnForRefund(null)}
                                disabled={isRefunding}
                                className="flex-1 cursor-pointer"
                            >
                                Hủy
                            </Button>
                            <Button
                                onClick={handleConfirmRefund}
                                disabled={isRefunding || !refundAmount || Number(refundAmount) <= 0}
                                className="flex-1 bg-purple-600 hover:bg-purple-700 text-white font-bold cursor-pointer"
                            >
                                {isRefunding ? (
                                    <>
                                        <Loader2 className="size-4 mr-2 animate-spin" />
                                        Đang hoàn tiền...
                                    </>
                                ) : (
                                    "Xác nhận hoàn tiền"
                                )}
                            </Button>
                        </div>
                    </div>
                </Modal>
            )}
        </>
    );
}
