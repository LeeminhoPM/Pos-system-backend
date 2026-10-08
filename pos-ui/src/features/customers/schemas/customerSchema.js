import { z } from "zod";

export const customerSchema = z.object({
    fullName: z
        .string()
        .min(2, "Tên khách hàng phải có ít nhất 2 ký tự"),
    phone: z
        .string()
        .min(9, "Số điện thoại không hợp lệ")
        .regex(/^[0-9+ ]+$/, "Số điện thoại chỉ được chứa số"),
    email: z
        .string()
        .email("Email không hợp lệ")
        .optional()
        .or(z.literal("")),
    address: z
        .string()
        .optional()
        .or(z.literal("")),
    notes: z
        .string()
        .optional()
        .or(z.literal("")),
});
