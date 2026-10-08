import React, { useState, useEffect } from "react";
import {
    Plus,
    Search,
    Edit2,
    Trash2,
    Package,
    FolderPlus,
    Sparkles,
    CheckCircle2,
    X,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { productApi, categoryApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";

export default function Products() {
    const { store, user } = useAuthStore();
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedCategory, setSelectedCategory] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    // Modal states
    const [isProductModalOpen, setIsProductModalOpen] = useState(false);
    const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);

    // Form states
    const [productForm, setProductForm] = useState({
        name: "",
        sku: "",
        barcode: "",
        categoryId: "",
        mrp: "",
        costPrice: "",
        sellingPrice: "",
        brand: "",
        minStockLevel: "5",
        image: "",
        description: "",
    });

    const [newCategoryName, setNewCategoryName] = useState("");

    const loadData = async () => {
        if (!store?.id) return;
        setIsLoading(true);
        try {
            const [prods, cats] = await Promise.all([
                productApi.getByStore(store.id).catch(() => []),
                categoryApi.getByStore(store.id).catch(() => []),
            ]);
            setProducts(prods || []);
            setCategories(cats || []);
        } catch (err) {
            console.error("Lỗi khi tải danh sách sản phẩm:", err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [store?.id]);

    const handleOpenAddModal = () => {
        setEditingProduct(null);
        setProductForm({
            name: "",
            sku: "SKU-" + Math.floor(1000 + Math.random() * 9000),
            barcode: "",
            categoryId: categories[0]?.id || "",
            mrp: "",
            costPrice: "",
            sellingPrice: "",
            brand: "",
            minStockLevel: "5",
            image: "",
            description: "",
        });
        setIsProductModalOpen(true);
    };

    const handleOpenEditModal = (product) => {
        setEditingProduct(product);
        setProductForm({
            name: product.name || "",
            sku: product.sku || "",
            barcode: product.barcode || "",
            categoryId: product.categoryId || product.category?.id || "",
            mrp: product.mrp || "",
            costPrice: product.costPrice || "",
            sellingPrice: product.sellingPrice || "",
            brand: product.brand || "",
            minStockLevel: product.minStockLevel?.toString() || "5",
            image: product.image || "",
            description: product.description || "",
        });
        setIsProductModalOpen(true);
    };

    const handleSaveProduct = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...productForm,
                storeId: store.id,
                mrp: Number(productForm.mrp) || Number(productForm.sellingPrice) || 0,
                costPrice: Number(productForm.costPrice) || 0,
                sellingPrice: Number(productForm.sellingPrice) || 0,
                minStockLevel: Number(productForm.minStockLevel) || 5,
            };

            if (editingProduct) {
                await productApi.update(editingProduct.id, payload);
            } else {
                await productApi.create(payload);
            }

            setIsProductModalOpen(false);
            loadData();
        } catch (err) {
            alert(err.message || "Không thể lưu sản phẩm");
        }
    };

    const handleDeleteProduct = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) return;
        try {
            await productApi.delete(id);
            loadData();
        } catch (err) {
            alert(err.message || "Không thể xóa sản phẩm");
        }
    };

    const handleAddCategory = async (e) => {
        e.preventDefault();
        if (!newCategoryName.trim() || !store?.id) return;
        try {
            await categoryApi.create({
                name: newCategoryName.trim(),
                storeId: store.id,
            });
            setNewCategoryName("");
            const cats = await categoryApi.getByStore(store.id);
            setCategories(cats || []);
        } catch (err) {
            alert(err.message || "Không thể thêm danh mục");
        }
    };

    const handleDeleteCategory = async (id) => {
        if (!window.confirm("Xóa danh mục này?")) return;
        try {
            await categoryApi.delete(id);
            const cats = await categoryApi.getByStore(store.id);
            setCategories(cats || []);
        } catch (err) {
            alert(err.message || "Không thể xóa danh mục");
        }
    };

    const filtered = products.filter((p) => {
        const matchesCategory =
            !selectedCategory || p.categoryId === selectedCategory || p.category?.id === selectedCategory;
        const q = searchQuery.toLowerCase().trim();
        const matchesQuery =
            !q ||
            p.name?.toLowerCase().includes(q) ||
            p.sku?.toLowerCase().includes(q) ||
            p.barcode?.toLowerCase().includes(q);
        return matchesCategory && matchesQuery;
    });

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Quản Lý Sản Phẩm
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Danh sách các mặt hàng, giá cả và thiết lập danh mục
                    </p>
                </div>
                <div className="flex items-center gap-2.5">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setIsCategoryModalOpen(true)}
                        className="gap-1.5 h-9 cursor-pointer"
                    >
                        <FolderPlus className="size-4 text-violet-500" />
                        Danh Mục ({categories.length})
                    </Button>
                    <Button
                        size="sm"
                        onClick={handleOpenAddModal}
                        className="bg-blue-600 hover:bg-blue-700 text-white gap-1.5 h-9 font-medium shadow-md shadow-blue-500/20 cursor-pointer"
                    >
                        <Plus className="size-4" />
                        Thêm Sản Phẩm
                    </Button>
                </div>
            </div>

            {/* Filter & Search Bar */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-4 flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                        <Input
                            placeholder="Tìm kiếm theo tên sản phẩm, SKU hoặc mã vạch..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-9 h-9 text-sm"
                        />
                    </div>
                    <select
                        value={selectedCategory}
                        onChange={(e) => setSelectedCategory(e.target.value)}
                        className="text-xs rounded-lg border border-border bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-1 focus:ring-primary min-w-[160px]"
                    >
                        <option value="">Tất cả danh mục</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.name}
                            </option>
                        ))}
                    </select>
                </CardContent>
            </Card>

            {/* Products Table */}
            <Card className="border-border/60 bg-card/60">
                <CardContent className="p-0">
                    {isLoading ? (
                        <div className="py-12 text-center text-sm text-muted-foreground">Đang tải sản phẩm...</div>
                    ) : filtered.length === 0 ? (
                        <div className="py-12 text-center text-sm text-muted-foreground flex flex-col items-center gap-2">
                            <Package className="size-10 text-muted-foreground/40" />
                            <span>Không tìm thấy sản phẩm nào.</span>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-xs border-collapse">
                                <thead>
                                    <tr className="border-b border-border/60 bg-muted/30 text-muted-foreground font-semibold uppercase text-[11px] tracking-wider">
                                        <th className="py-3 px-4">Sản phẩm</th>
                                        <th className="py-3 px-4">SKU / Mã vạch</th>
                                        <th className="py-3 px-4">Danh mục</th>
                                        <th className="py-3 px-4 text-right">Giá bán</th>
                                        <th className="py-3 px-4 text-right">Giá vốn</th>
                                        <th className="py-3 px-4 text-center">Tồn tối thiểu</th>
                                        <th className="py-3 px-4 text-center">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-border/40">
                                    {filtered.map((product) => (
                                        <tr key={product.id} className="hover:bg-muted/20 transition-colors">
                                            <td className="py-3 px-4 flex items-center gap-3">
                                                <div className="size-10 rounded-lg bg-muted/60 overflow-hidden shrink-0 border border-border/40">
                                                    {product.image ? (
                                                        <img
                                                            src={product.image}
                                                            alt={product.name}
                                                            className="w-full h-full object-cover"
                                                        />
                                                    ) : (
                                                        <div className="w-full h-full flex items-center justify-center text-blue-500 bg-blue-500/10">
                                                            <Sparkles className="size-4" />
                                                        </div>
                                                    )}
                                                </div>
                                                <div className="min-w-0">
                                                    <p className="font-semibold text-foreground truncate max-w-xs">
                                                        {product.name}
                                                    </p>
                                                    {product.brand && (
                                                        <p className="text-[11px] text-muted-foreground">
                                                            Thương hiệu: {product.brand}
                                                        </p>
                                                    )}
                                                </div>
                                            </td>
                                            <td className="py-3 px-4 font-mono text-[11px]">
                                                <div>{product.sku}</div>
                                                {product.barcode && product.barcode !== product.sku && (
                                                    <div className="text-muted-foreground">{product.barcode}</div>
                                                )}
                                            </td>
                                            <td className="py-3 px-4">
                                                <Badge variant="outline">
                                                    {product.category?.name || "Chưa phân loại"}
                                                </Badge>
                                            </td>
                                            <td className="py-3 px-4 text-right font-bold text-blue-600 dark:text-blue-400">
                                                {(product.sellingPrice || 0).toLocaleString("vi-VN")} ₫
                                            </td>
                                            <td className="py-3 px-4 text-right text-muted-foreground">
                                                {(product.costPrice || 0).toLocaleString("vi-VN")} ₫
                                            </td>
                                            <td className="py-3 px-4 text-center font-medium">
                                                {product.minStockLevel || 5}
                                            </td>
                                            <td className="py-3 px-4 text-center">
                                                <div className="flex items-center justify-center gap-1.5">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={() => handleOpenEditModal(product)}
                                                        className="text-muted-foreground hover:text-foreground cursor-pointer"
                                                    >
                                                        <Edit2 className="size-3.5" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={() => handleDeleteProduct(product.id)}
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

            {/* Product Add/Edit Modal */}
            <Modal
                isOpen={isProductModalOpen}
                onClose={() => setIsProductModalOpen(false)}
                title={editingProduct ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới"}
                maxWidth="max-w-xl"
            >
                <form onSubmit={handleSaveProduct} className="space-y-3.5">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Tên sản phẩm *</label>
                        <Input
                            placeholder="Ví dụ: Cà Phê Sữa Lon 235ml"
                            value={productForm.name}
                            onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                            required
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Mã SKU *</label>
                            <Input
                                placeholder="SKU-001"
                                value={productForm.sku}
                                onChange={(e) => setProductForm({ ...productForm, sku: e.target.value })}
                                required
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Mã vạch (Barcode)</label>
                            <Input
                                placeholder="893456789012"
                                value={productForm.barcode}
                                onChange={(e) => setProductForm({ ...productForm, barcode: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Danh mục</label>
                            <select
                                value={productForm.categoryId}
                                onChange={(e) => setProductForm({ ...productForm, categoryId: e.target.value })}
                                className="w-full text-xs rounded-lg border border-border bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
                            >
                                <option value="">Chọn danh mục</option>
                                {categories.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {c.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Thương hiệu</label>
                            <Input
                                placeholder="Highlands Coffee, v.v."
                                value={productForm.brand}
                                onChange={(e) => setProductForm({ ...productForm, brand: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-3 gap-3">
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Giá bán (₫) *</label>
                            <Input
                                type="number"
                                placeholder="15000"
                                value={productForm.sellingPrice}
                                onChange={(e) => setProductForm({ ...productForm, sellingPrice: e.target.value })}
                                required
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Giá vốn (₫)</label>
                            <Input
                                type="number"
                                placeholder="10000"
                                value={productForm.costPrice}
                                onChange={(e) => setProductForm({ ...productForm, costPrice: e.target.value })}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-semibold">Tồn tối thiểu</label>
                            <Input
                                type="number"
                                placeholder="5"
                                value={productForm.minStockLevel}
                                onChange={(e) => setProductForm({ ...productForm, minStockLevel: e.target.value })}
                            />
                        </div>
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Hình ảnh (URL)</label>
                        <Input
                            placeholder="https://example.com/image.jpg"
                            value={productForm.image}
                            onChange={(e) => setProductForm({ ...productForm, image: e.target.value })}
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Mô tả sản phẩm</label>
                        <Input
                            placeholder="Ghi chú chi tiết sản phẩm..."
                            value={productForm.description}
                            onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                        />
                    </div>

                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white cursor-pointer mt-2">
                        {editingProduct ? "Lưu Thay Đổi" : "Tạo Sản Phẩm"}
                    </Button>
                </form>
            </Modal>

            {/* Categories Management Modal */}
            <Modal
                isOpen={isCategoryModalOpen}
                onClose={() => setIsCategoryModalOpen(false)}
                title="Quản lý danh mục hàng hóa"
                maxWidth="max-w-md"
            >
                <div className="space-y-4">
                    <form onSubmit={handleAddCategory} className="flex gap-2">
                        <Input
                            placeholder="Tên danh mục mới..."
                            value={newCategoryName}
                            onChange={(e) => setNewCategoryName(e.target.value)}
                            className="text-xs"
                            required
                        />
                        <Button type="submit" size="sm" className="bg-blue-600 text-white text-xs shrink-0 cursor-pointer">
                            Thêm mới
                        </Button>
                    </form>

                    <div className="divide-y divide-border/60 max-h-60 overflow-y-auto">
                        {categories.map((c) => (
                            <div key={c.id} className="py-2.5 flex items-center justify-between text-xs">
                                <span className="font-medium text-foreground">{c.name}</span>
                                <Button
                                    variant="ghost"
                                    size="icon-xs"
                                    onClick={() => handleDeleteCategory(c.id)}
                                    className="text-muted-foreground hover:text-destructive cursor-pointer"
                                >
                                    <Trash2 className="size-3.5" />
                                </Button>
                            </div>
                        ))}
                    </div>
                </div>
            </Modal>
        </div>
    );
}
