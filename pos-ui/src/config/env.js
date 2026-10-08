/**
 * Safe environment configuration loader with defaults
 */
export const ENV = {
    API_URL: import.meta.env.VITE_API_URL || "http://localhost:5000",
    APP_TITLE: import.meta.env.VITE_APP_TITLE || "SkyPOS - Retail Point of Sale",
    API_TIMEOUT: Number(import.meta.env.VITE_API_TIMEOUT) || 15000,
    IS_DEV: import.meta.env.DEV,
    ENABLE_DEV_LOGS: import.meta.env.VITE_ENABLE_DEV_LOGS === "true" || import.meta.env.DEV,
};

export default ENV;
