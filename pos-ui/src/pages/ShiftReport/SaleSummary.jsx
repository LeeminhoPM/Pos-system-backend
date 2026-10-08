import { Card, CardContent } from "@/components/ui/card";

const saleData = {
    totalInvoices: 120,
    totalRevenue: 15000000,
    totalRefunds: 2000000,
    netRevenue: 13000000,
};
const SaleSummary = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-4">
                    Tổng kết ca làm việc
                </h2>
                <div className="space-y-2">
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Tổng hóa đơn:{" "}
                        </span>
                        <span className="font-medium">
                            {saleData.totalInvoices}
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Tổng doanh thu:{" "}
                        </span>
                        <span className="font-medium">
                            {saleData.totalRevenue.toLocaleString()} đ
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Tổng hoàn tiền:{" "}
                        </span>
                        <span className="font-medium text-destructive">
                            - {saleData.totalRefunds.toLocaleString()} đ
                        </span>
                    </div>
                    <div className="flex justify-between border-t pt-2">
                        <span className="font-bold">Doanh thu thực tế: </span>
                        <span className="font-bold">
                            {saleData.netRevenue.toLocaleString()} đ
                        </span>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};

export default SaleSummary;
