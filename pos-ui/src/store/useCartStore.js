import { create } from "zustand";

export const useCartStore = create((set, get) => ({
    items: [],
    customer: null,
    discountType: "FIXED", // "FIXED" | "PERCENT"
    discountValue: 0,
    taxRate: 0.08, // 8% VAT by default
    notes: "",

    addItem: (product, quantity = 1) => {
        const { items } = get();
        const existingIndex = items.findIndex((i) => i.product.id === product.id);

        if (existingIndex > -1) {
            const updatedItems = [...items];
            const currentItem = updatedItems[existingIndex];
            const newQty = currentItem.quantity + quantity;
            updatedItems[existingIndex] = {
                ...currentItem,
                quantity: newQty,
                subtotal: newQty * (product.sellingPrice || 0),
            };
            set({ items: updatedItems });
        } else {
            const newItem = {
                product,
                quantity,
                unitPrice: product.sellingPrice || 0,
                subtotal: quantity * (product.sellingPrice || 0),
            };
            set({ items: [...items, newItem] });
        }
    },

    updateQuantity: (productId, quantity) => {
        if (quantity <= 0) {
            get().removeItem(productId);
            return;
        }
        const { items } = get();
        const updated = items.map((item) => {
            if (item.product.id === productId) {
                return {
                    ...item,
                    quantity,
                    subtotal: quantity * item.unitPrice,
                };
            }
            return item;
        });
        set({ items: updated });
    },

    removeItem: (productId) => {
        set({ items: get().items.filter((i) => i.product.id !== productId) });
    },

    setCustomer: (customer) => set({ customer }),

    setDiscount: (discountType, discountValue) => set({ discountType, discountValue: Math.max(0, Number(discountValue) || 0) }),

    setTaxRate: (taxRate) => set({ taxRate: Math.max(0, Number(taxRate) || 0) }),

    setNotes: (notes) => set({ notes }),

    clearCart: () =>
        set({
            items: [],
            customer: null,
            discountValue: 0,
            notes: "",
        }),

    getSubtotal: () => {
        return get().items.reduce((sum, item) => sum + item.subtotal, 0);
    },

    getDiscountAmount: () => {
        const subtotal = get().getSubtotal();
        const { discountType, discountValue } = get();
        if (discountType === "PERCENT") {
            return Math.min(subtotal, (subtotal * discountValue) / 100);
        }
        return Math.min(subtotal, discountValue);
    },

    getTaxAmount: () => {
        const subtotal = get().getSubtotal();
        const discount = get().getDiscountAmount();
        const taxable = Math.max(0, subtotal - discount);
        return taxable * get().taxRate;
    },

    getTotalAmount: () => {
        const subtotal = get().getSubtotal();
        const discount = get().getDiscountAmount();
        const tax = get().getTaxAmount();
        return Math.max(0, Math.round(subtotal - discount + tax));
    },

    getItemCount: () => {
        return get().items.reduce((sum, item) => sum + item.quantity, 0);
    },
}));
