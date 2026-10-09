import apiClient, { ApiError } from "./apiClient";

export const api = apiClient;
export { ApiError };

export const authApi = {
    login: (credentials) => api.post("/auth/login", credentials),
    signup: (data) => api.post("/auth/signup", data),
    getProfile: () => api.get("/api/user/profile"),
    getMe: () => api.get("/api/user/me"),
    getUserById: (id) => api.get(`/api/user/${id}`),
    changePassword: (data) => api.post("/api/user/change-password", data),
};

export const storeApi = {
    getAll: () => api.get("/api/stores"),
    getAdminStore: () => api.get("/api/stores/admin"),
    getEmployeeStore: () => api.get("/api/stores/employee"),
    getById: (id) => api.get(`/api/stores/${id}`),
    create: (data) => api.post("/api/stores", data),
    update: (id, data) => api.put(`/api/stores/${id}`, data),
};

export const branchApi = {
    getByStore: (storeId) => api.get(`/api/branches/store/${storeId}`),
    getById: (id) => api.get(`/api/branches/${id}`),
    create: (data) => api.post("/api/branches", data),
    update: (id, data) => api.put(`/api/branches/${id}`, data),
    delete: (id) => api.delete(`/api/branches/${id}`),
};

export const categoryApi = {
    getByStore: (storeId) => api.get(`/api/categories/store/${storeId}`),
    getTree: (storeId) => api.get(`/api/categories/store/${storeId}/tree`),
    create: (data) => api.post("/api/categories", data),
    update: (id, data) => api.put(`/api/categories/${id}`, data),
    delete: (id) => api.delete(`/api/categories/${id}`),
};

export const productApi = {
    getByStore: (storeId) => api.get(`/api/products/store/${storeId}`),
    getPaged: (storeId, params = {}) => {
        const query = new URLSearchParams(params).toString();
        return api.get(`/api/products/store/${storeId}/paged${query ? `?${query}` : ""}`);
    },
    getById: (id) => api.get(`/api/products/${id}`),
    search: (storeId, keyword) => api.get(`/api/products/store/${storeId}/search?keyword=${encodeURIComponent(keyword)}`),
    create: (data) => api.post("/api/products", data),
    update: (id, data) => api.put(`/api/products/${id}`, data),
    delete: (id) => api.delete(`/api/products/${id}`),
};

export const inventoryApi = {
    getByBranch: (branchId) => api.get(`/api/inventories/branch/${branchId}`),
    getLowStock: (branchId) => api.get(`/api/inventories/branch/${branchId}/low-stock`),
    getLowStockSummary: (branchId) => api.get(`/api/inventories/branch/${branchId}/low-stock/summary`),
    adjustStock: (branchId, productId, deltaQuantity) =>
        api.post(`/api/inventories/adjust-quick?branchId=${branchId}&productId=${productId}&deltaQuantity=${deltaQuantity}`),
    adjustWithAudit: (data) => api.post("/api/inventories/adjust", data),
    getTransactions: (branchId, page = 0, size = 20) =>
        api.get(`/api/inventories/transactions/branch/${branchId}?page=${page}&size=${size}`),
    getProductTransactions: (productId) => api.get(`/api/inventories/transactions/product/${productId}`),
    create: (data) => api.post("/api/inventories", data),
    update: (id, data) => api.put(`/api/inventories/${id}`, data),
    delete: (id) => api.delete(`/api/inventories/${id}`),
};

export const orderApi = {
    create: (data) => api.post("/api/orders", data),
    getById: (id) => api.get(`/api/orders/${id}`),
    getByBranch: (branchId, params = {}) => {
        const query = new URLSearchParams(params).toString();
        return api.get(`/api/orders/branch/${branchId}${query ? `?${query}` : ""}`);
    },
    getToday: (branchId) => api.get(`/api/orders/today/branch/${branchId}`),
    getRecent: (branchId) => api.get(`/api/orders/recent/branch/${branchId}`),
    updateStatus: (id, status) => api.put(`/api/orders/${id}/status?status=${status}`),
};

