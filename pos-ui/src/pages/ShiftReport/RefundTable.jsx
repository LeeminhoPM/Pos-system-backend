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
    recentRefunds: [
        {
            id: 1,
            orderId: 1,
            reason: "Khách hàng đổi ý",
            amount: 600000,
        },
    ],
};
const RefundTable = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-4">Đơn hoàn gần đây</h2>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-37.5">Thứ tự</TableHead>
                            <TableHead className="w-37.5">
                                Mã đơn hàng
                            </TableHead>
                            <TableHead className="w-37.5">Lý do hoàn</TableHead>
                            <TableHead className="text-right">
                                Trị giá
                            </TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {shiftData.recentRefunds.map((refund, index) => (
                            <TableRow key={refund.id}>
                                <TableCell>{index + 1}</TableCell>
                                <TableCell>{refund.orderId}</TableCell>
                                <TableCell>{refund.reason}</TableCell>
                                <TableCell className="text-right">
                                    {refund.amount.toLocaleString("vi-VN", {
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

export default RefundTable;
