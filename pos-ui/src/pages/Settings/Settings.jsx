import React, { useState, useEffect } from "react";
import {
    Settings as SettingsIcon,
    Store,
    MapPin,
    Plus,
    Edit2,
    Trash2,
    Save,
    Clock,
    Phone,
    Mail,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { storeApi, branchApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Settings() {
    const { store, branches, loadStoreAndBranches, user } = useAuthStore();
    const [storeForm, setStoreForm] = useState({
        branch: "",
        description: "",
        storeType: "",
        address: "",
        phone: "",
        email: "",
    });
    const [isSavingStore, setIsSavingStore] = useState(false);

    // Branch Modal
    const [isBranchModalOpen, setIsBranchModalOpen] = useState(false);
    const [editingBranch, setEditingBranch] = useState(null);
    const [branchForm, setBranchForm] = useState({
        name: "",
        address: "",
        phone: "",
        email: "",
        openTime: "08:00",
        closeTime: "22:00",
    });

    useEffect(() => {
        if (store) {
            setStoreForm({
                branch: store.branch || "",
                description: store.description || "",
                storeType: store.storeType || "",
                address: store.contact?.address || "",
                phone: store.contact?.phone || "",
                email: store.contact?.email || "",
            });
        }
    }, [store]);

    const handleSaveStore = async (e) => {
        e.preventDefault();
        if (!store?.id) return;
        setIsSavingStore(true);
        try {
            await storeApi.update(store.id, {
                branch: storeForm.branch,
                description: storeForm.description,
                storeType: storeForm.storeType,
                contact: {
                    address: storeForm.address,
                    phone: storeForm.phone,
                    email: storeForm.email,
                },
            });
            await loadStoreAndBranches(user);
            alert("Đã cập nhật thông tin cửa hàng thành công!");
        } catch (err) {
            alert(err.message || "Không thể cập nhật cửa hàng");
        } finally {
            setIsSavingStore(false);
        }
    };

    const handleOpenAddBranch = () => {
        setEditingBranch(null);
        setBranchForm({
            name: "",
            address: "",
            phone: "",
            email: "",
            openTime: "08:00",
            closeTime: "22:00",
        });
        setIsBranchModalOpen(true);
    };

    const handleOpenEditBranch = (b) => {
        setEditingBranch(b);
        setBranchForm({
            name: b.name || "",
            address: b.address || "",
            phone: b.phone || "",
            email: b.email || "",
            openTime: b.openTime || "08:00",
            closeTime: b.closeTime || "22:00",
        });
        setIsBranchModalOpen(true);
    };

    const handleSaveBranch = async (e) => {
        e.preventDefault();
        if (!store?.id) return;
        try {
            const payload = {
                ...branchForm,
                storeId: store.id,
            };

            if (editingBranch) {
                await branchApi.update(editingBranch.id, payload);
            } else {
                await branchApi.create(payload);
            }

            setIsBranchModalOpen(false);
            await loadStoreAndBranches(user);
        } catch (err) {
            alert(err.message || "Không thể lưu chi nhánh");
        }
    };

    const handleDeleteBranch = async (id) => {
        if (!window.confirm("Bạn có chắc muốn xóa chi nhánh này?")) return;
        try {
            await branchApi.delete(id);
            await loadStoreAndBranches(user);
        } catch (err) {
            alert(err.message || "Không thể xóa chi nhánh");
        }
    };

    return (
        <div className="space-y-6 max-w-4xl">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-foreground">
                    Cấu Hình Hệ Thống & Cửa Hàng
                </h1>
                <p className="text-sm text-muted-foreground mt-0.5">
                    Thiết lập thông tin thương hiệu và các chi nhánh kinh doanh
                </p>
            </div>

            {/* Store Information */}
            <Card className="border-border/60 bg-card/60">
                <CardHeader>
                    <CardTitle className="text-base font-semibold flex items-center gap-2">
                        <Store className="size-4 text-blue-500" />
                        Thông tin doanh nghiệp / Cửa hàng
                    </CardTitle>
                    <CardDescription>
                        Tên và thông tin liên hệ sẽ xuất hiện trên tiêu đề hóa đơn in
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSaveStore} className="space-y-4">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold">Tên thương hiệu *</label>
                                <Input
                                    value={storeForm.branch}
                                    onChange={(e) => setStoreForm({ ...storeForm, branch: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="space-y-1">
                                <label className="text-xs font-semibold">Loại hình kinh doanh</label>
                                <Input
                                    value={storeForm.storeType}
                                    onChange={(e) => setStoreForm({ ...storeForm, storeType: e.target.value })}
                                />
                            </div>
                        </div>

                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Mô tả / Slogan</label>
                            <Input
                                value={storeForm.description}
                                onChange={(e) => setStoreForm({ ...storeForm, description: e.target.value })}
                            />
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                            <div className="space-y-1 sm:col-span-2">
                                <label className="text-xs font-semibold">Địa chỉ trụ sở</label>
                                <Input
                                    value={storeForm.address}
                                    onChange={(e) => setStoreForm({ ...storeForm, address: e.target.value })}
                                />
                            </div>
                            <div className="space-y-1">
                                <label className="text-xs font-semibold">Số điện thoại hotline</label>
                                <Input
                                    value={storeForm.phone}
                                    onChange={(e) => setStoreForm({ ...storeForm, phone: e.target.value })}
                                />
                            </div>
                        </div>

                        <div className="flex justify-end pt-2">
                            <Button
                                type="submit"
                                disabled={isSavingStore}
                                className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 text-xs cursor-pointer shadow-sm"
                            >
                                <Save className="size-3.5" />
                                {isSavingStore ? "Đang lưu..." : "Lưu Thông Tin Cửa Hàng"}
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>

            {/* Branches List */}
            <Card className="border-border/60 bg-card/60">
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle className="text-base font-semibold flex items-center gap-2">
                            <MapPin className="size-4 text-emerald-500" />
                            Danh sách chi nhánh
                        </CardTitle>
                        <CardDescription>
                            Các điểm bán và kho trực thuộc hệ thống
                        </CardDescription>
                    </div>
                    <Button
                        size="sm"
                        onClick={handleOpenAddBranch}
                        className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 text-xs cursor-pointer"
                    >
                        <Plus className="size-3.5" />
                        Thêm Chi Nhánh
                    </Button>
                </CardHeader>
                <CardContent className="space-y-3">
                    {branches.length === 0 ? (
                        <div className="py-6 text-center text-xs text-muted-foreground">
                            Chưa có chi nhánh nào được tạo.
                        </div>
                    ) : (
                        branches.map((b) => (
                            <div
                                key={b.id}
                                className="p-3.5 rounded-xl border border-border/60 bg-muted/20 flex items-center justify-between gap-4"
                            >
                                <div className="space-y-1 min-w-0">
                                    <div className="flex items-center gap-2">
                                        <h4 className="font-semibold text-sm text-foreground truncate">{b.name}</h4>
                                        <Badge variant="outline" className="text-[10px]">
                                            {b.openTime || "08:00"} - {b.closeTime || "22:00"}
                                        </Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                                        <MapPin className="size-3 shrink-0" /> {b.address || "Chưa cập nhật địa chỉ"}
                                    </p>
                                    {b.phone && (
                                        <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                                            <Phone className="size-3 shrink-0" /> {b.phone}
                                        </p>
                                    )}
                                </div>

                                <div className="flex items-center gap-1.5 shrink-0">
                                    <Button
                                        variant="ghost"
                                        size="icon-xs"
                                        onClick={() => handleOpenEditBranch(b)}
                                        className="text-muted-foreground hover:text-foreground cursor-pointer"
                                    >
                                        <Edit2 className="size-3.5" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon-xs"
                                        onClick={() => handleDeleteBranch(b.id)}
                                        className="text-muted-foreground hover:text-destructive cursor-pointer"
                                    >
                                        <Trash2 className="size-3.5" />
                                    </Button>
                                </div>
                            </div>
                        ))
                    )}
                </CardContent>
            </Card>

            {/* Branch Add/Edit Modal */}
            <Modal
                isOpen={isBranchModalOpen}
                onClose={() => setIsBranchModalOpen(false)}
                title={editingBranch ? "Chỉnh sửa chi nhánh" : "Thêm chi nhánh mới"}
                maxWidth="max-w-md"
            >
                <form onSubmit={handleSaveBranch} className="space-y-3.5">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Tên chi nhánh *</label>
                        <Input
                            placeholder="Chi nhánh Quận 1"
                            value={branchForm.name}
                            onChange={(e) => setBranchForm({ ...branchForm, name: e.target.value })}
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Địa chỉ chi nhánh *</label>
                        <Input
                            placeholder="Số nhà, tên đường, phường/xã..."
                            value={branchForm.address}
                            onChange={(e) => setBranchForm({ ...branchForm, address: e.target.value })}
                            required
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Điện thoại</label>
                            <Input
                                placeholder="0901234567"
                                value={branchForm.phone}
                                onChange={(e) => setBranchForm({ ...branchForm, phone: e.target.value })}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Email</label>
                            <Input
                                type="email"
                                placeholder="branch@skypos.vn"
                                value={branchForm.email}
                                onChange={(e) => setBranchForm({ ...branchForm, email: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Giờ mở cửa</label>
                            <Input
                                type="time"
                                value={branchForm.openTime}
                                onChange={(e) => setBranchForm({ ...branchForm, openTime: e.target.value })}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Giờ đóng cửa</label>
                            <Input
                                type="time"
                                value={branchForm.closeTime}
                                onChange={(e) => setBranchForm({ ...branchForm, closeTime: e.target.value })}
                            />
                        </div>
                    </div>

                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white cursor-pointer mt-2 text-xs">
                        {editingBranch ? "Lưu Thay Đổi" : "Tạo Chi Nhánh"}
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
