import { create } from "zustand";

export const useUIStore = create((set, get) => ({
    // Sidebar navigation state
    isSidebarCollapsed: false,
    toggleSidebar: () => set((state) => ({ isSidebarCollapsed: !state.isSidebarCollapsed })),
    setSidebarCollapsed: (collapsed) => set({ isSidebarCollapsed: collapsed }),

    // Toast notification system
    toasts: [],
    addToast: ({ type = "info", message, title = "", duration = 4000 }) => {
        const id = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        const newToast = { id, type, message, title };

        set((state) => ({ toasts: [...state.toasts, newToast] }));

        if (duration > 0) {
            setTimeout(() => {
                get().removeToast(id);
            }, duration);
        }
        return id;
    },
    removeToast: (id) => {
        set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }));
    },
    clearToasts: () => set({ toasts: [] }),

    // Global loading overlay for blocking transactions
    isGlobalLoading: false,
    globalLoadingText: "",
    setGlobalLoading: (isLoading, text = "Đang xử lý...") =>
        set({ isGlobalLoading: isLoading, globalLoadingText: text }),
}));

export default useUIStore;
