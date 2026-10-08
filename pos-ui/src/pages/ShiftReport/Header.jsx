import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";

const Header = () => {
    return (
        <div className="p-4 bg-card border-b">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold">Tổng kết ca</h1>
                <div className="flex gap-2">
                    <Button variant="destructive" size="lg">
                        <ArrowRight />
                        Kết thúc ca và đăng xuất
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default Header;
