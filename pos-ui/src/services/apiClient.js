import axios from "axios";
import { ENV } from "@/config/env";

export class ApiError extends Error {
    constructor(message, status = 500, errors = null, raw = null) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.errors = errors;
        this.raw = raw;
        this.isNetworkError = !status || status === 0;
    }
}

export const apiClient = axios.create({
    baseURL: ENV.API_URL,
    timeout: ENV.API_TIMEOUT,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

// Request Interceptor: Attach JWT Token & metadata
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = token.startsWith("Bearer ")
                ? token
                : `Bearer ${token}`;
        }

        if (ENV.ENABLE_DEV_LOGS) {
            config.metadata = { startTime: new Date() };
        }

        return config;
    },
    (error) => {
        return Promise.reject(new ApiError(error.message || "Lỗi cấu hình request", 0, null, error));
    }
);

// Response Interceptor: Extract data and normalize errors
apiClient.interceptors.response.use(
    (response) => {
        if (ENV.ENABLE_DEV_LOGS && response.config.metadata) {
            const duration = new Date() - response.config.metadata.startTime;
            console.debug(`[API ${response.config.method?.toUpperCase()} ${response.config.url}] ${response.status} (${duration}ms)`);
        }
        return response.data;
    },
    (error) => {
        // Network or timeout errors
        if (error.code === "ECONNABORTED" || error.message?.includes("timeout")) {
            return Promise.reject(
                new ApiError("Kết nối tới máy chủ quá thời gian (Timeout). Vui lòng kiểm tra lại mạng.", 408, null, error)
            );
        }

        if (!error.response) {
            return Promise.reject(
                new ApiError("Không thể kết nối đến máy chủ backend. Vui lòng kiểm tra server.", 0, null, error)
            );
        }

        const { status, data } = error.response;

        // 401 Unauthorized handling
        if (status === 401) {
            localStorage.removeItem("token");
            window.dispatchEvent(new CustomEvent("pos:unauthorized", { detail: { status } }));
            const message = data?.message || "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
            return Promise.reject(new ApiError(message, 401, null, error));
        }

        // 403 Forbidden handling
        if (status === 403) {
            const message = data?.message || "Bạn không có quyền truy cập chức năng này.";
            return Promise.reject(new ApiError(message, 403, null, error));
        }

        // 400 Bad Request or Validation Error
        const message =
            data?.message ||
            data?.error ||
            (data?.errors && Object.values(data.errors).join(", ")) ||
            "Dữ liệu gửi lên không hợp lệ.";

        return Promise.reject(new ApiError(message, status, data?.errors || null, error));
    }
);

export default apiClient;
