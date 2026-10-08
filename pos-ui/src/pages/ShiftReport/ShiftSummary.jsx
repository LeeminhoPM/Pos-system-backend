import Header from "./Header";
import PaymentSummary from "./PaymentSummary";
import RecentOrder from "./RecentOrder";
import RefundTable from "./RefundTable";
import SaleSummary from "./SaleSummary";
import ShiftInfo from "./ShiftInfo";
import TopSellingItem from "./TopSellingItem";

const ShiftSummary = () => {
    return (
        <div className="h-full flex flex-col">
            <Header />
            <div className="flex-1 overflow-auto p-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <ShiftInfo />
                    <SaleSummary />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <PaymentSummary />
                    <TopSellingItem />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <RecentOrder />
                    <RefundTable />
                </div>
            </div>
        </div>
    );
};

export default ShiftSummary;
