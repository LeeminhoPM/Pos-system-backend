import React, { useState, useEffect } from "react";
import { publicCatalogApi, vietnamPaymentApi } from "@/services/api";
import { ShoppingBag, Search, Tag, Check, ShoppingCart, QrCode, X } from "lucide-react";

export default function WebShopPage() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState("");
    const [searchKeyword, setSearchKeyword] = useState("");
    const [cart, setCart] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showCart, setShowCart] = useState(false);
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [paymentQR, setPaymentQR] = useState(null);

    // Form đặt hàng
    const [recipientName, setRecipientName] = useState("");
    const [recipientPhone, setRecipientPhone] = useState("");
    const [shippingAddress, setShippingAddress] = useState("");
    const [paymentMethod, setPaymentMethod] = useState("BANK_TRANSFER");

    useEffect(() => {
        loadData();
    }, [selectedCategory]);

    const loadData = async () => {
        setLoading(true);
        try {
            const [prodRes, catRes] = await Promise.all([
                publicCatalogApi.getCatalog({
                    categoryId: selectedCategory || undefined,
                    keyword: searchKeyword || undefined
                }),
                publicCatalogApi.getCategories().catch(() => ({ data: { data: [] } }))
            ]);
            setProducts(prodRes?.data?.data || []);
            setCategories(catRes?.data?.data || []);
        } catch (err) {
            console.error("Lỗi tải menu sản phẩm:", err);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = (product) => {
        setCart(prev => {
            const existing = prev.find(item => item.id === product.id);
            if (existing) {
                return prev.map(item => item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item);
            }
            return [...prev, { ...product, quantity: 1 }];
        });
    };

    const cartTotal = cart.reduce((sum, item) => sum + (item.sellingPrice * item.quantity), 0);

    const handleCheckout = async (e) => {
        e.preventDefault();
        if (cart.length === 0) return;

        try {
            // Giả định branchId từ default store
            const orderPayload = {
                branchId: "11111111-1111-1111-1111-111111111111",
                recipientName,
                recipientPhone,
                shippingAddress,
                shippingFee: 20000.0,
                paymentType: paymentMethod,
                items: cart.map(i => ({ productId: i.id, quantity: i.quantity }))
            };

            const res = await publicCatalogApi.checkout(orderPayload);
            const createdDelivery = res?.data?.data;

            if (paymentMethod === "BANK_TRANSFER") {
                const qrRes = await vietnamPaymentApi.createVietQR({
                    orderId: createdDelivery.orderId
                });
                setPaymentQR(qrRes?.data?.data?.qrCodeUrl);
                setShowPaymentModal(true);
            } else if (paymentMethod === "MOMO") {
                const momoRes = await vietnamPaymentApi.createEWallet({
                    orderId: createdDelivery.orderId,
                    walletType: "MOMO"
                });
                setPaymentQR(momoRes?.data?.data?.qrCodeUrl);
                setShowPaymentModal(true);
            } else {
                alert("Đặt hàng thành công! Thu tiền khi giao hàng (COD).");
                setCart([]);
                setShowCart(false);
            }
        } catch (err) {
            alert(err?.response?.data?.message || err.message || "Đặt hàng thất bại");
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-800 dark:text-slate-100 p-6">
            <header className="max-w-7xl mx-auto flex items-center justify-between pb-6 border-b border-slate-200 dark:border-slate-800">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white font-bold text-lg shadow-md">
                        S
                    </div>
                    <div>
                        <h1 className="text-xl font-bold tracking-tight">SkyPOS Web Shop</h1>
                        <p className="text-xs text-slate-500">Menu & Danh mục sản phẩm trực tuyến</p>
                    </div>
                </div>

                <button
                    onClick={() => setShowCart(true)}
                    className="relative flex items-center gap-2 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-medium shadow-sm transition"
                >
                    <ShoppingCart className="w-5 h-5" />
                    <span>Giỏ Hàng</span>
                    {cart.length > 0 && (
                        <span className="absolute -top-1.5 -right-1.5 bg-rose-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
                            {cart.length}
                        </span>
                    )}
                </button>
            </header>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto mt-6 grid grid-cols-1 md:grid-cols-4 gap-6">
                {/* Category Sidebar */}
                <div className="md:col-span-1 space-y-2 bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200 dark:border-slate-700 h-fit">
                    <h3 className="font-semibold text-sm uppercase text-slate-400 mb-3">Ngành Hàng</h3>
                    <button
                        onClick={() => setSelectedCategory("")}
                        className={`w-full text-left px-3 py-2 rounded-lg text-sm font-medium transition ${
                            selectedCategory === "" ? "bg-indigo-50 dark:bg-indigo-950/50 text-indigo-600" : "hover:bg-slate-100"
                        }`}
                    >
                        Tất cả sản phẩm
                    </button>
                    {categories.map((c) => (
                        <button
                            key={c.id}
                            onClick={() => setSelectedCategory(c.id)}
                            className={`w-full text-left px-3 py-2 rounded-lg text-sm font-medium transition ${
                                selectedCategory === c.id ? "bg-indigo-50 dark:bg-indigo-950/50 text-indigo-600" : "hover:bg-slate-100"
                            }`}
                        >
                            {c.name}
                        </button>
                    ))}
                </div>

                {/* Product Grid */}
                <div className="md:col-span-3">
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {products.map((p) => (
                            <div key={p.id} className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm flex flex-col justify-between hover:shadow-md transition">
                                <div className="h-44 bg-slate-100 dark:bg-slate-700/50 flex items-center justify-center relative">
                                    {p.image ? (
                                        <img src={p.image} alt={p.name} className="w-full h-full object-cover" />
                                    ) : (
                                        <ShoppingBag className="w-12 h-12 text-slate-400" />
                                    )}
                                    {p.categoryName && (
                                        <span className="absolute top-2.5 left-2.5 bg-white/90 dark:bg-slate-900/90 text-xs px-2.5 py-1 rounded-full font-medium">
                                            {p.categoryName}
                                        </span>
                                    )}
                                </div>
                                <div className="p-4 flex-1 flex flex-col justify-between">
                                    <div>
                                        <h4 className="font-semibold text-slate-900 dark:text-slate-100">{p.name}</h4>
                                        <p className="text-xs text-slate-400 mt-1 line-clamp-2">{p.description || "Sản phẩm chất lượng cao tại cửa hàng"}</p>
                                    </div>
                                    <div className="mt-4 flex items-center justify-between">
                                        <span className="text-lg font-bold text-indigo-600 dark:text-indigo-400">
                                            {Number(p.sellingPrice).toLocaleString("vi-VN")} đ
                                        </span>
                                        <button
                                            onClick={() => addToCart(p)}
                                            className="px-3 py-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-600 dark:bg-indigo-950/50 dark:text-indigo-300 text-xs font-semibold transition"
                                        >
                                            + Thêm vào giỏ
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </main>

            {/* Cart Drawer */}
            {showCart && (
                <div className="fixed inset-0 z-50 bg-black/40 flex justify-end">
                    <div className="w-full max-w-md bg-white dark:bg-slate-800 h-full p-6 shadow-2xl flex flex-col justify-between overflow-y-auto">
                        <div>
                            <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-700">
                                <h3 className="text-lg font-bold">Giỏ Hàng Của Bạn</h3>
                                <button onClick={() => setShowCart(false)} className="p-1 text-slate-400 hover:text-slate-600">
                                    <X className="w-5 h-5" />
                                </button>
                            </div>

                            <div className="divide-y divide-slate-100 dark:divide-slate-700 mt-4">
                                {cart.map(item => (
                                    <div key={item.id} className="py-3 flex justify-between items-center text-sm">
                                        <div>
                                            <div className="font-medium">{item.name}</div>
                                            <div className="text-xs text-slate-400">{Number(item.sellingPrice).toLocaleString("vi-VN")} đ x {item.quantity}</div>
                                        </div>
                                        <div className="font-semibold">
                                            {Number(item.sellingPrice * item.quantity).toLocaleString("vi-VN")} đ
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {cart.length > 0 && (
                            <form onSubmit={handleCheckout} className="space-y-3 pt-4 border-t border-slate-200 dark:border-slate-700">
                                <div className="flex justify-between font-bold text-base">
                                    <span>Tạm tính:</span>
                                    <span className="text-indigo-600">{cartTotal.toLocaleString("vi-VN")} đ</span>
                                </div>

                                <input
                                    required
                                    type="text"
                                    placeholder="Họ tên người nhận *"
                                    value={recipientName}
                                    onChange={(e) => setRecipientName(e.target.value)}
                                    className="w-full text-sm p-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900"
                                />
                                <input
                                    required
                                    type="tel"
                                    placeholder="Số điện thoại *"
                                    value={recipientPhone}
                                    onChange={(e) => setRecipientPhone(e.target.value)}
                                    className="w-full text-sm p-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900"
                                />
                                <input
                                    required
                                    type="text"
                                    placeholder="Địa chỉ giao hàng *"
                                    value={shippingAddress}
                                    onChange={(e) => setShippingAddress(e.target.value)}
                                    className="w-full text-sm p-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900"
                                />

                                <div className="space-y-1 text-xs">
                                    <label className="font-medium text-slate-500">Phương thức thanh toán:</label>
                                    <select
                                        value={paymentMethod}
                                        onChange={(e) => setPaymentMethod(e.target.value)}
                                        className="w-full p-2 text-sm rounded-lg border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-900"
                                    >
                                        <option value="BANK_TRANSFER">Chuyển khoản ngân hàng VietQR 247</option>
                                        <option value="MOMO">Ví điện tử MoMo</option>
                                        <option value="CASH">Tiền mặt khi nhận hàng (COD)</option>
                                    </select>
                                </div>

                                <button
                                    type="submit"
                                    className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-lg transition"
                                >
                                    Tiến Hành Thanh Toán
                                </button>
                            </form>
                        )}
                    </div>
                </div>
            )}

            {/* QR Payment Modal */}
            {showPaymentModal && paymentQR && (
                <div className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4">
                    <div className="bg-white dark:bg-slate-800 rounded-3xl p-6 max-w-sm w-full text-center space-y-4 shadow-2xl">
                        <div className="flex justify-between items-center">
                            <h3 className="font-bold text-lg flex items-center gap-2">
                                <QrCode className="w-5 h-5 text-indigo-600" />
                                Quét Mã Thanh Toán
                            </h3>
                            <button onClick={() => { setShowPaymentModal(false); setCart([]); setShowCart(false); }} className="text-slate-400">
                                <X className="w-5 h-5" />
                            </button>
                        </div>
                        <p className="text-xs text-slate-500">Mở ứng dụng Ngân hàng hoặc Ví MoMo để quét mã QR và xác nhận chuyển tiền tự động</p>
                        <div className="p-3 bg-white rounded-2xl shadow-inner border border-slate-100 flex items-center justify-center">
                            <img src={paymentQR} alt="VietQR" className="w-64 h-64 object-contain" />
                        </div>
                        <button
                            onClick={() => {
                                alert("Đơn hàng đã được lưu và đang chờ xử lý!");
                                setShowPaymentModal(false);
                                setCart([]);
                                setShowCart(false);
                            }}
                            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl transition"
                        >
                            Tôi Đã Chuyển Khoản Thành Công
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
