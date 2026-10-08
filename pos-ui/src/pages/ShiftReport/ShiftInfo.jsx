import { Card, CardContent } from "@/components/ui/card";

const shiftData = {
    cashier: {
        fullName: "Nguyễn Văn A",
    },
    shiftStart: "04/05/2026, 8:00",
    shiftEnd: "Đang diễn ra",
    duration: "Đang diễn ra",
};
const ShiftInfo = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-4">
                    Thông tin ca làm việc
                </h2>
                <div className="space-y-2">
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Thu ngân:{" "}
                        </span>
                        <span className="font-medium">
                            {shiftData.cashier.fullName}
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Chấm công đầu ca:{" "}
                        </span>
                        <span className="font-medium">
                            {shiftData.shiftStart}
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Chấm công cuối ca:{" "}
                        </span>
                        <span className="font-medium">
                            {shiftData.shiftEnd}
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">
                            Công thực tế:{" "}
                        </span>
                        <span className="font-medium">
                            {shiftData.duration}
                        </span>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};

export default ShiftInfo;
