import React, { useState, useEffect } from "react";
import {
    Users,
    Search,
    UserPlus,
    Edit2,
    Trash2,
    Phone,
    Mail,
    Award,
    DollarSign,
    MapPin,
} from "lucide-react";
import { customerApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Customers() {
    const [customers, setCustomers] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    // Modal state
    const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
    const [editingCustomer, setEditingCustomer] = useState(null);
    const [customerForm, setCustomerForm] = useState({
        fullName: "",
        phone: "",
        email: "",
        address: "",
        loyaltyPoints: "0",
        totalSpent: "0",
    });

    const loadCustomers = async () => {
        setIsLoading(true);
        try {
            const data = await customerApi.getAll();
            setCustomers(data || []);
        } catch (err) {
            console.error("Lỗi khi tải khách hàng:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadCustomers();
    }, []);

    const handleOpenAddModal = () => {
        setEditingCustomer(null);
        setCustomerForm({
            fullName: "",
            phone: "",
            email: "",
            address: "",
            loyaltyPoints: "0",
            totalSpent: "0",
        });
        setIsCustomerModalOpen(true);
    };

    const handleOpenEditModal = (cust) => {
        setEditingCustomer(cust);
        setCustomerForm({
            fullName: cust.fullName || "",
            phone: cust.phone || "",
            email: cust.email || "",
            address: cust.address || "",
            loyaltyPoints: cust.loyaltyPoints?.toString() || "0",
            totalSpent: cust.totalSpent?.toString() || "0",
        });
        setIsCustomerModalOpen(true);
    };

    const handleSaveCustomer = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...customerForm,
                loyaltyPoints: Number(customerForm.loyaltyPoints) || 0,
                totalSpent: Number(customerForm.totalSpent) || 0,
            };

            if (editingCustomer) {
                await customerApi.update(editingCustomer.id, payload);
            } else {
                await customerApi.create(payload);
            }

            setIsCustomerModalOpen(false);
            loadCustomers();
        } catch (err) {
            alert(err.message || "Không thể lưu thông tin khách hàng");
        }
    };

    const handleDeleteCustomer = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa khách hàng này?")) return;
        try {
            await customerApi.delete(id);
            loadCustomers();
        } catch (err) {
            alert(err.message || "Không thể xóa khách hàng");
        }
    };

    const filtered = customers.filter((c) => {
        const q = searchQuery.toLowerCase().trim();
        return (
            !q ||
            c.fullName?.toLowerCase().includes(q) ||
            c.phone?.includes(q) ||
            c.email?.toLowerCase().includes(q)
        );
    });

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Khách Hàng & Điểm Thưởng
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Quản lý cơ sở dữ liệu khách hàng, tích điểm và lịch sử chi tiêu
                    </p>
                </div>
                <Button
                    size="sm"
                    onClick={handleOpenAddModal}
                    className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 h-9 font-medium shadow-md shadow-blue-500/20 cursor-pointer"
                >
                    <UserPlus className="size-4" />
                    Thêm Khách Hàng
                </Button>
            </div>

            {/* Search */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-4">
                    <div className="relative">
                        <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                        <Input
                            placeholder="Tìm kiếm khách hàng theo tên, số điện thoại hoặc email..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-9 h-9 text-sm"
                        />
                    </div>
                </CardContent>
            </Card>

            {/* Customers Table */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-0">
                    {isLoading ? (
                        <div className="py-12 text-center text-sm text-muted-foreground">Đang tải khách hàng...</div>
                    ) : filtered.length === 0 ? (
                        <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                            <Users className="size-10 text-muted-foreground/40" />
                            <span>Không tìm thấy khách hàng nào.</span>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-xs border-collapse">
                                <thead>
                                    <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                        <th className="py-3 px-4">Khách hàng</th>
                                        <th className="py-3 px-4">Số điện thoại</th>
                                        <th className="py-3 px-4">Email</th>
                                        <th className="py-3 px-4">Địa chỉ</th>
                                        <th className="py-3 px-4 text-center">Điểm thưởng</th>
                                        <th className="py-3 px-4 text-right">Tổng chi tiêu</th>
                                        <th className="py-3 px-4 text-center">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {filtered.map((cust) => (
                                        <tr key={cust.id} className="hover:bg-muted/20 transition-colors">
                                            <td className="py-3 px-4 font-semibold text-foreground">
                                                {cust.fullName}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground font-mono">
                                                {cust.phone || "—"}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {cust.email || "—"}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground truncate max-w-xs">
                                                {cust.address || "—"}
                                            </td>
                                            <td className="py-3 px-4 text-center">
                                                <Badge variant="secondary" className="gap-1 font-bold">
                                                    <Award className="size-3 text-amber-500" />
                                                    {cust.loyaltyPoints || 0}
                                                </Badge>
                                            </td>
                                            <td className="py-3 px-4 text-right font-bold text-foreground">
                                                {(cust.totalSpent || 0).toLocaleString("vi-VN")} ₫
                                            </td>
                                            <td className="py-3 px-4 text-center">
                                                <div className="flex items-center justify-center gap-1.5">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={() => handleOpenEditModal(cust)}
                                                        className="text-muted-foreground hover:text-foreground cursor-pointer"
                                                    >
                                                        <Edit2 className="size-3.5" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={() => handleDeleteCustomer(cust.id)}
                                                        className="text-muted-foreground hover:text-destructive cursor-pointer"
                                                    >
                                                        <Trash2 className="size-3.5" />
                                                    </Button>
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

            {/* Add / Edit Customer Modal */}
            <Modal
                isOpen={isCustomerModalOpen}
                onClose={() => setIsCustomerModalOpen(false)}
                title={editingCustomer ? "Chỉnh sửa khách hàng" : "Thêm khách hàng mới"}
                maxWidth="max-w-md"
            >
                <form onSubmit={handleSaveCustomer} className="space-y-3.5">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Họ và tên *</label>
                        <Input
                            placeholder="Trần Thị Mai Anh"
                            value={customerForm.fullName}
                            onChange={(e) => setCustomerForm({ ...customerForm, fullName: e.target.value })}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Số điện thoại *</label>
                        <Input
                            placeholder="0909123456"
                            value={customerForm.phone}
                            onChange={(e) => setCustomerForm({ ...customerForm, phone: e.target.value })}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Email</label>
                        <Input
                            type="email"
                            placeholder="maianh.tran@gmail.com"
                            value={customerForm.email}
                            onChange={(e) => setCustomerForm({ ...customerForm, email: e.target.value })}
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Địa chỉ</label>
                        <Input
                            placeholder="Số nhà, đường, quận/huyện..."
                            value={customerForm.address}
                            onChange={(e) => setCustomerForm({ ...customerForm, address: e.target.value })}
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Điểm tích lũy</label>
                            <Input
                                type="number"
                                value={customerForm.loyaltyPoints}
                                onChange={(e) => setCustomerForm({ ...customerForm, loyaltyPoints: e.target.value })}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Tổng chi tiêu (₫)</label>
                            <Input
                                type="number"
                                value={customerForm.totalSpent}
                                onChange={(e) => setCustomerForm({ ...customerForm, totalSpent: e.target.value })}
                            />
                        </div>
                    </div>

                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white cursor-pointer mt-2 text-xs">
                        {editingCustomer ? "Lưu Thay Đổi" : "Tạo Khách Hàng"}
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
