import { Card, CardContent } from "@/components/ui/card";

const shiftData = {
    topSellingItems: [
        {
            id: "1",
            name: "Quần thể thao nam",
            sellingPrice: 500000,
            quantity: 5,
        },
        {
            id: "2",
            name: "Áo thể thao nam",
            sellingPrice: 300000,
            quantity: 4,
        },
    ],
};
const TopSellingItem = () => {
    return (
        <Card>
            <CardContent>
                <h2 className="text-xl font-semibold mb-6">
                    Sản phẩm bán chạy
                </h2>
                <div className="space-y-3">
                    {shiftData.topSellingItems.map((product, index) => (
                        <div key={product.id} className="flex items-center">
                            <div className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center mr-3 text-sm font-medium">
                                {index + 1}
                            </div>
                            <div className="flex-1">
                                <div className="flex justify-between">
                                    <span>{product.name}</span>
                                    <span>
                                        {product.sellingPrice.toLocaleString()}{" "}
                                        ₫
                                    </span>
                                </div>
                                <div className="flex justify-between text-sm text-muted-foreground">
                                    <span>Đã bán {product.quantity}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
};

export default TopSellingItem;
