import { paymentTypeOptions } from "@/assets/assets";
import { Card, CardContent } from "@/components/ui/card";

const shiftData = {
    PaymentSummaries: [
        {
            type: "CASH",
            amount: 1000000,
            transactionCount: 10,
        },
        {
            type: "CARD",
            amount: 1000000,
            transactionCount: 10,
        },
        {
            type: "BANK_TRANSFER",
            amount: 1000000,
            transactionCount: 10,
        },
    ],
    totalSales: 30,
};
const PaymentSummary = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-6">
                    Tổng kết thanh toán
                </h2>
                <div className="space-y-4">
                    {shiftData.PaymentSummaries.map((summary) => (
                        <div key={summary.type} className="flex items-center">
                            <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center mr-4">
                                {
                                    paymentTypeOptions.find(
                                        (option) => option.key === summary.type,
                                    )?.icon
                                }
                            </div>
                            <div className="flex-1">
                                <div className="flex justify-between">
                                    <span className="font-medium">
                                        {
                                            paymentTypeOptions.find(
                                                (option) =>
                                                    option.key === summary.type,
                                            )?.value
                                        }
                                    </span>
                                    <span className="font-bold">
                                        {summary.amount.toLocaleString()} đ
                                    </span>
                                </div>
                                <div className="flex justify-between text-sm text-muted-foreground">
                                    <span>
                                        {summary.transactionCount} giao dịch
                                    </span>
                                    <span>
                                        {(
                                            (summary.transactionCount /
                                                shiftData.totalSales) *
                                            100
                                        ).toFixed(2)}
                                        %
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
};

export default PaymentSummary;
