import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { authApi, storeApi, branchApi, shiftApi } from "@/services/api";

export const useAuthStore = create(
    persist(
        (set, get) => ({
            user: null,
            token: localStorage.getItem("token") || null,
            store: null,
            branch: null,
            branches: [],
            activeShift: null,
            isAuthenticated: !!localStorage.getItem("token"),
            isLoading: false,
            error: null,

            login: async (email, password) => {
                set({ isLoading: true, error: null });
                try {
                    const data = await authApi.login({ email, password });
                    const token = data.jwt;
                    localStorage.setItem("token", token);
                    set({ token, user: data.user, isAuthenticated: true });

                    await get().loadStoreAndBranches(data.user);
                    await get().checkActiveShift();

                    set({ isLoading: false });
                    return true;
                } catch (err) {
                    set({ error: err.message || "Đăng nhập thất bại", isLoading: false });
                    return false;
                }
            },

            signup: async (userData) => {
                set({ isLoading: true, error: null });
                try {
                    const data = await authApi.signup(userData);
                    const token = data.jwt;
                    localStorage.setItem("token", token);
                    set({ token, user: data.user, isAuthenticated: true });
                    await get().loadStoreAndBranches(data.user);
                    set({ isLoading: false });
                    return true;
                } catch (err) {
                    set({ error: err.message || "Đăng ký thất bại", isLoading: false });
                    return false;
                }
            },

            loadStoreAndBranches: async (user) => {
                try {
                    let store = null;
                    if (user?.roles === "ROLE_STORE_ADMIN" || user?.roles === "ROLE_ADMIN") {
                        store = await storeApi.getAdminStore().catch(() => null);
                    } else {
                        store = await storeApi.getEmployeeStore().catch(() => null);
                    }

                    if (!store && user?.storeId) {
                        store = await storeApi.getById(user.storeId).catch(() => null);
                    }

                    let branches = [];
                    let activeBranch = null;

                    if (store?.id) {
                        branches = await branchApi.getByStore(store.id).catch(() => []);
                        if (branches && branches.length > 0) {
                            if (user?.branchId) {
                                activeBranch = branches.find((b) => b.id === user.branchId) || branches[0];
                            } else {
                                activeBranch = branches[0];
                            }
                        }
                    }

                    set({ store, branches, branch: activeBranch });
                } catch (e) {
                    console.error("Lỗi khi tải thông tin cửa hàng / chi nhánh:", e);
                }
            },

            checkActiveShift: async () => {
                try {
                    const shift = await shiftApi.getCurrent().catch(() => null);
                    set({ activeShift: shift });
                } catch {
                    set({ activeShift: null });
                }
            },

            checkAuth: async () => {
                const token = localStorage.getItem("token");
                if (!token) {
                    set({ isAuthenticated: false, user: null });
                    return;
                }
                set({ isLoading: true });
                try {
                    const user = await authApi.getProfile();
                    set({ user, isAuthenticated: true });
                    await get().loadStoreAndBranches(user);
                    await get().checkActiveShift();
                } catch (err) {
                    console.warn("Phiên làm việc hết hạn:", err.message);
                    get().logout();
                } finally {
                    set({ isLoading: false });
                }
            },

            setActiveBranch: (branch) => {
                set({ branch });
            },

            logout: () => {
                localStorage.removeItem("token");
                set({
                    user: null,
                    token: null,
                    store: null,
                    branch: null,
                    branches: [],
                    activeShift: null,
                    isAuthenticated: false,
                    error: null,
                });
            },
        }),
        {
            name: "pos_auth_storage",
            storage: createJSONStorage(() => localStorage),
            partialize: (state) => ({
                token: state.token,
                user: state.user,
                store: state.store,
                branch: state.branch,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);

// Listen to global 401 unauthorized events to immediately reset auth store
if (typeof window !== "undefined") {
    window.addEventListener("pos:unauthorized", () => {
        useAuthStore.getState().logout();
    });
}

export default useAuthStore;
