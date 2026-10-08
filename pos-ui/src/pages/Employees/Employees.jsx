import React, { useState, useEffect } from "react";
import {
    UserCog,
    UserPlus,
    Edit2,
    Trash2,
    Shield,
    Store,
    Mail,
    Phone,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { employeeApi, branchApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Employees() {
    const { store, branches, user } = useAuthStore();
    const [employees, setEmployees] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    // Modal
    const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
    const [editingEmployee, setEditingEmployee] = useState(null);
    const [employeeForm, setEmployeeForm] = useState({
        fullName: "",
        email: "",
        phone: "",
        password: "",
        roles: "ROLE_BRANCH_CASHIER",
        branchId: "",
    });

    const loadEmployees = async () => {
        if (!store?.id) return;
        setIsLoading(true);
        try {
            const data = await employeeApi.getStoreEmployees(store.id);
            setEmployees(data || []);
        } catch (err) {
            console.error("Lỗi khi tải nhân viên:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadEmployees();
    }, [store?.id]);

    const handleOpenAddModal = () => {
        setEditingEmployee(null);
        setEmployeeForm({
            fullName: "",
            email: "",
            phone: "",
            password: "",
            roles: "ROLE_BRANCH_CASHIER",
            branchId: branches[0]?.id || "",
        });
        setIsEmployeeModalOpen(true);
    };

    const handleOpenEditModal = (emp) => {
        setEditingEmployee(emp);
        setEmployeeForm({
            fullName: emp.fullName || "",
            email: emp.email || "",
            phone: emp.phone || "",
            password: "",
            roles: emp.roles || "ROLE_BRANCH_CASHIER",
            branchId: emp.branchId || branches[0]?.id || "",
        });
        setIsEmployeeModalOpen(true);
    };

    const handleSaveEmployee = async (e) => {
        e.preventDefault();
        try {
            if (editingEmployee) {
                await employeeApi.updateEmployee(editingEmployee.id, {
                    ...employeeForm,
                    storeId: store.id,
                });
            } else {
                await employeeApi.createStoreEmployee(store.id, {
                    ...employeeForm,
                    storeId: store.id,
                });
            }
            setIsEmployeeModalOpen(false);
            loadEmployees();
        } catch (err) {
            alert(err.message || "Không thể lưu thông tin nhân viên");
        }
    };

    const handleDeleteEmployee = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa nhân viên này?")) return;
        try {
            await employeeApi.deleteEmployee(id);
            loadEmployees();
        } catch (err) {
            alert(err.message || "Không thể xóa nhân viên");
        }
    };

    const getRoleBadge = (role) => {
        switch (role) {
            case "ROLE_STORE_ADMIN":
                return <Badge variant="destructive">Chủ cửa hàng (Admin)</Badge>;
            case "ROLE_STORE_MANAGER":
                return <Badge variant="default">Quản lý tổng</Badge>;
            case "ROLE_BRANCH_MANAGER":
                return <Badge variant="info">Quản lý chi nhánh</Badge>;
            case "ROLE_BRANCH_CASHIER":
                return <Badge variant="secondary">Thu ngân</Badge>;
            default:
                return <Badge variant="outline">{role}</Badge>;
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Nhân Viên & Phân Quyền
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Quản lý danh sách nhân viên, tài khoản và phân quyền truy cập
                    </p>
                </div>
                <Button
                    size="sm"
                    onClick={handleOpenAddModal}
                    className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 h-9 font-medium shadow-md shadow-blue-500/20 cursor-pointer"
                >
                    <UserPlus className="size-4" />
                    Thêm Nhân Viên
                </Button>
            </div>

            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-0">
                    {isLoading ? (
                        <div className="py-12 text-center text-sm text-muted-foreground">Đang tải nhân viên...</div>
                    ) : employees.length === 0 ? (
                        <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                            <UserCog className="size-10 text-muted-foreground/40" />
                            <span>Chưa có nhân viên nào trong danh sách.</span>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-xs border-collapse">
                                <thead>
                                    <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                        <th className="py-3 px-4">Nhân viên</th>
                                        <th className="py-3 px-4">Email</th>
                                        <th className="py-3 px-4">Số điện thoại</th>
                                        <th className="py-3 px-4">Vai trò</th>
                                        <th className="py-3 px-4 text-center">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {employees.map((emp) => (
                                        <tr key={emp.id} className="hover:bg-muted/20 transition-colors">
                                            <td className="py-3 px-4 font-semibold text-foreground">
                                                {emp.fullName}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {emp.email}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground font-mono">
                                                {emp.phone || "—"}
                                            </td>
                                            <td className="py-3 px-4">
                                                {getRoleBadge(emp.roles)}
                                            </td>
                                            <td className="py-3 px-4 text-center">
                                                <div className="flex items-center justify-center gap-1.5">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={() => handleOpenEditModal(emp)}
                                                        className="text-muted-foreground hover:text-foreground cursor-pointer"
                                                    >
                                                        <Edit2 className="size-3.5" />
                                                    </Button>
                                                    {emp.id !== user?.id && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon-xs"
                                                            onClick={() => handleDeleteEmployee(emp.id)}
                                                            className="text-muted-foreground hover:text-destructive cursor-pointer"
                                                        >
                                                            <Trash2 className="size-3.5" />
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

            {/* Add / Edit Employee Modal */}
            <Modal
                isOpen={isEmployeeModalOpen}
                onClose={() => setIsEmployeeModalOpen(false)}
                title={editingEmployee ? "Chỉnh sửa nhân viên" : "Tạo tài khoản nhân viên"}
                maxWidth="max-w-md"
            >
                <form onSubmit={handleSaveEmployee} className="space-y-3.5">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Họ và tên *</label>
                        <Input
                            placeholder="Nguyễn Văn A"
                            value={employeeForm.fullName}
                            onChange={(e) => setEmployeeForm({ ...employeeForm, fullName: e.target.value })}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Email *</label>
                        <Input
                            type="email"
                            placeholder="nhanvien@example.com"
                            value={employeeForm.email}
                            onChange={(e) => setEmployeeForm({ ...employeeForm, email: e.target.value })}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Số điện thoại</label>
                        <Input
                            placeholder="0912345678"
                            value={employeeForm.phone}
                            onChange={(e) => setEmployeeForm({ ...employeeForm, phone: e.target.value })}
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">
                            {editingEmployee ? "Mật khẩu mới (bỏ trống nếu không đổi)" : "Mật khẩu *"}
                        </label>
                        <Input
                            type="password"
                            placeholder="••••••••"
                            value={employeeForm.password}
                            onChange={(e) => setEmployeeForm({ ...employeeForm, password: e.target.value })}
                            required={!editingEmployee}
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Vai trò phân quyền</label>
                        <select
                            value={employeeForm.roles}
                            onChange={(e) => setEmployeeForm({ ...employeeForm, roles: e.target.value })}
                            className="w-full text-xs rounded-lg border border-border bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
                        >
                            <option value="ROLE_BRANCH_CASHIER">Nhân viên thu ngân (ROLE_BRANCH_CASHIER)</option>
                            <option value="ROLE_BRANCH_MANAGER">Quản lý chi nhánh (ROLE_BRANCH_MANAGER)</option>
                            <option value="ROLE_STORE_MANAGER">Quản lý tổng cửa hàng (ROLE_STORE_MANAGER)</option>
                        </select>
                    </div>

                    {branches.length > 0 && (
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Chi nhánh công tác</label>
                            <select
                                value={employeeForm.branchId}
                                onChange={(e) => setEmployeeForm({ ...employeeForm, branchId: e.target.value })}
                                className="w-full text-xs rounded-lg border border-border bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
                            >
                                {branches.map((b) => (
                                    <option key={b.id} value={b.id}>
                                        {b.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white cursor-pointer mt-2 text-xs">
                        {editingEmployee ? "Lưu Thay Đổi" : "Tạo Nhân Viên"}
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
