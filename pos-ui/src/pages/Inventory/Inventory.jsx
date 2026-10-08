import React, { useState, useEffect } from "react";
import {
    Warehouse,
    Search,
    AlertTriangle,
    ArrowUpDown,
    CheckCircle2,
    PlusCircle,
    MinusCircle,
    RefreshCw,
    Package,
    History,
    Boxes,
    ShieldAlert,
    ArrowUpRight,
    ArrowDownRight,
    Clock,
    User,
    Check,
    AlertCircle,
    RotateCcw
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { inventoryApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Inventory() {
    const { branch } = useAuthStore();
    const [inventories, setInventories] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [lowStockSummary, setLowStockSummary] = useState(null);
    const [activeTab, setActiveTab] = useState("STOCK"); // "STOCK" | "AUDIT" | "ALERTS"
    const [isLoading, setIsLoading] = useState(true);
    const [isAuditLoading, setIsAuditLoading] = useState(false);

    // Filters for Stock tab
    const [searchQuery, setSearchQuery] = useState("");
    const [stockStatusFilter, setStockStatusFilter] = useState("ALL"); // ALL | IN_STOCK | LOW_STOCK | OUT_OF_STOCK

    // Filters for Audit tab
    const [auditSearch, setAuditSearch] = useState("");
    const [auditTypeFilter, setAuditTypeFilter] = useState("ALL"); // ALL | SALE | PURCHASE | ADJUSTMENT | DAMAGE | RETURN

    // Stock Adjust Modal
    const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [adjustType, setAdjustType] = useState("INCREASE"); // "INCREASE" | "DECREASE" | "SET"
    const [adjustQuantity, setAdjustQuantity] = useState("");
    const [adjustReason, setAdjustReason] = useState("AUDIT_COUNT");
    const [adjustNotes, setAdjustNotes] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    const loadInventoryData = async () => {
        if (!branch?.id) return;
        setIsLoading(true);
        setErrorMsg("");
        try {
            const [invs, summary] = await Promise.all([
                inventoryApi.getByBranch(branch.id).catch(() => []),
                inventoryApi.getLowStockSummary(branch.id).catch(() => null),
            ]);
            setInventories(invs || []);
            setLowStockSummary(summary);
        } catch (err) {
            console.error("Lỗi khi tải kho hàng:", err);
            setErrorMsg("Không thể tải danh sách tồn kho.");
        } finally {
            setIsLoading(false);
        }
    };

    const loadTransactions = async () => {
        if (!branch?.id) return;
        setIsAuditLoading(true);
        try {
            const res = await inventoryApi.getTransactions(branch.id, 0, 50).catch(() => []);
            const list = Array.isArray(res) ? res : res.content || [];
            setTransactions(list);
        } catch (err) {
            console.error("Lỗi khi tải lịch sử biến động kho:", err);
        } finally {
            setIsAuditLoading(false);
        }
    };

    useEffect(() => {
        loadInventoryData();
        loadTransactions();
    }, [branch?.id]);

    const handleOpenAdjust = (item, defaultType = "INCREASE", defaultReason = "AUDIT_COUNT") => {
        setSelectedItem(item);
        setAdjustQuantity("");
        setAdjustType(defaultType);
        setAdjustReason(defaultReason);
        setAdjustNotes("");
        setSuccessMsg("");
        setErrorMsg("");
        setIsAdjustModalOpen(true);
    };

    const handleSaveAdjust = async (e) => {
        e.preventDefault();
        if (!selectedItem || !branch?.id) return;
        const qtyNum = parseInt(adjustQuantity, 10);
        if (isNaN(qtyNum) || qtyNum <= 0) {
            alert("Vui lòng nhập số lượng hợp lệ lớn hơn 0");
            return;
        }

        setIsSubmitting(true);
        try {
            const payload = {
                branchId: branch.id,
                productId: selectedItem.productId || selectedItem.product?.id,
                adjustmentType: adjustType,
                quantity: qtyNum,
                reason: adjustReason,
                notes: adjustNotes || `Điều chỉnh kho qua POS Dashboard`,
            };

            await inventoryApi.adjustWithAudit(payload);
            setSuccessMsg("Cập nhật và ghi sổ biến động kho thành công!");
            setTimeout(() => {
                setIsAdjustModalOpen(false);
                setSuccessMsg("");
            }, 800);

            // Reload data
            loadInventoryData();
            loadTransactions();
        } catch (err) {
            alert(err.response?.data?.message || err.message || "Không thể cập nhật tồn kho");
        } finally {
            setIsSubmitting(false);
        }
    };

    // Calculate metrics
    const totalSKUs = inventories.length;
    const totalUnits = inventories.reduce((sum, item) => sum + (item.quantity || 0), 0);
    const lowStockItems = inventories.filter(
        (i) => (i.quantity || 0) <= (i.product?.minStockLevel || 5) && (i.quantity || 0) > 0
    );
    const outOfStockItems = inventories.filter((i) => (i.quantity || 0) <= 0);

    // Filtered inventory list
    const filteredInventories = inventories.filter((inv) => {
        const prod = inv.product;
        const q = searchQuery.toLowerCase().trim();
        const matchesQuery =
            !q ||
            prod?.name?.toLowerCase().includes(q) ||
            prod?.sku?.toLowerCase().includes(q) ||
            prod?.barcode?.toLowerCase().includes(q);

        const minStock = prod?.minStockLevel || 5;
        const qty = inv.quantity || 0;

        if (stockStatusFilter === "IN_STOCK" && qty <= minStock) return false;
        if (stockStatusFilter === "LOW_STOCK" && (qty > minStock || qty <= 0)) return false;
        if (stockStatusFilter === "OUT_OF_STOCK" && qty > 0) return false;

        return matchesQuery;
    });

    // Filtered transactions list
    const filteredTransactions = transactions.filter((tx) => {
        const q = auditSearch.toLowerCase().trim();
        const matchesQuery =
            !q ||
            tx.productName?.toLowerCase().includes(q) ||
            tx.productSku?.toLowerCase().includes(q) ||
            tx.notes?.toLowerCase().includes(q) ||
            tx.performedBy?.toLowerCase().includes(q);

        if (auditTypeFilter !== "ALL" && tx.type !== auditTypeFilter) return false;

        return matchesQuery;
    });

    // Calculate projected balance in adjust modal
    const calculateProjectedBalance = () => {
        if (!selectedItem) return 0;
        const cur = selectedItem.quantity || 0;
        const qty = parseInt(adjustQuantity, 10) || 0;
        if (adjustType === "INCREASE") return cur + qty;
        if (adjustType === "DECREASE") return Math.max(0, cur - qty);
        if (adjustType === "SET") return qty;
        return cur;
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
                        <Warehouse className="size-6 text-blue-600" />
                        Quản Lý Kho Hàng & Tồn Kho (Inventory)
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Kiểm soát tồn kho theo thời gian thực, điều chỉnh kiểm kê và truy vết lịch sử tại{" "}
                        <span className="font-semibold text-foreground">{branch ? branch.name : "chi nhánh"}</span>
                    </p>
                </div>
                <div className="flex items-center gap-2.5">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                            loadInventoryData();
                            loadTransactions();
                        }}
                        className="gap-1.5 h-9 cursor-pointer"
                    >
                        <RefreshCw className={`size-3.5 ${isLoading || isAuditLoading ? "animate-spin" : ""}`} /> Làm mới dữ liệu
                    </Button>
                </div>
            </div>

            {/* KPI Metrics Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card className="border-border/60 bg-card/60 backdrop-blur-xs">
                    <CardContent className="p-4 flex items-center justify-between">
                        <div>
                            <p className="text-xs font-medium text-muted-foreground">Tổng Mặt Hàng (SKUs)</p>
                            <h3 className="text-2xl font-black text-foreground mt-1">{totalSKUs}</h3>
                            <p className="text-[11px] text-muted-foreground mt-0.5">Mã hàng quản lý tại kho</p>
                        </div>
                        <div className="size-11 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center">
                            <Boxes className="size-5" />
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-border/60 bg-card/60 backdrop-blur-xs">
                    <CardContent className="p-4 flex items-center justify-between">
                        <div>
                            <p className="text-xs font-medium text-muted-foreground">Tổng Lượng Tồn Kho</p>
                            <h3 className="text-2xl font-black text-emerald-600 dark:text-emerald-400 mt-1">
                                {totalUnits.toLocaleString("vi-VN")}
                            </h3>
                            <p className="text-[11px] text-muted-foreground mt-0.5">Đơn vị sản phẩm có sẵn</p>
                        </div>
                        <div className="size-11 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
                            <Package className="size-5" />
                        </div>
                    </CardContent>
                </Card>

                <Card
                    className="border-border/60 bg-card/60 backdrop-blur-xs cursor-pointer hover:border-amber-500/40 transition-colors"
                    onClick={() => setActiveTab("ALERTS")}
                >
                    <CardContent className="p-4 flex items-center justify-between">
                        <div>
                            <p className="text-xs font-medium text-muted-foreground">Sắp Hết Hàng (Low Stock)</p>
                            <h3 className="text-2xl font-black text-amber-600 dark:text-amber-400 mt-1">
                                {lowStockItems.length}
                            </h3>
                            <p className="text-[11px] text-muted-foreground mt-0.5">Chạm mức cảnh báo tối thiểu</p>
                        </div>
                        <div className="size-11 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center">
                            <AlertTriangle className="size-5" />
                        </div>
                    </CardContent>
                </Card>

                <Card
                    className="border-border/60 bg-card/60 backdrop-blur-xs cursor-pointer hover:border-destructive/40 transition-colors"
                    onClick={() => {
                        setActiveTab("STOCK");
                        setStockStatusFilter("OUT_OF_STOCK");
                    }}
                >
                    <CardContent className="p-4 flex items-center justify-between">
                        <div>
                            <p className="text-xs font-medium text-muted-foreground">Đã Hết Hàng (Out of Stock)</p>
                            <h3 className="text-2xl font-black text-destructive mt-1">
                                {outOfStockItems.length}
                            </h3>
                            <p className="text-[11px] text-muted-foreground mt-0.5">Cần nhập hàng khẩn cấp</p>
                        </div>
                        <div className="size-11 rounded-xl bg-destructive/10 text-destructive flex items-center justify-center">
                            <ShieldAlert className="size-5" />
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Navigation Tabs */}
            <div className="flex border-b border-border/60 gap-4">
                <button
                    onClick={() => setActiveTab("STOCK")}
                    className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer ${
                        activeTab === "STOCK"
                            ? "border-blue-600 text-blue-600 dark:text-blue-400"
                            : "border-transparent text-muted-foreground hover:text-foreground"
                    }`}
                >
                    <Warehouse className="size-4" />
                    Tồn Kho Thực Tế ({inventories.length})
                </button>
                <button
                    onClick={() => {
                        setActiveTab("AUDIT");
                        loadTransactions();
                    }}
                    className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer ${
                        activeTab === "AUDIT"
                            ? "border-blue-600 text-blue-600 dark:text-blue-400"
                            : "border-transparent text-muted-foreground hover:text-foreground"
                    }`}
                >
                    <History className="size-4" />
                    Lịch Sử Biến Động Kho (Audit Trail)
                </button>
                <button
                    onClick={() => setActiveTab("ALERTS")}
                    className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer ${
                        activeTab === "ALERTS"
                            ? "border-amber-500 text-amber-600 dark:text-amber-400"
                            : "border-transparent text-muted-foreground hover:text-foreground"
                    }`}
                >
                    <AlertTriangle className="size-4" />
                    Cảnh Báo Hàng Tồn Thấp ({lowStockItems.length + outOfStockItems.length})
                </button>
            </div>

            {/* TAB 1: TỒN KHO THỰC TẾ */}
            {activeTab === "STOCK" && (
                <div className="space-y-4">
                    {/* Filter Bar */}
                    <Card className="border-border/60 bg-card/60">
                        <CardContent className="p-4 flex flex-col sm:flex-row items-center justify-between gap-3">
                            <div className="relative flex-1 w-full">
                                <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                <Input
                                    placeholder="Tìm kiếm theo tên sản phẩm, mã SKU hoặc Barcode..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="pl-9 h-9 text-sm"
                                />
                            </div>
                            <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
                                {[
                                    { id: "ALL", label: `Tất cả (${inventories.length})` },
                                    { id: "IN_STOCK", label: "Đủ hàng" },
                                    { id: "LOW_STOCK", label: `Sắp hết (${lowStockItems.length})` },
                                    { id: "OUT_OF_STOCK", label: `Hết hàng (${outOfStockItems.length})` },
                                ].map((tab) => (
                                    <button
                                        key={tab.id}
                                        onClick={() => setStockStatusFilter(tab.id)}
                                        className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-colors cursor-pointer ${
                                            stockStatusFilter === tab.id
                                                ? "bg-primary text-primary-foreground shadow-2xs font-bold"
                                                : "bg-secondary text-muted-foreground hover:text-foreground"
                                        }`}
                                    >
                                        {tab.label}
                                    </button>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Stock Table */}
                    <Card className="border-border/60 bg-card/60">
                        <CardContent className="p-0">
                            {isLoading ? (
                                <div className="py-12 text-center text-sm text-muted-foreground">Đang tải dữ liệu tồn kho...</div>
                            ) : filteredInventories.length === 0 ? (
                                <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                                    <Warehouse className="size-10 text-muted-foreground/40" />
                                    <span>Không có sản phẩm nào khớp với bộ lọc tồn kho.</span>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-left text-xs border-collapse">
                                        <thead>
                                            <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                                <th className="py-3 px-4">Sản phẩm</th>
                                                <th className="py-3 px-4">Mã SKU / Barcode</th>
                                                <th className="py-3 px-4 text-center">Tồn kho hiện tại</th>
                                                <th className="py-3 px-4 text-center">Ngưỡng tối thiểu</th>
                                                <th className="py-3 px-4">Trạng thái kho</th>
                                                <th className="py-3 px-4 text-center">Thao tác kiểm kê</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-border/40">
                                            {filteredInventories.map((inv) => {
                                                const prod = inv.product;
                                                const minStock = prod?.minStockLevel || 5;
                                                const qty = inv.quantity || 0;
                                                const isOut = qty <= 0;
                                                const isLow = qty <= minStock;

                                                return (
                                                    <tr key={inv.id} className="hover:bg-muted/20 transition-colors">
                                                        <td className="py-3 px-4">
                                                            <div className="font-semibold text-foreground text-sm">
                                                                {prod?.name || "Chưa đặt tên"}
                                                            </div>
                                                            <div className="text-[11px] text-muted-foreground">
                                                                {prod?.category?.name || "Chung"}
                                                            </div>
                                                        </td>
                                                        <td className="py-3 px-4 font-mono text-muted-foreground">
                                                            <div>{prod?.sku || "-"}</div>
                                                            {prod?.barcode && (
                                                                <div className="text-[10px] text-muted-foreground/80">{prod.barcode}</div>
                                                            )}
                                                        </td>
                                                        <td className="py-3 px-4 text-center">
                                                            <span className={`inline-block px-2.5 py-1 rounded-md font-black text-sm ${
                                                                isOut
                                                                    ? "bg-destructive/15 text-destructive"
                                                                    : isLow
                                                                    ? "bg-amber-500/15 text-amber-600 dark:text-amber-400"
                                                                    : "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                                                            }`}>
                                                                {qty}
                                                            </span>
                                                        </td>
                                                        <td className="py-3 px-4 text-center text-muted-foreground font-medium">
                                                            {minStock}
                                                        </td>
                                                        <td className="py-3 px-4">
                                                            <Badge
                                                                variant={isOut ? "destructive" : isLow ? "warning" : "success"}
                                                                className="text-[11px]"
                                                            >
                                                                {isOut ? "Hết hàng" : isLow ? "Sắp hết hàng" : "Đủ hàng"}
                                                            </Badge>
                                                        </td>
                                                        <td className="py-3 px-4 text-center">
                                                            <Button
                                                                variant="outline"
                                                                size="xs"
                                                                onClick={() => handleOpenAdjust(inv, "INCREASE", "RESTOCK")}
                                                                className="gap-1 text-xs cursor-pointer hover:border-blue-500"
                                                            >
                                                                <ArrowUpDown className="size-3" />
                                                                Điều chỉnh kho
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            )}

            {/* TAB 2: LỊCH SỬ BIẾN ĐỘNG KHO (AUDIT TRAIL) */}
            {activeTab === "AUDIT" && (
                <div className="space-y-4">
                    {/* Search & Type Filter Bar */}
                    <Card className="border-border/60 bg-card/60">
                        <CardContent className="p-4 flex flex-col sm:flex-row items-center justify-between gap-3">
                            <div className="relative flex-1 w-full">
                                <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                                <Input
                                    placeholder="Tìm theo tên sản phẩm, mã SKU, ghi chú hoặc người thực hiện..."
                                    value={auditSearch}
                                    onChange={(e) => setAuditSearch(e.target.value)}
                                    className="pl-9 h-9 text-sm"
                                />
                            </div>
                            <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
                                {[
                                    { id: "ALL", label: "Tất cả giao dịch" },
                                    { id: "SALE", label: "Bán hàng (SALE)" },
                                    { id: "ADJUSTMENT", label: "Điều chỉnh (ADJUSTMENT)" },
                                    { id: "PURCHASE", label: "Nhập hàng (PURCHASE)" },
                                    { id: "DAMAGE", label: "Hư hỏng (DAMAGE)" },
                                    { id: "RETURN", label: "Hoàn trả (RETURN)" },
                                ].map((filter) => (
                                    <button
                                        key={filter.id}
                                        onClick={() => setAuditTypeFilter(filter.id)}
                                        className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-colors cursor-pointer ${
                                            auditTypeFilter === filter.id
                                                ? "bg-primary text-primary-foreground shadow-2xs font-bold"
                                                : "bg-secondary text-muted-foreground hover:text-foreground"
                                        }`}
                                    >
                                        {filter.label}
                                    </button>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Audit Transactions Table */}
                    <Card className="border-border/60 bg-card/60">
                        <CardContent className="p-0">
                            {isAuditLoading ? (
                                <div className="py-12 text-center text-sm text-muted-foreground">Đang tải lịch sử giao dịch kho...</div>
                            ) : filteredTransactions.length === 0 ? (
                                <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                                    <History className="size-10 text-muted-foreground/40" />
                                    <span>Chưa có dữ liệu biến động kho nào ghi nhận.</span>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-left text-xs border-collapse">
                                        <thead>
                                            <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                                <th className="py-3 px-4">Thời gian</th>
                                                <th className="py-3 px-4">Sản phẩm & SKU</th>
                                                <th className="py-3 px-4">Loại giao dịch</th>
                                                <th className="py-3 px-4 text-center">Biến động (+/-)</th>
                                                <th className="py-3 px-4 text-center">Tồn Trước ➔ Sau</th>
                                                <th className="py-3 px-4">Lý do & Ghi chú</th>
                                                <th className="py-3 px-4">Người thực hiện</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-border/40">
                                            {filteredTransactions.map((tx) => {
                                                const isPositive = (tx.quantityChange || 0) > 0;
                                                const isZero = (tx.quantityChange || 0) === 0;

                                                const getBadgeVariant = (type) => {
                                                    switch (type) {
                                                        case "SALE":
                                                            return "bg-blue-500/10 text-blue-600 border-blue-500/20";
                                                        case "PURCHASE":
                                                            return "bg-emerald-500/10 text-emerald-600 border-emerald-500/20";
                                                        case "ADJUSTMENT":
                                                            return "bg-purple-500/10 text-purple-600 border-purple-500/20";
                                                        case "DAMAGE":
                                                            return "bg-destructive/10 text-destructive border-destructive/20";
                                                        case "RETURN":
                                                            return "bg-amber-500/10 text-amber-600 border-amber-500/20";
                                                        default:
                                                            return "bg-secondary text-secondary-foreground";
                                                    }
                                                };

                                                const getTypeLabel = (type) => {
                                                    switch (type) {
                                                        case "SALE":
                                                            return "Bán hàng";
                                                        case "PURCHASE":
                                                            return "Nhập hàng";
                                                        case "ADJUSTMENT":
                                                            return "Điều chỉnh";
                                                        case "DAMAGE":
                                                            return "Hư hỏng";
                                                        case "RETURN":
                                                            return "Hoàn trả";
                                                        default:
                                                            return type;
                                                    }
                                                };

                                                const formattedTime = tx.createdAt
                                                    ? new Date(tx.createdAt).toLocaleString("vi-VN", {
                                                          day: "2-digit",
                                                          month: "2-digit",
                                                          year: "numeric",
                                                          hour: "2-digit",
                                                          minute: "2-digit",
                                                      })
                                                    : "-";

                                                return (
                                                    <tr key={tx.id} className="hover:bg-muted/20 transition-colors">
                                                        <td className="py-3 px-4 text-muted-foreground whitespace-nowrap font-mono text-[11px]">
                                                            <div className="flex items-center gap-1.5">
                                                                <Clock className="size-3 text-muted-foreground/60" />
                                                                {formattedTime}
                                                            </div>
                                                        </td>
                                                        <td className="py-3 px-4">
                                                            <div className="font-semibold text-foreground">
                                                                {tx.productName || "Sản phẩm #" + tx.productId}
                                                            </div>
                                                            <div className="text-[11px] font-mono text-muted-foreground">
                                                                {tx.productSku || "-"}
                                                            </div>
                                                        </td>
                                                        <td className="py-3 px-4">
                                                            <span
                                                                className={`inline-block px-2 py-0.5 rounded text-[10px] font-bold border ${getBadgeVariant(
                                                                    tx.type
                                                                )}`}
                                                            >
                                                                {getTypeLabel(tx.type)}
                                                            </span>
                                                        </td>
                                                        <td className="py-3 px-4 text-center font-bold">
                                                            <span
                                                                className={`inline-flex items-center gap-0.5 text-xs font-mono font-bold ${
                                                                    isZero
                                                                        ? "text-muted-foreground"
                                                                        : isPositive
                                                                        ? "text-emerald-600 dark:text-emerald-400"
                                                                        : "text-destructive"
                                                                }`}
                                                            >
                                                                {isPositive ? (
                                                                    <ArrowUpRight className="size-3" />
                                                                ) : isZero ? null : (
                                                                    <ArrowDownRight className="size-3" />
                                                                )}
                                                                {isPositive ? `+${tx.quantityChange}` : tx.quantityChange}
                                                            </span>
                                                        </td>
                                                        <td className="py-3 px-4 text-center font-mono text-[11px] text-muted-foreground">
                                                            <span className="font-medium text-foreground">{tx.balanceBefore}</span>
                                                            <span className="mx-1.5 text-muted-foreground/50">➔</span>
                                                            <span className="font-bold text-foreground">{tx.balanceAfter}</span>
                                                        </td>
                                                        <td className="py-3 px-4">
                                                            <div className="font-medium text-foreground">{tx.reason || "-"}</div>
                                                            {tx.notes && (
                                                                <div className="text-[11px] text-muted-foreground line-clamp-1 italic">
                                                                    {tx.notes}
                                                                </div>
                                                            )}
                                                        </td>
                                                        <td className="py-3 px-4 text-muted-foreground text-[11px]">
                                                            <div className="flex items-center gap-1">
                                                                <User className="size-3 text-muted-foreground/60" />
                                                                <span>{tx.performedBy || "Hệ thống"}</span>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            )}

            {/* TAB 3: TRUNG TÂM CẢNH BÁO HÀNG TỒN THẤP (LOW STOCK ALERTS) */}
            {activeTab === "ALERTS" && (
                <div className="space-y-4">
                    <Card className="border-amber-500/30 bg-amber-500/5">
                        <CardContent className="p-4 flex items-start gap-3">
                            <AlertTriangle className="size-5 text-amber-500 shrink-0 mt-0.5" />
                            <div>
                                <h4 className="font-bold text-sm text-foreground">
                                    Cảnh báo lượng tồn kho dưới ngưỡng an toàn ({lowStockItems.length + outOfStockItems.length} mặt hàng)
                                </h4>
                                <p className="text-xs text-muted-foreground mt-0.5">
                                    Các mặt hàng dưới đây đã chạm hoặc vượt quá mức tồn tối thiểu quy định. Vui lòng tiến hành nhập hàng hoặc điều chỉnh kho để tránh gián đoạn bán hàng tại quầy POS.
                                </p>
                            </div>
                        </CardContent>
                    </Card>

                    <Card className="border-border/60 bg-card/60">
                        <CardContent className="p-0">
                            {lowStockItems.length === 0 && outOfStockItems.length === 0 ? (
                                <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                                    <CheckCircle2 className="size-10 text-emerald-500" />
                                    <span className="font-semibold text-foreground">Tất cả sản phẩm đều đủ tồn kho an toàn!</span>
                                    <span className="text-xs">Không có mặt hàng nào cần nhập bổ sung tại chi nhánh.</span>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full text-left text-xs border-collapse">
                                        <thead>
                                            <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                                <th className="py-3 px-4">Sản phẩm</th>
                                                <th className="py-3 px-4">Mã SKU</th>
                                                <th className="py-3 px-4 text-center">Tồn kho hiện tại</th>
                                                <th className="py-3 px-4 text-center">Mức tối thiểu</th>
                                                <th className="py-3 px-4 text-center">Lượng thiếu hụt</th>
                                                <th className="py-3 px-4">Mức độ khẩn cấp</th>
                                                <th className="py-3 px-4 text-center">Hành động khắc phục</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-border/40">
                                            {[...outOfStockItems, ...lowStockItems].map((inv) => {
                                                const prod = inv.product;
                                                const minStock = prod?.minStockLevel || 5;
                                                const qty = inv.quantity || 0;
                                                const deficit = Math.max(0, minStock - qty);
                                                const isOut = qty <= 0;

                                                return (
                                                    <tr key={inv.id} className="hover:bg-muted/20 transition-colors">
                                                        <td className="py-3 px-4 font-semibold text-foreground">
                                                            {prod?.name || "Chưa đặt tên"}
                                                        </td>
                                                        <td className="py-3 px-4 font-mono text-muted-foreground">
                                                            {prod?.sku || "-"}
                                                        </td>
                                                        <td className="py-3 px-4 text-center">
                                                            <span className={`font-black text-sm ${isOut ? "text-destructive" : "text-amber-600 dark:text-amber-400"}`}>
                                                                {qty}
                                                            </span>
                                                        </td>
                                                        <td className="py-3 px-4 text-center font-medium text-muted-foreground">
                                                            {minStock}
                                                        </td>
                                                        <td className="py-3 px-4 text-center font-bold text-destructive">
                                                            +{deficit + 10} cái (gợi ý nhập)
                                                        </td>
                                                        <td className="py-3 px-4">
                                                            <Badge variant={isOut ? "destructive" : "warning"} className="text-[10px]">
                                                                {isOut ? "HẾT HÀNG - KHẨN CẤP" : "CẦN NHẬP BỔ SUNG"}
                                                            </Badge>
                                                        </td>
                                                        <td className="py-3 px-4 text-center">
                                                            <Button
                                                                size="xs"
                                                                onClick={() => handleOpenAdjust(inv, "INCREASE", "RESTOCK")}
                                                                className="bg-blue-600 hover:bg-blue-700 text-white gap-1 cursor-pointer"
                                                            >
                                                                <PlusCircle className="size-3" />
                                                                Nhập hàng ngay
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            )}

            {/* STOCK ADJUSTMENT & AUDIT MODAL */}
            <Modal
                isOpen={isAdjustModalOpen}
                onClose={() => setIsAdjustModalOpen(false)}
                title="Điều Chỉnh Tồn Kho & Ghi Sổ Biến Động"
                description={selectedItem ? `${selectedItem.product?.name} (SKU: ${selectedItem.product?.sku})` : ""}
                maxWidth="max-w-md"
            >
                <form onSubmit={handleSaveAdjust} className="space-y-4">
                    {/* Mode selector: INCREASE / DECREASE / SET */}
                    <div>
                        <label className="text-xs font-semibold text-foreground mb-1.5 block">
                            Hình thức điều chỉnh
                        </label>
                        <div className="grid grid-cols-3 gap-2">
                            <button
                                type="button"
                                onClick={() => setAdjustType("INCREASE")}
                                className={`p-2 rounded-lg border text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer ${
                                    adjustType === "INCREASE"
                                        ? "bg-emerald-600 text-white border-emerald-600 shadow-xs"
                                        : "bg-secondary text-muted-foreground hover:text-foreground"
                                }`}
                            >
                                <PlusCircle className="size-3.5" />
                                Nhập thêm (+)
                            </button>
                            <button
                                type="button"
                                onClick={() => setAdjustType("DECREASE")}
                                className={`p-2 rounded-lg border text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer ${
                                    adjustType === "DECREASE"
                                        ? "bg-destructive text-white border-destructive shadow-xs"
                                        : "bg-secondary text-muted-foreground hover:text-foreground"
                                }`}
                            >
                                <MinusCircle className="size-3.5" />
                                Xuất bớt (-)
                            </button>
                            <button
                                type="button"
                                onClick={() => setAdjustType("SET")}
                                className={`p-2 rounded-lg border text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer ${
                                    adjustType === "SET"
                                        ? "bg-blue-600 text-white border-blue-600 shadow-xs"
                                        : "bg-secondary text-muted-foreground hover:text-foreground"
                                }`}
                            >
                                <RotateCcw className="size-3.5" />
                                Đặt lại (=)
                            </button>
                        </div>
                    </div>

                    {/* Quantity Input */}
                    <div className="space-y-1.5">
                        <label className="text-xs font-semibold text-foreground">
                            {adjustType === "SET" ? "Số lượng tồn kho thực tế sau kiểm kê" : "Số lượng thay đổi (Số dương)"}
                        </label>
                        <Input
                            type="number"
                            min="1"
                            placeholder="Ví dụ: 10"
                            value={adjustQuantity}
                            onChange={(e) => setAdjustQuantity(e.target.value)}
                            required
                            className="h-10"
                        />
                    </div>

                    {/* Balance Preview Card */}
                    <div className="p-3 rounded-lg bg-muted/40 border border-border/60 text-xs space-y-1.5">
                        <div className="flex justify-between text-muted-foreground">
                            <span>Tồn kho hiện tại:</span>
                            <span className="font-bold text-foreground">{selectedItem?.quantity || 0} cái</span>
                        </div>
                        <div className="flex justify-between items-center pt-1 border-t border-border/40">
                            <span className="font-semibold text-foreground">Tồn kho sau điều chỉnh dự kiến:</span>
                            <span className="font-black text-sm text-blue-600 dark:text-blue-400">
                                {calculateProjectedBalance()} cái
                            </span>
                        </div>
                    </div>

                    {/* Adjustment Reason */}
                    <div className="space-y-1.5">
                        <label className="text-xs font-semibold text-foreground">Lý do điều chỉnh</label>
                        <select
                            value={adjustReason}
                            onChange={(e) => setAdjustReason(e.target.value)}
                            className="w-full h-10 px-3 rounded-lg border border-border bg-background text-xs font-medium focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                        >
                            <option value="AUDIT_COUNT">Kiểm kê định kỳ (AUDIT_COUNT)</option>
                            <option value="RESTOCK">Nhập bổ sung hàng hóa (RESTOCK)</option>
                            <option value="DAMAGED">Hàng bị hư hỏng / bể vỡ (DAMAGED)</option>
                            <option value="EXPIRED">Hàng hết hạn sử dụng (EXPIRED)</option>
                            <option value="CORRECTION">Sai lệch số liệu / Sửa lỗi kho (CORRECTION)</option>
                        </select>
                    </div>

                    {/* Audit Notes */}
                    <div className="space-y-1.5">
                        <label className="text-xs font-semibold text-foreground">Ghi chú kiểm kê / Audit Trail</label>
                        <textarea
                            rows={3}
                            placeholder="Nhập ghi chú chi tiết hoặc mã biên bản kiểm kê..."
                            value={adjustNotes}
                            onChange={(e) => setAdjustNotes(e.target.value)}
                            className="w-full p-2.5 rounded-lg border border-border bg-background text-xs resize-none focus:ring-2 focus:ring-blue-500 focus:outline-hidden"
                        />
                    </div>

                    {/* Status Messages */}
                    {successMsg && (
                        <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-600 text-xs font-semibold flex items-center gap-2">
                            <Check className="size-4" /> {successMsg}
                        </div>
                    )}
                    {errorMsg && (
                        <div className="p-2.5 rounded-lg bg-destructive/10 text-destructive text-xs font-semibold flex items-center gap-2">
                            <AlertCircle className="size-4" /> {errorMsg}
                        </div>
                    )}

                    {/* Action buttons */}
                    <div className="flex items-center justify-end gap-2 pt-2 border-t border-border/60">
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => setIsAdjustModalOpen(false)}
                            disabled={isSubmitting}
                            className="cursor-pointer text-xs"
                        >
                            Hủy bỏ
                        </Button>
                        <Button
                            type="submit"
                            size="sm"
                            disabled={isSubmitting || !adjustQuantity}
                            className="bg-blue-600 hover:bg-blue-700 text-white font-bold cursor-pointer text-xs gap-1.5"
                        >
                            {isSubmitting ? (
                                <RefreshCw className="size-3.5 animate-spin" />
                            ) : (
                                <Check className="size-3.5" />
                            )}
                            Xác Nhận & Ghi Sổ
                        </Button>
                    </div>
                </form>
            </Modal>
        </div>
    );
}
