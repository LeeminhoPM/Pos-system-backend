import { z } from "zod";

export const loginSchema = z.object({
    email: z
        .string()
        .min(1, "Vui lòng nhập email")
        .email("Email không hợp lệ"),
    password: z
        .string()
        .min(6, "Mật khẩu phải có ít nhất 6 ký tự"),
});

export const registerSchema = z
    .object({
        fullName: z
            .string()
            .min(2, "Họ và tên tối thiểu 2 ký tự"),
        email: z
            .string()
            .min(1, "Vui lòng nhập email")
            .email("Địa chỉ email không hợp lệ"),
        password: z
            .string()
            .min(6, "Mật khẩu phải có ít nhất 6 ký tự"),
        confirmPassword: z
            .string()
            .min(6, "Vui lòng nhập lại mật khẩu"),
        phone: z
            .string()
            .min(10, "Số điện thoại tối thiểu 10 chữ số")
            .regex(/^[0-9+ ]+$/, "Số điện thoại chỉ được chứa chữ số"),
        storeName: z
            .string()
            .min(3, "Tên cửa hàng phải có ít nhất 3 ký tự"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "Mật khẩu xác nhận không khớp",
        path: ["confirmPassword"],
    });
