import apiClient from "@/services/apiClient";

/**
 * SkyPOS Modern RESTful API Client SDK
 * Production-ready typed client supporting /api/v1 endpoints,
 * fluent query builder, and automatic interceptors.
 */
export class PosClient {
    constructor(client = apiClient) {
        this.http = client;
        this.version = "v1";

        // Resource Namespaces
        this.auth = {
            login: (credentials) => this.http.post("/auth/login", credentials),
            signup: (data) => this.http.post("/auth/signup", data),
            me: () => this.http.get("/api/v1/user/me"),
            getProfile: () => this.http.get("/api/v1/user/profile"),
            changePassword: (data) => this.http.post("/api/v1/user/change-password", data),
        };

        this.products = {
            list: (params = {}) => {
                const query = this._buildQuery(params);
                const storeId = params.storeId;
                if (storeId) {
                    return this.http.get(`/api/v1/products/store/${storeId}/paged${query}`);
                }
                return this.http.get(`/api/v1/products${query}`);
            },
            getById: (id) => this.http.get(`/api/v1/products/${id}`),
            getByStore: (storeId) => this.http.get(`/api/v1/products/store/${storeId}`),
            search: (storeId, keyword) =>
                this.http.get(`/api/v1/products/store/${storeId}/search?keyword=${encodeURIComponent(keyword || "")}`),
            create: (data) => this.http.post("/api/v1/products", data),
            update: (id, data) => this.http.put(`/api/v1/products/${id}`, data),
            delete: (id) => this.http.delete(`/api/v1/products/${id}`),
        };

        this.orders = {
            create: (data) => this.http.post("/api/v1/orders", data),
            getById: (id) => this.http.get(`/api/v1/orders/${id}`),
            updateStatus: (id, status) => this.http.put(`/api/v1/orders/${id}/status?status=${status}`),
            listByBranch: (branchId, params = {}) => {
                const query = this._buildQuery(params);
                return this.http.get(`/api/v1/orders/branch/${branchId}${query}`);
            },
        };

        this.payments = {
            initiate: (request) => this.http.post("/api/v1/payments/initiate", request),
            getStatus: (transactionId) => this.http.get(`/api/v1/payments/${transactionId}`),
        };

        this.customers = {
            list: () => this.http.get("/api/v1/customers"),
            search: (query) => this.http.get(`/api/v1/customers/search?query=${encodeURIComponent(query || "")}`),
            getById: (id) => this.http.get(`/api/v1/customers/${id}`),
            create: (data) => this.http.post("/api/v1/customers", data),
            update: (id, data) => this.http.put(`/api/v1/customers/${id}`, data),
            delete: (id) => this.http.delete(`/api/v1/customers/${id}`),
        };

        this.promotions = {
            list: (storeId) => this.http.get(`/api/v1/promotions/store/${storeId}`),
            getById: (id) => this.http.get(`/api/v1/promotions/${id}`),
            apply: (code, orderAmount, storeId) =>
                this.http.post("/api/v1/promotions/apply", { code, orderAmount, storeId }),
            create: (data) => this.http.post("/api/v1/promotions", data),
            update: (id, data) => this.http.put(`/api/v1/promotions/${id}`, data),
            delete: (id) => this.http.delete(`/api/v1/promotions/${id}`),
        };

        this.categories = {
            list: (storeId) => this.http.get(`/api/v1/categories/store/${storeId}`),
            tree: (storeId) => this.http.get(`/api/v1/categories/store/${storeId}/tree`),
            create: (data) => this.http.post("/api/v1/categories", data),
            update: (id, data) => this.http.put(`/api/v1/categories/${id}`, data),
            delete: (id) => this.http.delete(`/api/v1/categories/${id}`),
        };

        this.inventory = {
            getByBranch: (branchId) => this.http.get(`/api/v1/inventories/branch/${branchId}`),
            getByProduct: (productId) => this.http.get(`/api/v1/inventories/product/${productId}`),
        };

        this.shifts = {
            getCurrent: () => this.http.get("/api/v1/shift-reports/current"),
            start: () => this.http.post("/api/v1/shift-reports/start"),
            close: (id, data) => this.http.put(`/api/v1/shift-reports/${id}/close`, data),
        };

        this.analytics = {
            getDashboard: (params = {}) => {
                const query = this._buildQuery(params);
                return this.http.get(`/api/v1/analytics/dashboard${query}`);
            },
        };
    }

    _buildQuery(params) {
        if (!params || Object.keys(params).length === 0) return "";
        const clean = Object.entries(params)
            .filter(([_, v]) => v !== undefined && v !== null && v !== "")
            .reduce((acc, [k, v]) => ({ ...acc, [k]: v }), {});
        const qs = new URLSearchParams(clean).toString();
        return qs ? `?${qs}` : "";
    }
}

export const pos = new PosClient();
export default pos;
