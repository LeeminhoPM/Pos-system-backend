import { CreditCard, Smartphone, Wallet } from "lucide-react";

export const paymentTypeOptions = [
    { key: "CASH", value: "Tiền mặt", icon: <Wallet /> },
    { key: "CARD", value: "Thẻ", icon: <CreditCard /> },
    { key: "BANK_TRANSFER", value: "Chuyển khoản", icon: <Smartphone /> },
];
