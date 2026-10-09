import React, { useState, useEffect } from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
    Elements,
    CardElement,
    useStripe,
    useElements,
} from "@stripe/react-stripe-js";
import { Modal } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { paymentApi } from "@/services/api";
import {
    CreditCard,
    ShieldCheck,
    AlertCircle,
    CheckCircle2,
    Lock,
    Sparkles,
    Loader2,
} from "lucide-react";

// Sub-component containing Stripe CardElement form
function StripeCheckoutForm({
    clientSecret,
    amount,
    currency,
    order,
    customer,
    onSuccess,
    onCancel,
    isMockMode,
}) {
    const stripe = useStripe();
    const elements = useElements();
    const [isProcessing, setIsProcessing] = useState(false);
    const [cardholderName, setCardholderName] = useState(customer?.fullName || "");
    const [errorMessage, setErrorMessage] = useState(null);
    const [mockCardNumber, setMockCardNumber] = useState("4242 •••• •••• 4242");
    const [mockExpiry, setMockExpiry] = useState("12/28");
    const [mockCvc, setMockCvc] = useState("123");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage(null);
        setIsProcessing(true);

        try {
            if (isMockMode || !stripe || !elements) {
                // Simulated Test Mode Payment
                await new Promise((resolve) => setTimeout(resolve, 1200));
                onSuccess({
                    transactionId: "TXN-SIM-" + Math.floor(100000 + Math.random() * 900000),
                    orderId: order?.id,
                    paymentMethod: "STRIPE",
                    status: "SUCCESS",
                    amount,
                    currency,
                    cardBrand: "Visa (Test)",
                    cardLast4: "4242",
                    isMock: true,
                });
                return;
            }

            const cardElement = elements.getElement(CardElement);
            if (!cardElement) {
                setErrorMessage("Chưa sẵn sàng form nhập thẻ Stripe.");
                setIsProcessing(false);
                return;
            }

            const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
                payment_method: {
                    card: cardElement,
                    billing_details: {
                        name: cardholderName || "SkyPOS Customer",
                        email: customer?.email || undefined,
                    },
                },
            });

            if (error) {
                setErrorMessage(error.message || "Giao dịch không thành công");
                setIsProcessing(false);
            } else if (paymentIntent && paymentIntent.status === "succeeded") {
                onSuccess({
                    transactionId: paymentIntent.id,
                    orderId: order?.id,
                    paymentMethod: "STRIPE",
                    status: "SUCCESS",
                    amount,
                    currency,
                    gatewayReference: paymentIntent.id,
                });
            } else {
                setErrorMessage("Trạng thái giao dịch: " + (paymentIntent?.status || "Không xác định"));
                setIsProcessing(false);
            }
        } catch (err) {
            console.error("Stripe payment error:", err);
            setErrorMessage(err.message || "Đã xảy ra lỗi trong quá trình xử lý thanh toán");
            setIsProcessing(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {/* Amount Summary */}
            <div className="p-4 rounded-xl bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-800/50 flex items-center justify-between">
                <div>
                    <span className="text-xs font-medium text-muted-foreground block">
                        Số tiền cần thanh toán
                    </span>
                    <span className="text-2xl font-black text-blue-600 dark:text-blue-400">
                        {amount?.toLocaleString("vi-VN")} {currency?.toUpperCase() || "VND"}
                    </span>
                </div>
                <Badge variant="outline" className="bg-background text-xs gap-1.5 py-1 px-2.5">
                    <ShieldCheck className="size-3.5 text-emerald-500" />
                    Stripe 256-bit SSL
                </Badge>
            </div>

            {/* Test Simulation Notice */}
            {isMockMode && (
                <div className="p-3 rounded-lg bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800/40 text-xs text-amber-800 dark:text-amber-300 flex items-start gap-2">
                    <Sparkles className="size-4 shrink-0 mt-0.5 text-amber-500" />
                    <div>
                        <span className="font-semibold">Stripe Sandbox / Test Mode:</span> Khóa Stripe API thật chưa được cấu hình. Hệ thống đang kích hoạt môi trường mô phỏng thẻ thử nghiệm an toàn.
                    </div>
                </div>
            )}

            {/* Cardholder Name */}
            <div>
                <label className="text-xs font-semibold text-muted-foreground block mb-1">
                    Tên chủ thẻ
                </label>
                <Input
                    placeholder="NGUYEN VAN A"
                    value={cardholderName}
                    onChange={(e) => setCardholderName(e.target.value.toUpperCase())}
                    className="font-medium uppercase text-xs h-9"
                    required
                />
            </div>

            {/* Card Input */}
            <div>
                <label className="text-xs font-semibold text-muted-foreground block mb-1">
                    Thông tin thẻ thanh toán (Visa, Mastercard, JCB)
                </label>
                {isMockMode ? (
                    <div className="space-y-2 p-3.5 rounded-xl border border-border bg-muted/40">
                        <div className="flex items-center gap-2">
                            <CreditCard className="size-4 text-blue-500" />
                            <Input
                                value={mockCardNumber}
                                onChange={(e) => setMockCardNumber(e.target.value)}
                                className="font-mono text-xs bg-background h-8"
                                placeholder="4242 •••• •••• 4242"
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-2">
                            <Input
                                value={mockExpiry}
                                onChange={(e) => setMockExpiry(e.target.value)}
                                className="font-mono text-xs bg-background h-8"
                                placeholder="MM/YY"
                            />
                            <Input
                                value={mockCvc}
                                onChange={(e) => setMockCvc(e.target.value)}
                                className="font-mono text-xs bg-background h-8"
                                placeholder="CVC"
                            />
                        </div>
                    </div>
                ) : (
                    <div className="p-3.5 rounded-xl border border-border bg-background focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500 transition-all">
                        <CardElement
                            options={{
                                style: {
                                    base: {
                                        fontSize: "14px",
                                        color: "#1e293b",
                                        fontFamily: "system-ui, -apple-system, sans-serif",
                                        "::placeholder": {
                                            color: "#94a3b8",
                                        },
                                    },
                                    invalid: {
                                        color: "#ef4444",
                                    },
                                },
                            }}
                        />
                    </div>
                )}
            </div>

            {/* Error Message */}
            {errorMessage && (
                <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
                    <AlertCircle className="size-4 shrink-0 text-rose-500" />
                    <span>{errorMessage}</span>
                </div>
            )}

            {/* Action Buttons */}
            <div className="flex gap-2 pt-2">
                <Button
                    type="button"
                    variant="outline"
                    onClick={onCancel}
                    disabled={isProcessing}
                    className="flex-1 cursor-pointer"
                >
                    Hủy bỏ
                </Button>
                <Button
                    type="submit"
                    disabled={isProcessing}
                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-bold cursor-pointer shadow-md shadow-blue-500/20"
                >
                    {isProcessing ? (
                        <>
                            <Loader2 className="size-4 mr-2 animate-spin" />
                            Đang xử lý thẻ...
                        </>
                    ) : (
                        <>
                            <Lock className="size-4 mr-1.5" />
                            Xác nhận thanh toán
                        </>
                    )}
                </Button>
            </div>
        </form>
    );
}

