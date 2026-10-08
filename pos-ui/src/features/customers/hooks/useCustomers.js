import { useState, useEffect, useCallback } from "react";
import { customerApi } from "@/services/api";
import { useUIStore } from "@/store/useUIStore";

export function useCustomers({ initialSearch = "" } = {}) {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(initialSearch);
    const addToast = useUIStore((state) => state.addToast);

    const fetchCustomers = useCallback(async () => {
        setLoading(true);
        try {
            let data;
            if (search.trim()) {
                data = await customerApi.search(search.trim());
            } else {
                data = await customerApi.getAll();
            }
            setCustomers(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Lỗi khi tải khách hàng:", err);
        } finally {
            setLoading(false);
        }
    }, [search]);

    useEffect(() => {
        fetchCustomers();
    }, [fetchCustomers]);

    const createCustomer = async (customerData) => {
        try {
            const created = await customerApi.create(customerData);
            addToast({
                type: "success",
                title: "Thành công",
                message: "Đã thêm khách hàng mới thành công!",
            });
            fetchCustomers();
            return { success: true, data: created };
        } catch (err) {
            addToast({
                type: "error",
                title: "Lỗi",
                message: err.message || "Không thể tạo khách hàng",
            });
            return { success: false, error: err };
        }
    };

    return {
        customers,
        loading,
        search,
        setSearch,
        refetch: fetchCustomers,
        createCustomer,
    };
}

export default useCustomers;
