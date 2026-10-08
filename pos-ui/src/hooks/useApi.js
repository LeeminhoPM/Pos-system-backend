import { useState, useCallback } from "react";
import { useUIStore } from "@/store/useUIStore";

/**
 * Reusable hook for managing async API calls with loading, error, and data states.
 * @param {Function} apiFunc - The async API function to call
 * @param {Object} options - Configuration options (showToastOnError, onSuccess, onError, initialData)
 */
export function useApi(apiFunc, options = {}) {
    const { showToastOnError = true, onSuccess, onError, initialData = null } = options;
    const [data, setData] = useState(initialData);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const addToast = useUIStore((state) => state.addToast);

    const execute = useCallback(
        async (...args) => {
            setLoading(true);
            setError(null);
            try {
                const result = await apiFunc(...args);
                setData(result);
                if (onSuccess) {
                    onSuccess(result);
                }
                return { success: true, data: result };
            } catch (err) {
                const message = err.message || "Đã xảy ra lỗi khi gọi API";
                setError(err);
                if (showToastOnError) {
                    addToast({ type: "error", message, title: "Lỗi kết nối" });
                }
                if (onError) {
                    onError(err);
                }
                return { success: false, error: err };
            } finally {
                setLoading(false);
            }
        },
        [apiFunc, showToastOnError, onSuccess, onError, addToast]
    );

    const reset = useCallback(() => {
        setData(initialData);
        setLoading(false);
        setError(null);
    }, [initialData]);

    return {
        data,
        loading,
        error,
        execute,
        reset,
        setData,
    };
}

export default useApi;
