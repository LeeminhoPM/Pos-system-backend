import { useState, useEffect, useCallback } from "react";
import { productApi, categoryApi } from "@/services/api";
import { useAuthStore } from "@/store/useAuthStore";
import { useUIStore } from "@/store/useUIStore";

export function useProducts({ initialSearch = "", initialCategory = "all" } = {}) {
    const { store } = useAuthStore();
    const addToast = useUIStore((state) => state.addToast);

    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [search, setSearch] = useState(initialSearch);
    const [selectedCategory, setSelectedCategory] = useState(initialCategory);

    // Fetch categories for the store
    const fetchCategories = useCallback(async () => {
        if (!store?.id) return;
        try {
            const data = await categoryApi.getByStore(store.id);
            setCategories(data || []);
        } catch (err) {
            console.error("Lỗi khi tải danh mục:", err);
        }
    }, [store?.id]);

    // Fetch products with filters
    const fetchProducts = useCallback(
        async (pageIndex = page) => {
            if (!store?.id) return;
            setLoading(true);
            try {
                const categoryId = selectedCategory === "all" ? null : selectedCategory;
                const data = await productApi.filter(store.id, {
                    search: search.trim(),
                    categoryId,
                    page: pageIndex,
                    size: 10,
                });

                if (data && data.content) {
                    setProducts(data.content);
                    setTotalPages(data.totalPages || 1);
                    setTotalElements(data.totalElements || data.content.length);
                } else if (Array.isArray(data)) {
                    setProducts(data);
                    setTotalPages(1);
                    setTotalElements(data.length);
                } else {
                    setProducts([]);
                }
            } catch (err) {
                addToast({
                    type: "error",
                    title: "Lỗi",
                    message: err.message || "Không thể tải danh sách sản phẩm",
                });
            } finally {
                setLoading(false);
            }
        },
        [store?.id, search, selectedCategory, page, addToast]
    );

    // Initial load
    useEffect(() => {
        fetchCategories();
    }, [fetchCategories]);

    useEffect(() => {
        fetchProducts(page);
    }, [fetchProducts, page]);

    // Delete product action
    const deleteProduct = async (id) => {
        try {
            await productApi.delete(id);
            addToast({
                type: "success",
                title: "Thành công",
                message: "Đã xóa sản phẩm thành công",
            });
            fetchProducts(page);
            return true;
        } catch (err) {
            addToast({
                type: "error",
                title: "Lỗi xóa sản phẩm",
                message: err.message || "Không thể xóa sản phẩm",
            });
            return false;
        }
    };

    return {
        products,
        categories,
        loading,
        page,
        setPage,
        totalPages,
        totalElements,
        search,
        setSearch,
        selectedCategory,
        setSelectedCategory,
        refetch: () => fetchProducts(page),
        deleteProduct,
    };
}

export default useProducts;
