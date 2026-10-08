import { paymentTypeOptions } from "@/assets/assets";
import { Card, CardContent } from "@/components/ui/card";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

const shiftData = {
    recentOrder: [
        {
            id: 1,
            createdAt: "23:00",
            paymentType: "CASH",
            totalAmount: 800000,
        },
        {
            id: 2,
            createdAt: "10:00",
            paymentType: "CARD",
            totalAmount: 1800000,
        },
        {
            id: 3,
            createdAt: "2:00",
            paymentType: "CASH",
            totalAmount: 500000,
        },
    ],
};
const RecentOrder = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-4">Đơn hàng gần đây</h2>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-37.5">Thứ tự</TableHead>
                            <TableHead className="w-37.5">
                                Thời gian tạo
                            </TableHead>
                            <TableHead className="w-37.5">
                                Loại thanh toán
                            </TableHead>
                            <TableHead className="text-right">
                                Trị giá
                            </TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {shiftData.recentOrder.map((order, index) => (
                            <TableRow key={order.id}>
                                <TableCell>{index + 1}</TableCell>
                                <TableCell>{order.createdAt}</TableCell>
                                <TableCell>
                                    {
                                        paymentTypeOptions.find(
                                            (option) =>
                                                option.key ===
                                                order.paymentType,
                                        )?.value
                                    }
                                </TableCell>
                                <TableCell className="text-right">
                                    {order.totalAmount.toLocaleString("vi-VN", {
                                        style: "currency",
                                        currency: "VND",
                                    })}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
};

export default RecentOrder;
