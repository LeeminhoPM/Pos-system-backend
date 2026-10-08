import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
    DollarSign,
    ShoppingCart,
    TrendingUp,
    AlertTriangle,
    Package,
    ArrowUpRight,
    Users,
    Clock,
    Plus,
    Receipt,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { orderApi, inventoryApi, productApi } from "@/services/api";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export default function Dashboard() {
    const { branch, store } = useAuthStore();
    const [todayOrders, setTodayOrders] = useState([]);
    const [recentOrders, setRecentOrders] = useState([]);
    const [lowStockItems, setLowStockItems] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!branch?.id) return;
        const fetchDashboardData = async () => {
            setIsLoading(true);
            try {
                const [today, recent, lowStock] = await Promise.all([
                    orderApi.getToday(branch.id).catch(() => []),
                    orderApi.getRecent(branch.id).catch(() => []),
                    inventoryApi.getLowStock(branch.id).catch(() => []),
                ]);
                setTodayOrders(today || []);
                setRecentOrders(recent || []);
                setLowStockItems(lowStock || []);
            } catch (err) {
                console.error("Lỗi khi tải dữ liệu dashboard:", err);
            } finally {
                setIsLoading(false);
            }
        };

        fetchDashboardData();
    }, [branch?.id]);

    const todayRevenue = todayOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
    const avgOrderValue = todayOrders.length > 0 ? Math.round(todayRevenue / todayOrders.length) : 0;

    return (
        <div className="space-y-6">
            {/* Top Bar Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Tổng Quan Hoạt Động
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        {branch ? `${branch.name} • ${store?.branch || "SkyPOS"}` : "Chào mừng trở lại"}
                    </p>
                </div>
                <div className="flex items-center gap-2.5">
                    <Link to="/shifts">
                        <Button variant="outline" size="sm" className="gap-1.5 h-9 cursor-pointer">
                            <Clock className="size-4 text-blue-500" />
                            Xem Ca Làm
                        </Button>
                    </Link>
                    <Link to="/pos">
                        <Button size="sm" className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 h-9 font-medium shadow-md shadow-blue-500/20 cursor-pointer">
                            <ShoppingCart className="size-4" />
                            Mở POS Bán Hàng
                        </Button>
                    </Link>
                </div>
            </div>

            {/* KPI Metric Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card className="border-border/60 bg-card/60 backdrop-blur-xs relative overflow-hidden">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                        <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Doanh thu hôm nay
                        </CardTitle>
                        <div className="size-8 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
                            <DollarSign className="size-4" />
                        </div>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-foreground">
                            {todayRevenue.toLocaleString("vi-VN")} ₫
                        </div>
                        <p className="text-xs text-muted-foreground mt-1 flex items-center gap-1">
                            <span className="text-emerald-600 dark:text-emerald-400 font-semibold inline-flex items-center">
                                <TrendingUp className="size-3 mr-0.5" /> {todayOrders.length}
                            </span>
                            đơn hoàn thành trong ngày
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-border/60 bg-card/60 backdrop-blur-xs relative overflow-hidden">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                        <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Số đơn hàng hôm nay
                        </CardTitle>
                        <div className="size-8 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400 flex items-center justify-center">
                            <Receipt className="size-4" />
                        </div>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-foreground">
                            {todayOrders.length}
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                            Cập nhật thời gian thực
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-border/60 bg-card/60 backdrop-blur-xs relative overflow-hidden">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                        <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Giá trị đơn TB
                        </CardTitle>
                        <div className="size-8 rounded-lg bg-violet-500/10 text-violet-600 dark:text-violet-400 flex items-center justify-center">
                            <TrendingUp className="size-4" />
                        </div>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-foreground">
                            {avgOrderValue.toLocaleString("vi-VN")} ₫
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                            Doanh thu / tổng đơn
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-border/60 bg-card/60 backdrop-blur-xs relative overflow-hidden">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                        <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Cảnh báo hết hàng
                        </CardTitle>
                        <div className="size-8 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400 flex items-center justify-center">
                            <AlertTriangle className="size-4" />
                        </div>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-foreground">
                            {lowStockItems.length}
                        </div>
                        <p className="text-xs text-amber-600 dark:text-amber-400 font-medium mt-1">
                            <Link to="/inventory" className="hover:underline flex items-center gap-0.5">
                                Xem danh sách cần nhập hàng <ArrowUpRight className="size-3" />
                            </Link>
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Recent Orders - 2 Cols */}
                <Card className="lg:col-span-2 border-border/60 bg-card/60">
                    <CardHeader className="flex flex-row items-center justify-between pb-3">
                        <div>
                            <CardTitle className="text-base font-semibold">Đơn hàng gần đây</CardTitle>
                            <CardDescription>Các giao dịch phát sinh gần nhất tại chi nhánh</CardDescription>
                        </div>
                        <Link to="/orders">
                            <Button variant="ghost" size="sm" className="text-xs gap-1 cursor-pointer">
                                Xem tất cả <ArrowUpRight className="size-3.5" />
                            </Button>
                        </Link>
                    </CardHeader>
                    <CardContent>
                        {isLoading ? (
                            <div className="py-8 text-center text-sm text-muted-foreground">Đang tải...</div>
                        ) : recentOrders.length === 0 ? (
                            <div className="py-8 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                                <Receipt className="size-8 text-muted-foreground/50" />
                                <span>Chưa có đơn hàng nào hôm nay. Hãy mở POS để bán hàng!</span>
                            </div>
                        ) : (
                            <div className="divide-y divide-border/60">
                                {recentOrders.map((order) => (
                                    <div key={order.id} className="py-3 flex items-center justify-between gap-4">
                                        <div className="min-w-0">
                                            <div className="flex items-center gap-2">
                                                <span className="font-semibold text-xs font-mono text-foreground">
                                                    {order.orderNumber || order.id?.substring(0, 8)}
                                                </span>
                                                <Badge
                                                    variant={order.status === "REFUNDED" ? "destructive" : "success"}
                                                    className="text-[10px] py-0 px-1.5"
                                                >
                                                    {order.status === "REFUNDED" ? "Đã hoàn" : "Hoàn thành"}
                                                </Badge>
                                            </div>
                                            <p className="text-xs text-muted-foreground mt-0.5 truncate">
                                                {order.customer?.fullName || "Khách vãng lai"} •{" "}
                                                {order.cashier?.fullName || "Thu ngân"} •{" "}
                                                {order.paymentType === "CASH" ? "Tiền mặt" : "Thẻ / Chuyển khoản"}
                                            </p>
                                        </div>
                                        <div className="text-right shrink-0">
                                            <div className="font-bold text-sm text-foreground">
                                                {(order.totalAmount || 0).toLocaleString("vi-VN")} ₫
                                            </div>
                                            <div className="text-[11px] text-muted-foreground font-mono">
                                                {order.createdAt ? new Date(order.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) : ""}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Low Stock Alerts & Quick Actions - 1 Col */}
                <div className="space-y-6">
                    <Card className="border-border/60 bg-card/60">
                        <CardHeader className="pb-3">
                            <CardTitle className="text-base font-semibold flex items-center gap-2">
                                <AlertTriangle className="size-4 text-amber-500" />
                                Cảnh báo tồn kho thấp
                            </CardTitle>
                            <CardDescription>Sản phẩm dưới định mức tồn tối thiểu</CardDescription>
                        </CardHeader>
                        <CardContent>
                            {lowStockItems.length === 0 ? (
                                <div className="py-6 text-center text-xs text-muted-foreground">
                                    Tồn kho an toàn, không có sản phẩm nào sắp hết.
                                </div>
                            ) : (
                                <div className="space-y-3">
                                    {lowStockItems.slice(0, 4).map((item) => (
                                        <div key={item.id} className="flex items-center justify-between p-2 rounded-lg bg-muted/40 border border-border/40">
                                            <div className="min-w-0 pr-2">
                                                <p className="text-xs font-semibold text-foreground truncate">
                                                    {item.product?.name}
                                                </p>
                                                <p className="text-[11px] text-muted-foreground">
                                                    Mã: {item.product?.sku}
                                                </p>
                                            </div>
                                            <Badge variant="destructive" className="shrink-0 text-xs">
                                                Còn {item.quantity}
                                            </Badge>
                                        </div>
                                    ))}
                                    <Link to="/inventory">
                                        <Button variant="outline" size="sm" className="w-full text-xs mt-2 cursor-pointer">
                                            Vào quản lý kho
                                        </Button>
                                    </Link>
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    {/* Quick Shortcuts */}
                    <Card className="border-border/60 bg-card/60">
                        <CardHeader className="pb-3">
                            <CardTitle className="text-base font-semibold">Thao tác nhanh</CardTitle>
                        </CardHeader>
                        <CardContent className="grid grid-cols-2 gap-2.5">
                            <Link to="/products">
                                <Button variant="outline" size="sm" className="w-full h-auto py-2.5 flex-col gap-1 text-xs cursor-pointer">
                                    <Package className="size-4 text-blue-500" />
                                    <span>Thêm Sản Phẩm</span>
                                </Button>
                            </Link>
                            <Link to="/customers">
                                <Button variant="outline" size="sm" className="w-full h-auto py-2.5 flex-col gap-1 text-xs cursor-pointer">
                                    <Users className="size-4 text-emerald-500" />
                                    <span>Khách Hàng</span>
                                </Button>
                            </Link>
                            <Link to="/inventory">
                                <Button variant="outline" size="sm" className="w-full h-auto py-2.5 flex-col gap-1 text-xs cursor-pointer">
                                    <TrendingUp className="size-4 text-violet-500" />
                                    <span>Nhập Tồn Kho</span>
                                </Button>
                            </Link>
                            <Link to="/shifts">
                                <Button variant="outline" size="sm" className="w-full h-auto py-2.5 flex-col gap-1 text-xs cursor-pointer">
                                    <Clock className="size-4 text-amber-500" />
                                    <span>Chốt Ca Làm</span>
                                </Button>
                            </Link>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}
