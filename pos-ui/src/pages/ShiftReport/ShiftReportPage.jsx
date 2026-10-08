import React, { useState, useEffect } from "react";
import {
    Clock,
    PlayCircle,
    StopCircle,
    Printer,
    CheckCircle2,
    AlertCircle,
    DollarSign,
    RotateCcw,
    Receipt,
    CreditCard,
    Banknote,
    TrendingUp,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { shiftApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

export default function ShiftReportPage() {
    const { user, branch, activeShift, checkActiveShift } = useAuthStore();
    const [shiftData, setShiftData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isActionLoading, setIsActionLoading] = useState(false);

    const loadShift = async () => {
        setIsLoading(true);
        try {
            const data = await shiftApi.getCurrent();
            setShiftData(data);
        } catch {
            setShiftData(null);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadShift();
    }, []);

    const handleStartShift = async () => {
        setIsActionLoading(true);
        try {
            await shiftApi.start();
            await checkActiveShift();
            await loadShift();
        } catch (err) {
            alert(err.message || "Không thể bắt đầu ca làm");
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleEndShift = async () => {
        if (!window.confirm("Bạn có chắc chắn muốn kết thúc và chốt ca làm việc này?")) return;
        setIsActionLoading(true);
        try {
            const result = await shiftApi.end(new Date().toISOString());
            setShiftData(result);
            await checkActiveShift();
            alert("Đã kết thúc ca làm việc thành công!");
        } catch (err) {
            alert(err.message || "Không thể kết thúc ca làm");
        } finally {
            setIsActionLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Báo Cáo & Chốt Ca Làm Việc
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Kiểm soát doanh thu, tiền mặt, phương thức thanh toán trong ca của thu ngân
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    {shiftData && !shiftData.shiftEnd && (
                        <Button
                            variant="destructive"
                            size="sm"
                            onClick={handleEndShift}
                            disabled={isActionLoading}
                            className="gap-1.5 h-9 cursor-pointer shadow-sm text-xs font-semibold"
                        >
                            <StopCircle className="size-4" />
                            Kết Thúc Ca & Chốt Sổ
                        </Button>
                    )}
                    {shiftData && (
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => window.print()}
                            className="gap-1.5 h-9 cursor-pointer text-xs"
                        >
                            <Printer className="size-4" /> In Báo Cáo
                        </Button>
                    )}
                </div>
            </div>

            {/* Shift Inactive Banner */}
            {!isLoading && !shiftData && (
                <Card className="border-border/60 bg-card/60 p-8 text-center space-y-4">
                    <div className="size-14 rounded-2xl bg-amber-500/10 text-amber-600 dark:text-amber-400 mx-auto flex items-center justify-center">
                        <Clock className="size-7" />
                    </div>
                    <div className="max-w-md mx-auto space-y-1">
                        <h3 className="font-bold text-lg text-foreground">Chưa có ca làm việc nào đang mở</h3>
                        <p className="text-xs text-muted-foreground">
                            Thu ngân cần bắt đầu ca làm việc trước khi thực hiện bán hàng để hệ thống ghi nhận doanh số và đối soát tiền mặt.
                        </p>
                    </div>
                    <Button
                        onClick={handleStartShift}
                        disabled={isActionLoading}
                        className="bg-blue-600 hover:bg-blue-700 text-white gap-2 cursor-pointer shadow-md shadow-blue-500/20"
                    >
                        <PlayCircle className="size-4" />
                        {isActionLoading ? "Đang mở ca..." : "Bắt Đầu Ca Làm Việc Ngay"}
                    </Button>
                </Card>
            )}

            {/* Active Shift Details */}
            {shiftData && (
                <div className="space-y-6">
                    {/* Top KPI Cards */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-2">
                                <CardTitle className="text-xs font-semibold text-muted-foreground uppercase">
                                    Tổng Doanh Thu
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold text-foreground">
                                    {(shiftData.totalSales || 0).toLocaleString("vi-VN")} ₫
                                </div>
                                <p className="text-xs text-muted-foreground mt-1">
                                    {shiftData.totalOrders || 0} đơn hàng trong ca
                                </p>
                            </CardContent>
                        </Card>

                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-2">
                                <CardTitle className="text-xs font-semibold text-muted-foreground uppercase">
                                    Tiền Hoàn Lại
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold text-destructive">
                                    {(shiftData.totalRefunds || 0).toLocaleString("vi-VN")} ₫
                                </div>
                                <p className="text-xs text-muted-foreground mt-1">
                                    {shiftData.refunds?.length || 0} giao dịch hoàn trả
                                </p>
                            </CardContent>
                        </Card>

                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-2">
                                <CardTitle className="text-xs font-semibold text-muted-foreground uppercase">
                                    Doanh Thu Thực (Net)
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">
                                    {(shiftData.netSale || 0).toLocaleString("vi-VN")} ₫
                                </div>
                                <p className="text-xs text-muted-foreground mt-1">
                                    Tổng thu trừ tiền hoàn
                                </p>
                            </CardContent>
                        </Card>

                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-2">
                                <CardTitle className="text-xs font-semibold text-muted-foreground uppercase">
                                    Trạng Thái Ca
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="flex items-center gap-2 mt-1">
                                    <Badge variant={shiftData.shiftEnd ? "secondary" : "success"} className="text-xs">
                                        {shiftData.shiftEnd ? "Đã chốt sổ" : "Đang diễn ra"}
                                    </Badge>
                                </div>
                                <p className="text-[11px] text-muted-foreground mt-1.5 font-mono truncate">
                                    Mở lúc: {new Date(shiftData.shiftStart).toLocaleTimeString("vi-VN")}
                                </p>
                            </CardContent>
                        </Card>
                    </div>

                    {/* Shift Info & Payment Method Breakdown */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {/* Info Card */}
                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-3">
                                <CardTitle className="text-base font-semibold">Thông tin ca làm</CardTitle>
                                <CardDescription>Chi tiết phiên làm việc của nhân viên thu ngân</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-3 text-xs">
                                <div className="flex justify-between py-1 border-b border-border/40">
                                    <span className="text-muted-foreground">Thu ngân phụ trách:</span>
                                    <span className="font-semibold text-foreground">
                                        {shiftData.cashier?.fullName || user?.fullName || "Thu ngân"}
                                    </span>
                                </div>
                                <div className="flex justify-between py-1 border-b border-border/40">
                                    <span className="text-muted-foreground">Chi nhánh:</span>
                                    <span className="font-semibold text-foreground">
                                        {shiftData.branch?.name || branch?.name || "Chi nhánh"}
                                    </span>
                                </div>
                                <div className="flex justify-between py-1 border-b border-border/40">
                                    <span className="text-muted-foreground">Thời điểm mở ca:</span>
                                    <span className="font-medium text-foreground">
                                        {new Date(shiftData.shiftStart).toLocaleString("vi-VN")}
                                    </span>
                                </div>
                                <div className="flex justify-between py-1 border-b border-border/40">
                                    <span className="text-muted-foreground">Thời điểm đóng ca:</span>
                                    <span className="font-medium text-foreground">
                                        {shiftData.shiftEnd ? new Date(shiftData.shiftEnd).toLocaleString("vi-VN") : "Đang mở..."}
                                    </span>
                                </div>
                            </CardContent>
                        </Card>

                        {/* Payment Breakdown Card */}
                        <Card className="border-border/60 bg-card/60">
                            <CardHeader className="pb-3">
                                <CardTitle className="text-base font-semibold">Phân bổ thanh toán</CardTitle>
                                <CardDescription>Doanh số chia theo hình thức thanh toán</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                {shiftData.paymentSummaries?.length > 0 ? (
                                    shiftData.paymentSummaries.map((ps, idx) => (
                                        <div key={idx} className="p-3 rounded-lg bg-muted/40 border border-border/40 flex items-center justify-between">
                                            <div className="flex items-center gap-2.5">
                                                <div className="size-8 rounded-lg bg-background flex items-center justify-center text-blue-500">
                                                    {ps.paymentType === "CASH" ? <Banknote className="size-4" /> : <CreditCard className="size-4" />}
                                                </div>
                                                <div>
                                                    <p className="font-semibold text-xs text-foreground">
                                                        {ps.paymentType === "CASH" ? "Tiền mặt (Cash)" : "Thẻ / Chuyển khoản"}
                                                    </p>
                                                    <p className="text-[11px] text-muted-foreground">
                                                        {ps.transactionCount} giao dịch ({Math.round(ps.percentage || 0)}%)
                                                    </p>
                                                </div>
                                            </div>
                                            <span className="font-bold text-sm text-foreground">
                                                {(ps.totalAmount || 0).toLocaleString("vi-VN")} ₫
                                            </span>
                                        </div>
                                    ))
                                ) : (
                                    <div className="py-8 text-center text-xs text-muted-foreground">
                                        Chưa có giao dịch phát sinh trong ca này.
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    </div>
                </div>
            )}
        </div>
    );
}
