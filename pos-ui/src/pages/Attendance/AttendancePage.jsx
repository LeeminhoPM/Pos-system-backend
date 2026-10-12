import React, { useState, useEffect } from "react";
import { attendanceApi, branchApi } from "@/services/api";
import { Clock, LogIn, LogOut, CheckCircle, AlertCircle, Calendar } from "lucide-react";

export default function AttendancePage() {
    const [todayAttendance, setTodayAttendance] = useState(null);
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);
    const [notes, setNotes] = useState("");
    const [branches, setBranches] = useState([]);
    const [selectedBranch, setSelectedBranch] = useState("");

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const [todayRes, historyRes] = await Promise.all([
                attendanceApi.getToday().catch(() => ({ data: null })),
                attendanceApi.getMyHistory(0, 10).catch(() => ({ data: { content: [] } }))
            ]);
            setTodayAttendance(todayRes?.data?.data || null);
            setHistory(historyRes?.data?.data?.content || []);
        } catch (err) {
            console.error("Lỗi tải thông tin chấm công:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleCheckIn = async () => {
        setActionLoading(true);
        try {
            await attendanceApi.checkIn(selectedBranch || undefined, notes);
            setNotes("");
            await loadData();
        } catch (err) {
            alert(err?.response?.data?.message || err.message || "Check-in thất bại");
        } finally {
            setActionLoading(false);
        }
    };

    const handleCheckOut = async () => {
        setActionLoading(true);
        try {
            await attendanceApi.checkOut(notes);
            setNotes("");
            await loadData();
        } catch (err) {
            alert(err?.response?.data?.message || err.message || "Check-out thất bại");
        } finally {
            setActionLoading(false);
        }
    };

    return (
        <div className="space-y-6 max-w-6xl mx-auto p-6">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
                        <Clock className="w-7 h-7 text-indigo-600 dark:text-indigo-400" />
                        Chấm Công & Ca Làm Việc
                    </h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                        Quản lý giờ vào ca, tan ca và theo dõi tổng giờ công lao động hàng ngày
                    </p>
                </div>
            </div>

            {/* Quick Action Card */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="md:col-span-1 bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
                    <div>
                        <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-2">Trạng Thái Hôm Nay</h2>
                        {todayAttendance ? (
                            <div className="space-y-3">
                                <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400 font-medium">
                                    <CheckCircle className="w-5 h-5" />
                                    <span>Đã vào ca: {new Date(todayAttendance.checkInTime).toLocaleTimeString("vi-VN")}</span>
                                </div>
                                {todayAttendance.checkOutTime ? (
                                    <div className="flex items-center gap-2 text-indigo-600 dark:text-indigo-400 font-medium">
                                        <LogOut className="w-5 h-5" />
                                        <span>Đã tan ca: {new Date(todayAttendance.checkOutTime).toLocaleTimeString("vi-VN")}</span>
                                    </div>
                                ) : (
                                    <p className="text-xs text-amber-600 dark:text-amber-400">Đang trong ca làm việc...</p>
                                )}
                                {todayAttendance.workHours > 0 && (
                                    <div className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                                        Tổng giờ làm: <span className="text-indigo-600">{todayAttendance.workHours} giờ</span>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="flex items-center gap-2 text-amber-600 dark:text-amber-400 font-medium">
                                <AlertCircle className="w-5 h-5" />
                                <span>Chưa chấm công vào ca</span>
                            </div>
                        )}
                    </div>

                    <div className="mt-6 space-y-3">
                        <textarea
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                            placeholder="Ghi chú ca làm (nếu có)..."
                            className="w-full text-sm rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900 p-2.5 text-slate-900 dark:text-slate-100"
                            rows={2}
                        />

                        {!todayAttendance ? (
                            <button
                                onClick={handleCheckIn}
                                disabled={actionLoading}
                                className="w-full inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-medium shadow transition disabled:opacity-50"
                            >
                                <LogIn className="w-4 h-4" />
                                {actionLoading ? "Đang xử lý..." : "Check-in Vào Ca"}
                            </button>
                        ) : !todayAttendance.checkOutTime ? (
                            <button
                                onClick={handleCheckOut}
                                disabled={actionLoading}
                                className="w-full inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-medium shadow transition disabled:opacity-50"
                            >
                                <LogOut className="w-4 h-4" />
                                {actionLoading ? "Đang xử lý..." : "Check-out Tan Ca"}
                            </button>
                        ) : (
                            <div className="text-center text-sm text-slate-500 py-2">
                                Bạn đã hoàn thành chấm công ngày hôm nay
                            </div>
                        )}
                    </div>
                </div>

                {/* History Table */}
                <div className="md:col-span-2 bg-white dark:bg-slate-800 rounded-2xl p-6 border border-slate-200 dark:border-slate-700 shadow-sm">
                    <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-4 flex items-center gap-2">
                        <Calendar className="w-5 h-5 text-indigo-500" />
                        Lịch Sử Chấm Công Gần Đây
                    </h2>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600 dark:text-slate-300">
                            <thead className="bg-slate-50 dark:bg-slate-900/50 text-xs uppercase text-slate-500 border-b border-slate-200 dark:border-slate-700">
                                <tr>
                                    <th className="py-3 px-4">Ngày</th>
                                    <th className="py-3 px-4">Giờ Vào</th>
                                    <th className="py-3 px-4">Giờ Ra</th>
                                    <th className="py-3 px-4">Tổng Giờ</th>
                                    <th className="py-3 px-4">Trạng Thái</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/50">
                                {history.length > 0 ? (
                                    history.map((item) => (
                                        <tr key={item.id} className="hover:bg-slate-50 dark:hover:bg-slate-700/30">
                                            <td className="py-3 px-4 font-medium text-slate-900 dark:text-slate-100">{item.workDate}</td>
                                            <td className="py-3 px-4 text-emerald-600 font-medium">
                                                {item.checkInTime ? new Date(item.checkInTime).toLocaleTimeString("vi-VN") : "-"}
                                            </td>
                                            <td className="py-3 px-4 text-indigo-600 font-medium">
                                                {item.checkOutTime ? new Date(item.checkOutTime).toLocaleTimeString("vi-VN") : "-"}
                                            </td>
                                            <td className="py-3 px-4 font-semibold">{item.workHours || 0}h</td>
                                            <td className="py-3 px-4">
                                                <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                                                    item.status === "PRESENT" ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400" :
                                                    item.status === "LATE" ? "bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400" :
                                                    "bg-slate-100 text-slate-700"
                                                }`}>
                                                    {item.status === "PRESENT" ? "Đúng giờ" : item.status === "LATE" ? "Đi muộn" : item.status}
                                                </span>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="text-center py-8 text-slate-400">
                                            Chưa có dữ liệu chấm công
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
