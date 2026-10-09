import React, { useState, useEffect } from "react";
import {
    Search,
    Barcode,
    Plus,
    Minus,
    Trash2,
    CreditCard,
    Banknote,
    QrCode,
    UserCheck,
    UserPlus,
    Receipt,
    Printer,
    CheckCircle2,
    X,
    Sparkles,
    ShoppingCart,
    History,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { useCartStore } from "@/store/useCartStore";
import { productApi, categoryApi, customerApi, orderApi, inventoryApi } from "@/services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/ui/dialog";
import StripePaymentModal from "@/components/payment/StripePaymentModal";
import PaymentHistoryModal from "@/components/payment/PaymentHistoryModal";

export default function POS() {
    const { branch, store, user } = useAuthStore();
    const {
        items,
        customer,
        discountType,
        discountValue,
        taxRate,
        notes,
        addItem,
        removeItem,
        updateQuantity,
        setCustomer,
        setDiscount,
        setTaxRate,
        setNotes,
        clearCart,
        getSubtotal,
        getDiscountAmount,
        getTaxAmount,
        getTotalAmount,
        getItemCount,
    } = useCartStore();

    // Local states
    const [products, setProducts] = useState([]);
    const [inventoryMap, setInventoryMap] = useState({});
    const [categories, setCategories] = useState([]);
    const [customers, setCustomers] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState("ALL");
    const [searchQuery, setSearchQuery] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    // Modal states
    const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
    const [isReceiptOpen, setIsReceiptOpen] = useState(false);
    const [isNewCustomerOpen, setIsNewCustomerOpen] = useState(false);
    const [completedOrder, setCompletedOrder] = useState(null);
    const [isStripeModalOpen, setIsStripeModalOpen] = useState(false);
    const [isPaymentHistoryOpen, setIsPaymentHistoryOpen] = useState(false);
    const [pendingOrderForStripe, setPendingOrderForStripe] = useState(null);

    // Payment states
    const [paymentMethod, setPaymentMethod] = useState("CASH");
    const [cashTendered, setCashTendered] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    // New Customer Form State
    const [newCustomerData, setNewCustomerData] = useState({ fullName: "", phone: "", email: "" });

    // Fetch initial POS data
    useEffect(() => {
        if (!store?.id || !branch?.id) return;
        const loadPOSData = async () => {
            setIsLoading(true);
            try {
                const [prods, cats, custs, invs] = await Promise.all([
                    productApi.getByStore(store.id).catch(() => []),
                    categoryApi.getByStore(store.id).catch(() => []),
                    customerApi.getAll().catch(() => []),
                    inventoryApi.getByBranch(branch.id).catch(() => []),
                ]);

                setProducts(prods || []);
                setCategories(cats || []);
                setCustomers(custs || []);

                const invMapping = {};
                (invs || []).forEach((inv) => {
                    if (inv.productId) {
                        invMapping[inv.productId] = inv.quantity;
                    }
                });
                setInventoryMap(invMapping);
            } catch (err) {
                console.error("Lỗi khi tải dữ liệu POS:", err);
            } finally {
                setIsLoading(false);
            }
        };

        loadPOSData();
    }, [store?.id, branch?.id]);

    // Filter products
    const filteredProducts = products.filter((p) => {
        const matchesCategory =
            selectedCategory === "ALL" || p.categoryId === selectedCategory || p.category?.id === selectedCategory;
        const query = searchQuery.toLowerCase().trim();
        const matchesQuery =
            !query ||
            p.name?.toLowerCase().includes(query) ||
            p.sku?.toLowerCase().includes(query) ||
            p.barcode?.toLowerCase().includes(query);
        return matchesCategory && matchesQuery;
    });

    // Validate stock before adding to cart or increasing quantity
    const handleProductClick = (product) => {
        const availableStock = inventoryMap[product.id] ?? 0;
        const currentCartItem = items.find((i) => i.product.id === product.id);
        const currentInCart = currentCartItem ? currentCartItem.quantity : 0;

        if (availableStock <= 0) {
            alert(`Sản phẩm "${product.name}" hiện đã HẾT HÀNG trong kho của chi nhánh! Không thể thêm vào giỏ hàng.`);
            return;
        }

        if (currentInCart + 1 > availableStock) {
            alert(`Không đủ tồn kho! Sản phẩm "${product.name}" hiện chỉ còn ${availableStock} cái trong kho (trong giỏ hàng đã có ${currentInCart} cái).`);
            return;
        }

        addItem(product, 1);
    };

    const handleIncreaseQuantity = (item) => {
        const availableStock = inventoryMap[item.product.id] ?? 0;
        if (item.quantity + 1 > availableStock) {
            alert(`Không thể tăng thêm! Sản phẩm "${item.product.name}" chỉ còn tối đa ${availableStock} cái trong kho.`);
            return;
        }
        updateQuantity(item.product.id, item.quantity + 1);
    };

    // Pre-checkout stock validation across all cart items
    const validateStockBeforeCheckout = () => {
        for (const item of items) {
            const availableStock = inventoryMap[item.product.id] ?? 0;
            if (availableStock <= 0) {
                alert(`Không thể thanh toán! Sản phẩm "${item.product.name}" hiện đã hết hàng trong kho. Vui lòng xóa khỏi giỏ.`);
                return false;
            }
            if (item.quantity > availableStock) {
                alert(`Không thể thanh toán! Sản phẩm "${item.product.name}" có số lượng ${item.quantity} cái nhưng tồn kho chỉ còn ${availableStock} cái. Vui lòng giảm số lượng.`);
                return false;
            }
        }
        return true;
    };

    // Handle Checkout Open with stock validation
    const handleOpenCheckout = () => {
        if (items.length === 0) return;
        if (!validateStockBeforeCheckout()) return;
        setCashTendered(getTotalAmount().toString());
        setIsCheckoutOpen(true);
    };

    // Submit Order with backend & frontend stock validation
    const handleCompleteOrder = async () => {
        if (!validateStockBeforeCheckout()) return;
        setIsSubmitting(true);
        try {
            const orderPayload = {
                branchId: branch?.id,
                customerId: customer?.id || null,
                paymentType: paymentMethod,
                discount: getDiscountAmount(),
                tax: getTaxAmount(),
                notes: notes,
                items: items.map((i) => ({
                    productId: i.product.id,
                    quantity: i.quantity,
                })),
            };

            const createdOrder = await orderApi.create(orderPayload);

            if (paymentMethod === "STRIPE") {
                setPendingOrderForStripe(createdOrder);
                setIsCheckoutOpen(false);
                setIsStripeModalOpen(true);
                return;
            }

            setCompletedOrder(createdOrder);
            clearCart();
            setIsCheckoutOpen(false);
            setIsReceiptOpen(true);

            // Refresh branch inventory after confirmed order
            if (branch?.id) {
                const invs = await inventoryApi.getByBranch(branch.id).catch(() => []);
                const invMapping = {};
                (invs || []).forEach((inv) => {
                    if (inv.productId) invMapping[inv.productId] = inv.quantity;
                });
                setInventoryMap(invMapping);
            }
        } catch (err) {
            const msg = err.response?.data?.message || err.message || "Không thể tạo đơn hàng";
            alert(msg);
        } finally {
            setIsSubmitting(false);
        }
    };

    // Callback when Stripe card checkout succeeds
    const handleStripePaymentSuccess = async (paymentResult) => {
        setIsStripeModalOpen(false);
        setCompletedOrder(pendingOrderForStripe);
        clearCart();
        setIsReceiptOpen(true);

        if (branch?.id) {
            const invs = await inventoryApi.getByBranch(branch.id).catch(() => []);
            const invMapping = {};
            (invs || []).forEach((inv) => {
                if (inv.productId) invMapping[inv.productId] = inv.quantity;
            });
            setInventoryMap(invMapping);
        }
    };

    // Quick Add Customer
    const handleCreateCustomer = async (e) => {
        e.preventDefault();
        try {
            const newCust = await customerApi.create(newCustomerData);
            setCustomers([...customers, newCust]);
            setCustomer(newCust);
            setIsNewCustomerOpen(false);
            setNewCustomerData({ fullName: "", phone: "", email: "" });
        } catch (err) {
            alert(err.message || "Không thể tạo khách hàng");
        }
    };

    const total = getTotalAmount();
    const tendered = Number(cashTendered) || 0;
    const changeAmount = Math.max(0, tendered - total);

    return (
        <div className="h-[calc(100vh-6rem)] flex flex-col lg:flex-row gap-4 -m-2">
            {/* Left Column: Product Selection Grid */}
            <div className="flex-1 flex flex-col min-w-0 bg-card/60 backdrop-blur-xs rounded-xl border border-border/60 p-4">
                {/* Search & Barcode Scan Bar */}
                <div className="flex items-center gap-3 mb-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
                        <Input
                            placeholder="Tìm sản phẩm theo tên, SKU hoặc quét mã vạch..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-9 h-10 text-sm bg-background"
                        />
                    </div>
                    <Button
                        variant="outline"
                        onClick={() => setIsPaymentHistoryOpen(true)}
                        className="h-10 text-xs gap-1.5 cursor-pointer border-blue-200 dark:border-blue-900 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/40 shrink-0 font-semibold"
                    >
                        <History className="size-4" />
                        Lịch sử TT
                    </Button>
                </div>

                {/* Category Filter Tabs */}
                <div className="flex items-center gap-2 overflow-x-auto pb-2 mb-3 scrollbar-none">
                    <button
                        onClick={() => setSelectedCategory("ALL")}
                        className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors cursor-pointer ${
                            selectedCategory === "ALL"
                                ? "bg-primary text-primary-foreground shadow-2xs"
                                : "bg-secondary text-muted-foreground hover:text-foreground"
                        }`}
                    >
                        Tất cả ({products.length})
                    </button>
                    {categories.map((cat) => (
                        <button
                            key={cat.id}
                            onClick={() => setSelectedCategory(cat.id)}
                            className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors cursor-pointer ${
                                selectedCategory === cat.id
                                    ? "bg-primary text-primary-foreground shadow-2xs"
                                    : "bg-secondary text-muted-foreground hover:text-foreground"
                            }`}
                        >
                            {cat.name}
                        </button>
                    ))}
                </div>

                {/* Products Grid */}
                <div className="flex-1 overflow-y-auto pr-1">
                    {isLoading ? (
                        <div className="h-full flex items-center justify-center text-sm text-muted-foreground">
                            Đang tải danh mục sản phẩm...
                        </div>
                    ) : filteredProducts.length === 0 ? (
                        <div className="h-full flex flex-col items-center justify-center text-sm text-muted-foreground gap-2">
                            <ShoppingCart className="size-10 text-muted-foreground/40" />
                            <span>Không tìm thấy sản phẩm phù hợp.</span>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-4 gap-3">
                            {filteredProducts.map((p) => {
                                const stock = inventoryMap[p.id] ?? 0;
                                const isOutOfStock = stock <= 0;

                                return (
                                    <div
                                        key={p.id}
                                        onClick={() => handleProductClick(p)}
                                        className={`group relative rounded-xl border p-3 transition-all cursor-pointer flex flex-col justify-between ${
                                            isOutOfStock
                                                ? "opacity-60 bg-muted/15 border-destructive/20 hover:border-destructive/40"
                                                : "bg-card border-border/60 hover:border-blue-500/50 hover:shadow-md"
                                        }`}
                                    >
                                        <div>
                                            {/* Image or fallback */}
                                            <div className="aspect-square w-full rounded-lg bg-muted/50 overflow-hidden mb-2 relative">
                                                {p.image ? (
                                                    <img
                                                        src={p.image}
                                                        alt={p.name}
                                                        className={`w-full h-full object-cover transition-transform duration-300 ${
                                                            isOutOfStock ? "grayscale" : "group-hover:scale-105"
                                                        }`}
                                                    />
                                                ) : (
                                                    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-500/10 to-indigo-500/10 text-blue-600">
                                                        <Sparkles className="size-6 opacity-60" />
                                                    </div>
                                                )}
                                                <div className="absolute top-1.5 right-1.5">
                                                    <Badge
                                                        variant={isOutOfStock ? "destructive" : stock <= (p.minStockLevel || 5) ? "warning" : "secondary"}
                                                        className="text-[10px] px-1.5 py-0 shadow-xs"
                                                    >
                                                        {isOutOfStock ? "Hết hàng" : `Kho: ${stock}`}
                                                    </Badge>
                                                </div>
                                            </div>

                                            <h4 className="font-semibold text-xs text-foreground line-clamp-2 leading-tight">
                                                {p.name}
                                            </h4>
                                            <p className="text-[11px] text-muted-foreground font-mono mt-0.5">
                                                {p.sku}
                                            </p>
                                        </div>

                                        <div className="mt-2 pt-2 border-t border-border/40 flex items-center justify-between">
                                            <span className="font-bold text-sm text-blue-600 dark:text-blue-400">
                                                {(p.sellingPrice || 0).toLocaleString("vi-VN")} ₫
                                            </span>
                                            <div className={`size-7 rounded-lg flex items-center justify-center transition-colors ${
                                                isOutOfStock
                                                    ? "bg-destructive/10 text-destructive"
                                                    : "bg-secondary group-hover:bg-blue-600 group-hover:text-white"
                                            }`}>
                                                {isOutOfStock ? <X className="size-3.5" /> : <Plus className="size-3.5" />}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>

            {/* Right Column: POS Cart & Bill Drawer */}
            <div className="w-full lg:w-96 bg-card/90 backdrop-blur-md rounded-xl border border-border/80 flex flex-col shrink-0 h-full">
                {/* Cart Customer Header */}
                <div className="p-3.5 border-b border-border/60 bg-muted/20">
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                            <UserCheck className="size-3.5 text-blue-500" />
                            Khách hàng
                        </span>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setIsNewCustomerOpen(true)}
                            className="h-6 text-[11px] gap-1 px-2 text-blue-600 dark:text-blue-400 hover:bg-blue-500/10 cursor-pointer"
                        >
                            <UserPlus className="size-3" /> Thêm khách
                        </Button>
                    </div>

                    <select
                        value={customer?.id || ""}
                        onChange={(e) => {
                            const val = e.target.value;
                            if (!val) setCustomer(null);
                            else {
                                const found = customers.find((c) => c.id === val);
                                if (found) setCustomer(found);
                            }
                        }}
                        className="w-full text-xs rounded-lg border border-border bg-background px-2.5 py-1.5 text-foreground shadow-2xs focus:outline-none focus:ring-1 focus:ring-primary"
                    >
                        <option value="">Khách vãng lai (Không tích điểm)</option>
                        {customers.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.fullName} - {c.phone || c.email} ({c.loyaltyPoints || 0} điểm)
                            </option>
                        ))}
                    </select>
                </div>

                {/* Cart Items List */}
                <div className="flex-1 overflow-y-auto p-3 space-y-2">
                    {items.length === 0 ? (
                        <div className="h-full flex flex-col items-center justify-center text-xs text-muted-foreground gap-2 py-12">
                            <ShoppingCart className="size-8 text-muted-foreground/40" />
                            <span>Giỏ hàng trống. Chọn sản phẩm bên trái!</span>
                        </div>
                    ) : (
                        items.map((item) => {
                            const itemStock = inventoryMap[item.product.id] ?? 0;
                            const isExceeded = item.quantity > itemStock;

                            return (
                                <div
                                    key={item.product.id}
                                    className={`p-2.5 rounded-lg border flex items-center justify-between gap-3 text-xs transition-colors ${
                                        isExceeded
                                            ? "bg-destructive/10 border-destructive/50"
                                            : "bg-muted/30 border-border/40"
                                    }`}
                                >
                                    <div className="min-w-0 flex-1">
                                        <p className="font-semibold text-foreground truncate">{item.product.name}</p>
                                        <p className="text-[11px] text-muted-foreground font-mono mt-0.5">
                                            {(item.unitPrice || 0).toLocaleString("vi-VN")} ₫
                                        </p>
                                        {isExceeded && (
                                            <p className="text-[10px] text-destructive font-bold mt-0.5">
                                                ⚠️ Vượt quá tồn kho (Kho còn {itemStock})
                                            </p>
                                        )}
                                    </div>

                                    {/* Quantity Control */}
                                    <div className="flex items-center gap-1 bg-background rounded-md border border-border p-0.5">
                                        <button
                                            onClick={() => updateQuantity(item.product.id, item.quantity - 1)}
                                            className="size-5 rounded flex items-center justify-center hover:bg-muted text-muted-foreground cursor-pointer"
                                        >
                                            <Minus className="size-3" />
                                        </button>
                                        <span className={`w-6 text-center font-bold text-xs ${isExceeded ? "text-destructive" : ""}`}>
                                            {item.quantity}
                                        </span>
                                        <button
                                            onClick={() => handleIncreaseQuantity(item)}
                                            className="size-5 rounded flex items-center justify-center hover:bg-muted text-muted-foreground cursor-pointer"
                                        >
                                            <Plus className="size-3" />
                                        </button>
                                    </div>

                                    <div className="text-right shrink-0">
                                        <div className="font-bold text-foreground">
                                            {(item.subtotal || 0).toLocaleString("vi-VN")} ₫
                                        </div>
                                        <button
                                            onClick={() => removeItem(item.product.id)}
                                            className="text-muted-foreground hover:text-destructive text-[10px] mt-0.5 cursor-pointer"
                                        >
                                            Xóa
                                        </button>
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>

                {/* Bill Pricing Breakdown Footer */}
                <div className="p-4 border-t border-border/60 bg-muted/15 space-y-2">
                    <div className="flex justify-between text-xs text-muted-foreground">
                        <span>Tạm tính ({getItemCount()} món)</span>
                        <span className="font-medium text-foreground">
                            {getSubtotal().toLocaleString("vi-VN")} ₫
                        </span>
                    </div>

                    <div className="flex items-center justify-between text-xs">
                        <span className="text-muted-foreground">Giảm giá</span>
                        <div className="flex items-center gap-1.5">
                            <Input
                                type="number"
                                placeholder="0"
                                value={discountValue || ""}
                                onChange={(e) => setDiscount(discountType, e.target.value)}
                                className="h-6 w-20 text-xs px-2 text-right bg-background"
                            />
                            <span className="text-muted-foreground">₫</span>
                        </div>
                    </div>

                    <div className="flex justify-between text-xs text-muted-foreground">
                        <span>Thuế VAT (8%)</span>
                        <span className="font-medium text-foreground">
                            {Math.round(getTaxAmount()).toLocaleString("vi-VN")} ₫
                        </span>
                    </div>

                    <div className="pt-2 border-t border-border/60 flex justify-between items-baseline">
                        <span className="font-bold text-sm text-foreground">Tổng thanh toán</span>
                        <span className="font-black text-xl text-blue-600 dark:text-blue-400">
                            {total.toLocaleString("vi-VN")} ₫
                        </span>
                    </div>

                    {/* Bottom Action Buttons */}
                    <div className="grid grid-cols-3 gap-2 pt-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={clearCart}
                            disabled={items.length === 0}
                            className="text-xs text-muted-foreground hover:text-destructive cursor-pointer"
                        >
                            <Trash2 className="size-3.5" />
                        </Button>
                        <Button
                            size="sm"
                            onClick={handleOpenCheckout}
                            disabled={items.length === 0}
                            className="col-span-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs h-10 shadow-md shadow-blue-500/25 cursor-pointer"
                        >
                            Thanh Toán ({total.toLocaleString("vi-VN")} ₫)
                        </Button>
                    </div>
                </div>
            </div>

            {/* Checkout Payment Modal */}
            <Modal
                isOpen={isCheckoutOpen}
                onClose={() => setIsCheckoutOpen(false)}
                title="Xác nhận thanh toán đơn hàng"
                description={`Tổng thanh toán: ${total.toLocaleString("vi-VN")} ₫`}
                maxWidth="max-w-md"
            >
                <div className="space-y-4">
                    {/* Payment Method Selector */}
                    <div>
                        <label className="text-xs font-semibold text-foreground mb-2 block">
                            Phương thức thanh toán
                        </label>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                            <button
                                type="button"
                                onClick={() => setPaymentMethod("CASH")}
                                className={`p-3 rounded-xl border flex flex-col items-center gap-1.5 text-xs font-semibold transition-all cursor-pointer ${
                                    paymentMethod === "CASH"
                                        ? "border-blue-600 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 shadow-2xs"
                                        : "border-border hover:bg-muted text-muted-foreground"
                                }`}
                            >
                                <Banknote className="size-5" />
                                Tiền mặt
                            </button>
                            <button
                                type="button"
                                onClick={() => setPaymentMethod("CARD")}
                                className={`p-3 rounded-xl border flex flex-col items-center gap-1.5 text-xs font-semibold transition-all cursor-pointer ${
                                    paymentMethod === "CARD"
                                        ? "border-blue-600 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 shadow-2xs"
                                        : "border-border hover:bg-muted text-muted-foreground"
                                }`}
                            >
                                <CreditCard className="size-5" />
                                Quẹt thẻ
                            </button>
                            <button
                                type="button"
                                onClick={() => setPaymentMethod("UPI")}
                                className={`p-3 rounded-xl border flex flex-col items-center gap-1.5 text-xs font-semibold transition-all cursor-pointer ${
                                    paymentMethod === "UPI"
                                        ? "border-blue-600 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 shadow-2xs"
                                        : "border-border hover:bg-muted text-muted-foreground"
                                }`}
                            >
                                <QrCode className="size-5" />
                                Chuyển khoản QR
                            </button>
                            <button
                                type="button"
                                onClick={() => setPaymentMethod("STRIPE")}
                                className={`p-3 rounded-xl border flex flex-col items-center gap-1.5 text-xs font-semibold transition-all cursor-pointer ${
                                    paymentMethod === "STRIPE"
                                        ? "border-blue-600 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 shadow-2xs ring-1 ring-blue-500"
                                        : "border-border hover:bg-muted text-muted-foreground"
                                }`}
                            >
                                <CreditCard className="size-5 text-indigo-500" />
                                Thẻ Stripe
                            </button>
                        </div>
                    </div>

                    {/* Stripe Info Notice */}
                    {paymentMethod === "STRIPE" && (
                        <div className="p-3.5 rounded-xl bg-blue-50/70 dark:bg-blue-950/30 border border-blue-200/80 dark:border-blue-800/40 space-y-1.5 text-xs text-blue-950 dark:text-blue-200">
                            <div className="flex items-center gap-2 font-semibold text-blue-600 dark:text-blue-400">
                                <CreditCard className="size-4" />
                                Cổng thanh toán Stripe Elements
                            </div>
                            <p className="text-[11px] text-muted-foreground">
                                Nhấn xác nhận để mở giao dịch thẻ Stripe bảo mật (hỗ trợ Visa, Mastercard, JCB hoặc chế độ mô phỏng Sandbox Test Mode).
                            </p>
                        </div>
                    )}

                    {/* Cash Calculation */}
                    {paymentMethod === "CASH" && (
                        <div className="space-y-3 p-3.5 rounded-xl bg-muted/40 border border-border/60">
                            <div>
                                <label className="text-xs font-medium text-muted-foreground">
                                    Tiền khách đưa (₫)
                                </label>
                                <Input
                                    type="number"
                                    value={cashTendered}
                                    onChange={(e) => setCashTendered(e.target.value)}
                                    className="text-lg font-bold mt-1 text-right"
                                />
                            </div>

                            {/* Preset Cash Buttons */}
                            <div className="flex flex-wrap gap-1.5">
                                {[total, 50000, 100000, 200000, 500000].map((val) => (
                                    <button
                                        key={val}
                                        type="button"
                                        onClick={() => setCashTendered(val.toString())}
                                        className="text-[11px] font-mono px-2 py-1 rounded bg-background border border-border hover:bg-secondary cursor-pointer"
                                    >
                                        {val.toLocaleString("vi-VN")}
                                    </button>
                                ))}
                            </div>

                            <div className="flex justify-between items-center pt-2 border-t border-border/60 text-xs">
                                <span className="font-medium text-muted-foreground">Tiền thối lại:</span>
                                <span className="font-bold text-sm text-emerald-600 dark:text-emerald-400">
                                    {changeAmount.toLocaleString("vi-VN")} ₫
                                </span>
                            </div>
                        </div>
                    )}

                    {/* Payment Notes */}
                    <div>
                        <label className="text-xs font-medium text-muted-foreground">Ghi chú đơn hàng</label>
                        <Input
                            placeholder="Ghi chú thêm (tùy chọn)..."
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                            className="text-xs mt-1"
                        />
                    </div>

                    <Button
                        type="button"
                        onClick={handleCompleteOrder}
                        disabled={isSubmitting || (paymentMethod === "CASH" && tendered < total)}
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold h-10 cursor-pointer shadow-md shadow-blue-500/20"
                    >
                        {isSubmitting
                            ? "Đang xử lý..."
                            : paymentMethod === "STRIPE"
                            ? "Tiến Hành Thanh Toán Thẻ Stripe"
                            : "Hoàn Tất Đơn & In Hóa Đơn"}
                    </Button>
                </div>
            </Modal>

            {/* Receipt Modal (Printable) */}
            <Modal
                isOpen={isReceiptOpen}
                onClose={() => setIsReceiptOpen(false)}
                title="Hóa đơn thanh toán"
                maxWidth="max-w-md"
            >
                {completedOrder && (
                    <div className="space-y-4">
                        {/* Printable Receipt Paper Container */}
                        <div id="pos-printable-receipt" className="p-4 bg-background border border-dashed border-border rounded-lg font-mono text-xs space-y-3">
                            <div className="text-center space-y-1 pb-3 border-b border-dashed border-border">
                                <h3 className="font-bold text-sm uppercase">{store?.branch || "SkyPOS Mart"}</h3>
                                <p className="text-[11px] text-muted-foreground">{branch?.address || "Chi nhánh chính"}</p>
                                <p className="text-[11px] text-muted-foreground">Hotline: {branch?.phone || "02838123456"}</p>
                                <h4 className="font-bold text-xs uppercase pt-1">PHIẾU THANH TOÁN</h4>
                            </div>

                            <div className="text-[11px] space-y-1 text-muted-foreground">
                                <div className="flex justify-between">
                                    <span>Mã đơn:</span>
                                    <span className="font-bold text-foreground">{completedOrder.orderNumber || completedOrder.id?.substring(0, 8)}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Thời gian:</span>
                                    <span>{new Date().toLocaleString("vi-VN")}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Thu ngân:</span>
                                    <span>{completedOrder.cashier?.fullName || user?.fullName || "Thu ngân"}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Khách hàng:</span>
                                    <span>{completedOrder.customer?.fullName || "Khách lẻ"}</span>
                                </div>
                            </div>

                            {/* Item list */}
                            <div className="border-t border-b border-dashed border-border py-2 space-y-1.5">
                                {completedOrder.items?.map((item, idx) => (
                                    <div key={idx} className="flex justify-between text-xs">
                                        <div className="min-w-0 pr-2">
                                            <p className="font-medium text-foreground truncate">{item.product?.name}</p>
                                            <p className="text-[10px] text-muted-foreground">
                                                {item.quantity} x {(item.product?.sellingPrice || 0).toLocaleString("vi-VN")}
                                            </p>
                                        </div>
                                        <span className="font-bold text-foreground shrink-0">
                                            {(item.price || 0).toLocaleString("vi-VN")} ₫
                                        </span>
                                    </div>
                                ))}
                            </div>

                            {/* Totals */}
                            <div className="space-y-1 text-xs">
                                <div className="flex justify-between text-muted-foreground">
                                    <span>Tạm tính:</span>
                                    <span>{(completedOrder.subtotal || completedOrder.totalAmount || 0).toLocaleString("vi-VN")} ₫</span>
                                </div>
                                {(completedOrder.discount || 0) > 0 && (
                                    <div className="flex justify-between text-muted-foreground">
                                        <span>Giảm giá:</span>
                                        <span>-{(completedOrder.discount || 0).toLocaleString("vi-VN")} ₫</span>
                                    </div>
                                )}
                                <div className="flex justify-between font-bold text-sm text-foreground pt-1 border-t border-border">
                                    <span>TỔNG CỘNG:</span>
                                    <span>{(completedOrder.totalAmount || 0).toLocaleString("vi-VN")} ₫</span>
                                </div>
                                <div className="flex justify-between text-[11px] text-muted-foreground">
                                    <span>Phương thức:</span>
                                    <span>{completedOrder.paymentType === "CASH" ? "Tiền mặt" : "Thẻ / Chuyển khoản"}</span>
                                </div>
                            </div>

                            <div className="text-center text-[11px] text-muted-foreground pt-2 border-t border-dashed border-border">
                                Cảm ơn quý khách và hẹn gặp lại!
                            </div>
                        </div>

                        {/* Action buttons */}
                        <div className="flex gap-2">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => window.print()}
                                className="flex-1 gap-1.5 cursor-pointer text-xs"
                            >
                                <Printer className="size-4" /> In Hóa Đơn
                            </Button>
                            <Button
                                type="button"
                                onClick={() => setIsReceiptOpen(false)}
                                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white cursor-pointer text-xs"
                            >
                                Tạo Đơn Mới
                            </Button>
                        </div>
                    </div>
                )}
            </Modal>

            {/* Quick Add Customer Modal */}
            <Modal
                isOpen={isNewCustomerOpen}
                onClose={() => setIsNewCustomerOpen(false)}
                title="Thêm khách hàng mới"
                maxWidth="max-w-sm"
            >
                <form onSubmit={handleCreateCustomer} className="space-y-3">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Tên khách hàng</label>
                        <Input
                            placeholder="Nguyễn Văn A"
                            value={newCustomerData.fullName}
                            onChange={(e) => setNewCustomerData({ ...newCustomerData, fullName: e.target.value })}
                            required
                        />
                    </div>
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Số điện thoại</label>
                        <Input
                            placeholder="0912345678"
                            value={newCustomerData.phone}
                            onChange={(e) => setNewCustomerData({ ...newCustomerData, phone: e.target.value })}
                            required
                        />
                    </div>
                    <div className="space-y-1">
                        <label className="text-xs font-semibold">Email (tùy chọn)</label>
                        <Input
                            type="email"
                            placeholder="khach@example.com"
                            value={newCustomerData.email}
                            onChange={(e) => setNewCustomerData({ ...newCustomerData, email: e.target.value })}
                        />
                    </div>
                    <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white text-xs mt-2 cursor-pointer">
                        Lưu & Chọn Khách Hàng
                    </Button>
                </form>
            </Modal>

            {/* Stripe Online Card Checkout Modal */}
            <StripePaymentModal
                isOpen={isStripeModalOpen}
                onClose={() => setIsStripeModalOpen(false)}
                order={pendingOrderForStripe}
                amount={pendingOrderForStripe?.totalAmount || total}
                customer={customer}
                onSuccess={handleStripePaymentSuccess}
            />

            {/* Payment Transactions History & Refund Modal */}
            <PaymentHistoryModal
                isOpen={isPaymentHistoryOpen}
                onClose={() => setIsPaymentHistoryOpen(false)}
            />
        </div>
    );
}
