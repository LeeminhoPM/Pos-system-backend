import { useState, useEffect, useCallback } from "react";
import { orderApi } from "@/services/api";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";

export function useOrders({ initialStatus = "ALL" } = {}) {
    const { branch } = useAuthStore();
    const addToast = useUIStore((state) => state.addToast);

    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedStatus, setSelectedStatus] = useState(initialStatus);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);

    const fetchOrders = useCallback(
        async (pageIndex = page) => {
            if (!branch?.id) return;
            setLoading(true);
            try {
                const data = await orderApi.getByBranch(branch.id, {
                    page: pageIndex,
                    size: 15,
                });

                if (data && data.content) {
                    setOrders(data.content);
                    setTotalPages(data.totalPages || 1);
                    setTotalElements(data.totalElements || data.content.length);
                } else if (Array.isArray(data)) {
                    setOrders(data);
                    setTotalPages(1);
                    setTotalElements(data.length);
                } else {
                    setOrders([]);
                }
            } catch (err) {
                addToast({
                    type: "error",
                    title: "Lỗi tải đơn hàng",
                    message: err.message || "Không thể tải danh sách đơn hàng",
                });
            } finally {
                setLoading(false);
            }
        },
        [branch?.id, page, addToast]
    );

    useEffect(() => {
        fetchOrders(page);
    }, [fetchOrders, page]);

    const updateOrderStatus = async (orderId, newStatus) => {
        try {
            await orderApi.updateStatus(orderId, newStatus);
            addToast({
                type: "success",
                title: "Thành công",
                message: `Đã cập nhật trạng thái đơn hàng thành ${newStatus}`,
            });
            fetchOrders(page);
            return true;
        } catch (err) {
            addToast({
                type: "error",
                title: "Lỗi",
                message: err.message || "Không thể cập nhật trạng thái đơn hàng",
            });
            return false;
        }
    };

    return {
        orders,
        loading,
        page,
        setPage,
        totalPages,
        totalElements,
        selectedStatus,
        setSelectedStatus,
        refetch: () => fetchOrders(page),
        updateOrderStatus,
    };
}

export default useOrders;
