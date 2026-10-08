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
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { inventoryApi, productApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Inventory() {
    const { branch, store } = useAuthStore();
    const [inventories, setInventories] = useState([]);
    const [products, setProducts] = useState([]);
    const [filterLowStockOnly, setFilterLowStockOnly] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    // Stock Adjust Modal
    const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [adjustQuantity, setAdjustQuantity] = useState("");
    const [adjustType, setAdjustType] = useState("ADD"); // "ADD" | "SET"

    const loadInventoryData = async () => {
        if (!branch?.id) return;
        setIsLoading(true);
        try {
            const [invs, prods] = await Promise.all([
                inventoryApi.getByBranch(branch.id).catch(() => []),
                store?.id ? productApi.getByStore(store.id).catch(() => []) : [],
            ]);
            setInventories(invs || []);
            setProducts(prods || []);
        } catch (err) {
            console.error("Lỗi khi tải kho hàng:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadInventoryData();
    }, [branch?.id, store?.id]);

    const handleOpenAdjust = (item) => {
        setSelectedItem(item);
        setAdjustQuantity("");
        setAdjustType("ADD");
        setIsAdjustModalOpen(true);
    };

    const handleSaveAdjust = async (e) => {
        e.preventDefault();
        if (!selectedItem || !branch?.id) return;
        const qtyNum = Number(adjustQuantity);
        if (isNaN(qtyNum)) return;

        try {
            if (adjustType === "ADD") {
                await inventoryApi.adjustStock(branch.id, selectedItem.productId || selectedItem.product?.id, qtyNum);
            } else {
                // Set absolute quantity
                await inventoryApi.update(selectedItem.id, {
                    branchId: branch.id,
                    productId: selectedItem.productId || selectedItem.product?.id,
                    quantity: Math.max(0, qtyNum),
                });
            }
            setIsAdjustModalOpen(false);
            loadInventoryData();
        } catch (err) {
            alert(err.message || "Không thể cập nhật số lượng tồn kho");
        }
    };

    const filtered = inventories.filter((inv) => {
        const prod = inv.product;
        const q = searchQuery.toLowerCase().trim();
        const matchesQuery =
            !q ||
            prod?.name?.toLowerCase().includes(q) ||
            prod?.sku?.toLowerCase().includes(q) ||
            prod?.barcode?.toLowerCase().includes(q);

        const isLowStock = inv.quantity <= (prod?.minStockLevel || 5);
        if (filterLowStockOnly && !isLowStock) return false;

        return matchesQuery;
    });

    const lowStockCount = inventories.filter(
        (i) => i.quantity <= (i.product?.minStockLevel || 5)
    ).length;

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Quản Lý Kho Hàng & Tồn Kho
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Kiểm soát lượng tồn kho thực tế tại {branch ? branch.name : "chi nhánh"}
                    </p>
                </div>
                <div className="flex items-center gap-2.5">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={loadInventoryData}
                        className="gap-1.5 h-9 cursor-pointer"
                    >
                        <RefreshCw className="size-3.5" /> Làm mới
                    </Button>
                </div>
            </div>

            {/* Filter bar */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-4 flex flex-col sm:flex-row items-center justify-between gap-3">
                    <div className="relative flex-1 w-full">
                        <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                        <Input
                            placeholder="Tìm kiếm theo sản phẩm hoặc SKU trong kho..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-9 h-9 text-sm"
                        />
                    </div>
                    <div className="flex items-center gap-2 w-full sm:w-auto">
                        <button
                            onClick={() => setFilterLowStockOnly(!filterLowStockOnly)}
                            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors cursor-pointer ${
                                filterLowStockOnly
                                    ? "bg-amber-500 text-white shadow-xs"
                                    : "bg-secondary text-muted-foreground hover:text-foreground"
                            }`}
                        >
                            <AlertTriangle className="size-3.5" />
                            Chỉ hiện sắp hết hàng ({lowStockCount})
                        </button>
                    </div>
                </CardContent>
            </Card>

            {/* Inventory Table */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-0">
                    {isLoading ? (
                        <div className="py-12 text-center text-sm text-muted-foreground">Đang tải dữ liệu tồn kho...</div>
                    ) : filtered.length === 0 ? (
                        <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                            <Warehouse className="size-10 text-muted-foreground/40" />
                            <span>Không có dữ liệu tồn kho nào khớp.</span>
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
                                        <th className="py-3 px-4">Trạng thái</th>
                                        <th className="py-3 px-4 text-center">Điều chỉnh</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {filtered.map((inv) => {
                                        const prod = inv.product;
                                        const minStock = prod?.minStockLevel || 5;
                                        const isOut = inv.quantity <= 0;
                                        const isLow = inv.quantity <= minStock;

                                        return (
                                            <tr key={inv.id} className="hover:bg-muted/20 transition-colors">
                                                <td className="py-3 px-4 font-semibold text-foreground">
                                                    {prod?.name || "Sản phẩm chưa đặt tên"}
                                                </td>
                                                <td className="py-3 px-4 font-mono text-muted-foreground">
                                                    {prod?.sku}
                                                </td>
                                                <td className="py-3 px-4 text-center">
                                                    <span className={`font-black text-sm ${
                                                        isOut
                                                            ? "text-destructive"
                                                            : isLow
                                                            ? "text-amber-600 dark:text-amber-400"
                                                            : "text-foreground"
                                                    }`}>
                                                        {inv.quantity}
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
                                                        {isOut ? "Hết hàng" : isLow ? "Cần nhập hàng" : "Đủ hàng"}
                                                    </Badge>
                                                </td>
                                                <td className="py-3 px-4 text-center">
                                                    <Button
                                                        variant="outline"
                                                        size="xs"
                                                        onClick={() => handleOpenAdjust(inv)}
                                                        className="gap-1 text-xs cursor-pointer"
                                                    >
                                                        <ArrowUpDown className="size-3" />
                                                        Cập nhật
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

            {/* Adjust Stock Modal */}
            <Modal
                isOpen={isAdjustModalOpen}
                onClose={() => setIsAdjustModalOpen(false)}
                title="Điều chỉnh số lượng tồn kho"
                description={selectedItem?.product?.name}
                maxWidth="max-w-sm"
            >
                <form onSubmit={handleSaveAdjust} className="space-y-4">
                    <div className="flex rounded-lg border border-border p-1 bg-muted/40">
                        <button
                            type="button"
                            onClick={() => setAdjustType("ADD")}
                            className={`flex-1 py-1.5 text-xs font-semibold rounded-md transition-colors cursor-pointer ${
                                adjustType === "ADD" ? "bg-background text-foreground shadow-2xs" : "text-muted-foreground"
                            }`}
                        >
                            Nhập thêm / Xuất bớt (+/-)
                        </button>
                        <button
                            type="button"
                            onClick={() => setAdjustType("SET")}
                            className={`flex-1 py-1.5 text-xs font-semibold rounded-md transition-colors cursor-pointer ${
                                adjustType === "SET" ? "bg-background text-foreground shadow-2xs" : "text-muted-foreground"
                            }`}
                        >
                            Đặt số lượng mới
                        </button>
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">
                            {adjustType === "ADD" ? "Số lượng thay đổi (dương nhập, âm xuất)" : "Số lượng tồn kho thực tế"}
                        </label>
                        <Input
                            type="number"
                            placeholder={adjustType === "ADD" ? "+10 hoặc -5" : "100"}
                            value={adjustQuantity}
                            onChange={(e) => setAdjustQuantity(e.target.value)}
                            required
                        />
                    </div>

                    <div className="p-3 rounded-lg bg-muted/30 text-xs text-muted-foreground flex justify-between">
                        <span>Tồn kho hiện tại:</span>
                        <span className="font-bold text-foreground">{selectedItem?.quantity}</span>
                    </div>

                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white cursor-pointer text-xs">
                        Xác Nhận Cập Nhật
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