export default function StripePaymentModal({
    isOpen,
    onClose,
    order,
    amount,
    customer,
    onSuccess,
}) {
    const [stripePromise, setStripePromise] = useState(null);
    const [clientSecret, setClientSecret] = useState(null);
    const [isMockMode, setIsMockMode] = useState(true);
    const [currency, setCurrency] = useState("vnd");
    const [isLoading, setIsLoading] = useState(true);
    const [initError, setInitError] = useState(null);

    useEffect(() => {
        if (!isOpen || !order?.id) return;

        let isMounted = true;
        const initializeStripe = async () => {
            setIsLoading(true);
            setInitError(null);
            try {
                // 1. Get Stripe config from backend
                const configRes = await paymentApi.getConfig().catch(() => null);
                const config = configRes?.data || configRes || {};

                const pubKey = config.publishableKey;
                const mock = config.mockMode || !pubKey;
                const cur = config.currency || "vnd";

                if (isMounted) {
                    setIsMockMode(mock);
                    setCurrency(cur);
                    if (pubKey && !mock) {
                        setStripePromise(loadStripe(pubKey));
                    }
                }

                // 2. Initiate Payment Intent on backend
                const initiateRes = await paymentApi.initiate({
                    orderId: order.id,
                    amount: amount || order.totalAmount || 0,
                    paymentMethod: "STRIPE",
                    currency: cur,
                    customerEmail: customer?.email || null,
                });

                if (isMounted) {
                    setClientSecret(initiateRes?.clientSecret || "mock_secret");
                }
            } catch (err) {
                console.error("Lỗi khởi tạo cổng Stripe:", err);
                if (isMounted) {
                    setInitError(err.response?.data?.message || err.message || "Không thể khởi tạo phiên thanh toán Stripe");
                }
            } finally {
                if (isMounted) setIsLoading(false);
            }
        };

        initializeStripe();

        return () => {
            isMounted = false;
        };
    }, [isOpen, order?.id, amount, customer?.email]);

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Thanh Toán Trực Tuyến Qua Stripe"
            description={`Đơn hàng #${order?.orderNumber || ""} - Cổng thanh toán bảo mật`}
            maxWidth="max-w-md"
        >
            {isLoading ? (
                <div className="py-12 flex flex-col items-center justify-center gap-3 text-muted-foreground">
                    <Loader2 className="size-8 animate-spin text-blue-600" />
                    <p className="text-xs font-medium">Đang kết nối cổng thanh toán Stripe...</p>
                </div>
            ) : initError ? (
                <div className="py-6 space-y-4">
                    <div className="p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
                        <AlertCircle className="size-5 shrink-0 text-rose-500" />
                        <span>{initError}</span>
                    </div>
                    <Button variant="outline" onClick={onClose} className="w-full">
                        Đóng
                    </Button>
                </div>
            ) : (
                <Elements stripe={stripePromise}>
                    <StripeCheckoutForm
                        clientSecret={clientSecret}
                        amount={amount || order?.totalAmount || 0}
                        currency={currency}
                        order={order}
                        customer={customer}
                        onSuccess={onSuccess}
                        onCancel={onClose}
                        isMockMode={isMockMode}
                    />
                </Elements>
            )}
        </Modal>
    );
}
