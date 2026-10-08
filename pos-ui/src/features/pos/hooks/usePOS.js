import { useState, useCallback } from "react";
import { useCartStore } from "@/store/useCartStore";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";
import { orderApi, promotionApi, productApi } from "@/services/api";

export function usePOS() {
    const { branch, store } = useAuthStore();
    const cart = useCartStore();
    const addToast = useUIStore((state) => state.addToast);

    const [isCheckingOut, setIsCheckingOut] = useState(false);
    const [lastCompletedOrder, setLastCompletedOrder] = useState(null);

    // Apply promotion voucher code
    const applyVoucher = useCallback(
        async (voucherCode) => {
            if (!voucherCode?.trim()) {
                addToast({ type: "warning", message: "Vui lòng nhập mã khuyến mãi" });
                return false;
            }

            try {
                const subtotal = cart.getSubtotal();
                const result = await promotionApi.apply({
                    code: voucherCode.trim().toUpperCase(),
                    orderAmount: subtotal,
                    storeId: store?.id,
                });

                if (result.valid) {
                    cart.applyPromotion({
                        code: result.code,
                        discountAmount: result.discountAmount,
                        title: result.promotionName || `Mã ${result.code}`,
                    });
                    addToast({
                        type: "success",
                        title: "Mã giảm giá hợp lệ",
                        message: `Đã áp dụng mã "${result.code}" giảm ${result.discountAmount?.toLocaleString()}đ`,
                    });
                    return true;
                } else {
                    addToast({
                        type: "error",
                        title: "Mã không hợp lệ",
                        message: result.message || "Mã khuyến mãi không áp dụng được cho đơn này",
                    });
                    return false;
                }
            } catch (err) {
                addToast({
                    type: "error",
                    title: "Lỗi khuyến mãi",
                    message: err.message || "Không thể kiểm tra mã khuyến mãi",
                });
                return false;
            }
        },
        [cart, store?.id, addToast]
    );

    // Scan Barcode and automatically add to cart
    const scanBarcode = useCallback(
        async (barcode) => {
            if (!barcode || !store?.id) return false;
            try {
                const product = await productApi.getByBarcode(barcode.trim());
                if (product) {
                    cart.addItem(product, 1);
                    addToast({
                        type: "success",
                        message: `Đã thêm: ${product.name}`,
                    });
                    return true;
                } else {
                    addToast({
                        type: "warning",
                        message: `Không tìm thấy sản phẩm có mã vạch ${barcode}`,
                    });
                    return false;
                }
            } catch {
                addToast({
                    type: "warning",
                    message: `Không tìm thấy sản phẩm có mã vạch ${barcode}`,
                });
                return false;
            }
        },
        [cart, store?.id, addToast]
    );

    // Checkout Order
    const checkoutOrder = useCallback(
        async ({ paymentType = "CASH", receivedAmount = 0 } = {}) => {
            if (cart.items.length === 0) {
                addToast({ type: "warning", message: "Giỏ hàng đang trống!" });
                return null;
            }

            if (!branch?.id) {
                addToast({ type: "error", message: "Chưa chọn chi nhánh làm việc để tạo đơn!" });
                return null;
            }

            setIsCheckingOut(true);
            try {
                const payload = {
                    branchId: branch.id,
                    customerId: cart.customer?.id || null,
                    paymentType,
                    discount: cart.getDiscountAmount(),
                    tax: cart.getTaxAmount(),
                    subtotal: cart.getSubtotal(),
                    totalAmount: cart.getTotalAmount(),
                    notes: cart.notes,
                    items: cart.items.map((i) => ({
                        productId: i.product.id,
                        quantity: i.quantity,
                        price: i.unitPrice,
                    })),
                };

                const orderResponse = await orderApi.create(payload);
                setLastCompletedOrder(orderResponse);
                cart.clearCart();

                addToast({
                    type: "success",
                    title: "Thanh toán thành công!",
                    message: `Mã đơn hàng: ${orderResponse.orderNumber || orderResponse.id?.slice(0, 8)}`,
                });

                return orderResponse;
            } catch (err) {
                addToast({
                    type: "error",
                    title: "Lỗi thanh toán",
                    message: err.message || "Không thể hoàn tất đơn hàng",
                });
                return null;
            } finally {
                setIsCheckingOut(false);
            }
        },
        [cart, branch?.id, addToast]
    );

    return {
        cart,
        isCheckingOut,
        lastCompletedOrder,
        applyVoucher,
        scanBarcode,
        checkoutOrder,
        resetLastOrder: () => setLastCompletedOrder(null),
    };
}

export default usePOS;
