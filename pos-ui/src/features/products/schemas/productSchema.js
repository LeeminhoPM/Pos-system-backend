import { z } from "zod";

export const productSchema = z.object({
    name: z
        .string()
        .min(2, "Tên sản phẩm phải có ít nhất 2 ký tự"),
    sku: z
        .string()
        .optional()
        .or(z.literal("")),
    barcode: z
        .string()
        .optional()
        .or(z.literal("")),
    brand: z
        .string()
        .optional()
        .or(z.literal("")),
    categoryId: z
        .string()
        .min(1, "Vui lòng chọn danh mục sản phẩm"),
    costPrice: z
        .preprocess((val) => Number(val), z.number().min(0, "Giá vốn không được âm")),
    sellingPrice: z
        .preprocess((val) => Number(val), z.number().min(0, "Giá bán không được âm")),
    minStockLevel: z
        .preprocess((val) => (val === "" || val == null ? 0 : Number(val)), z.number().min(0, "Tồn tối thiểu không được âm"))
        .default(5),
    description: z
        .string()
        .optional()
        .or(z.literal("")),
    image: z
        .string()
        .optional()
        .or(z.literal("")),
});