export const customerApi = {
    getAll: () => api.get("/api/customers"),
    search: (keyword) => api.get(`/api/customers/search?keyword=${encodeURIComponent(keyword)}`),
    create: (data) => api.post("/api/customers", data),
    update: (id, data) => api.put(`/api/customers/${id}`, data),
    delete: (id) => api.delete(`/api/customers/${id}`),
};

export const shiftApi = {
    start: () => api.post("/api/shift-reports/start"),
    end: (endDate) => api.patch(`/api/shift-reports/end?endDate=${encodeURIComponent(endDate || new Date().toISOString())}`),
    getCurrent: () => api.get("/api/shift-reports/current"),
    getByCashier: (cashierId) => api.get(`/api/shift-reports/cashier/${cashierId}`),
    getByBranch: (branchId) => api.get(`/api/shift-reports/branch/${branchId}`),
    getById: (id) => api.get(`/api/shift-reports/${id}`),
};

export const refundApi = {
    create: (data) => api.post("/api/refunds", data),
    getAll: () => api.get("/api/refunds"),
    getByBranch: (branchId) => api.get(`/api/refunds/branch/${branchId}`),
    getById: (id) => api.get(`/api/refunds/${id}`),
};

export const employeeApi = {
    getStoreEmployees: (storeId, role) => api.get(`/api/employees/store/${storeId}${role ? `?userRole=${role}` : ""}`),
    getBranchEmployees: (branchId, role) => api.get(`/api/employees/branch/${branchId}${role ? `?userRole=${role}` : ""}`),
    createStoreEmployee: (storeId, data) => api.post(`/api/employees/store/${storeId}`, data),
    createBranchEmployee: (branchId, data) => api.post(`/api/employees/branch/${branchId}`, data),
    updateEmployee: (id, data) => api.put(`/api/employees/${id}`, data),
    deleteEmployee: (id) => api.delete(`/api/employees/${id}`),
};

export const supplierApi = {
    getByStore: (storeId) => api.get(`/api/suppliers/store/${storeId}`),
    search: (storeId, query) => api.get(`/api/suppliers/store/${storeId}/search?query=${encodeURIComponent(query || "")}`),
    getById: (id) => api.get(`/api/suppliers/${id}`),
    create: (data) => api.post("/api/suppliers", data),
    update: (id, data) => api.put(`/api/suppliers/${id}`, data),
    delete: (id) => api.delete(`/api/suppliers/${id}`),
};

export const stockReceiptApi = {
    create: (data) => api.post("/api/stock-receipts", data),
    getById: (id) => api.get(`/api/stock-receipts/${id}`),
    getByBranch: (branchId) => api.get(`/api/stock-receipts/branch/${branchId}`),
    getBranchTransactions: (branchId) => api.get(`/api/stock-receipts/transactions/branch/${branchId}`),
    getProductTransactions: (productId) => api.get(`/api/stock-receipts/transactions/product/${productId}`),
};

export const promotionApi = {
    getByStore: (storeId) => api.get(`/api/promotions/store/${storeId}`),
    getById: (id) => api.get(`/api/promotions/${id}`),
    create: (data) => api.post("/api/promotions", data),
    update: (id, data) => api.put(`/api/promotions/${id}`, data),
    delete: (id) => api.delete(`/api/promotions/${id}`),
    apply: (data) => api.post("/api/promotions/apply", data),
};

export const analyticsApi = {
    getDashboard: (params = {}) => {
        const query = new URLSearchParams(params).toString();
        return api.get(`/api/analytics/dashboard${query ? `?${query}` : ""}`);
    },
};

export const auditLogApi = {
    getLogs: (page = 0, size = 20) => api.get(`/api/audit-logs?page=${page}&size=${size}`),
};

export const paymentApi = {
    getConfig: () => api.get("/api/v1/payments/config"),
    initiate: (data) => api.post("/api/v1/payments/initiate", data),
    getStatus: (id) => api.get(`/api/v1/payments/${id}`),
    getByOrder: (orderId) => api.get(`/api/v1/payments/order/${orderId}`),
    getHistory: (params = {}) => {
        const query = new URLSearchParams(params).toString();
        return api.get(`/api/v1/payments/history${query ? `?${query}` : ""}`);
    },
    refund: (data) => api.post("/api/v1/payments/refund", data),
};

export default api;
