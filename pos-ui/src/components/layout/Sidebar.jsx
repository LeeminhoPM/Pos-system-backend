import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
    LayoutDashboard,
    ShoppingCart,
    Package,
    Warehouse,
    ReceiptText,
    Users,
    Clock,
    UserCog,
    Settings,
    LogOut,
    Store,
    Sparkles,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { Badge } from "@/components/ui/badge";

const navigationItems = [
    { name: "Tổng quan", path: "/", icon: LayoutDashboard },
    { name: "Bán hàng (POS)", path: "/pos", icon: ShoppingCart, highlight: true },
    { name: "Sản phẩm", path: "/products", icon: Package },
    { name: "Tồn kho", path: "/inventory", icon: Warehouse },
    { name: "Đơn hàng", path: "/orders", icon: ReceiptText },
    { name: "Khách hàng", path: "/customers", icon: Users },
    { name: "Báo cáo ca", path: "/shifts", icon: Clock },
    { name: "Nhân viên", path: "/employees", icon: UserCog, adminOnly: true },
    { name: "Cài đặt", path: "/settings", icon: Settings },
];

export default function Sidebar() {
    const { user, store, branch, branches, setActiveBranch, logout } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const isAdmin = user?.roles === "ROLE_STORE_ADMIN" || user?.roles === "ROLE_ADMIN";

    return (
        <aside className="w-64 border-r border-border/80 bg-card/60 backdrop-blur-md flex flex-col h-screen shrink-0 sticky top-0">
            {/* Brand Header */}
            <div className="p-5 border-b border-border/60 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="size-10 rounded-xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-violet-500 flex items-center justify-center text-white shadow-md shadow-blue-500/20">
                        <Sparkles className="size-5" />
                    </div>
                    <div>
                        <h1 className="font-bold text-base tracking-tight leading-none text-foreground flex items-center gap-1.5">
                            SkyPOS
                            <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-600 dark:text-blue-400">
                                Pro
                            </span>
                        </h1>
                        <p className="text-xs text-muted-foreground truncate max-w-[130px] mt-0.5">
                            {store?.branch || "Hệ thống POS"}
                        </p>
                    </div>
                </div>
            </div>

            {/* Branch Selector */}
            {branches && branches.length > 0 && (
                <div className="px-4 py-3 border-b border-border/40 bg-muted/30">
                    <label className="text-[11px] font-semibold uppercase text-muted-foreground tracking-wider flex items-center gap-1.5 mb-1.5">
                        <Store className="size-3" /> Chi nhánh làm việc
                    </label>
                    <select
                        value={branch?.id || ""}
                        onChange={(e) => {
                            const selected = branches.find((b) => b.id === e.target.value);
                            if (selected) setActiveBranch(selected);
                        }}
                        className="w-full text-xs rounded-lg border border-border bg-background px-2.5 py-1.5 text-foreground shadow-2xs focus:outline-none focus:ring-1 focus:ring-primary"
                    >
                        {branches.map((b) => (
                            <option key={b.id} value={b.id}>
                                {b.name}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            {/* Navigation Menu */}
            <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
                {navigationItems.map((item) => {
                    if (item.adminOnly && !isAdmin) return null;
                    const Icon = item.icon;

                    return (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) =>
                                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                                    item.highlight
                                        ? isActive
                                            ? "bg-blue-600 text-white shadow-md shadow-blue-600/30 font-semibold"
                                            : "bg-blue-500/10 text-blue-600 dark:text-blue-400 hover:bg-blue-500/20 font-semibold"
                                        : isActive
                                        ? "bg-secondary text-foreground font-semibold shadow-2xs"
                                        : "text-muted-foreground hover:bg-secondary/60 hover:text-foreground"
                                }`
                            }
                        >
                            <Icon className="size-4.5 shrink-0" />
                            <span className="flex-1">{item.name}</span>
                            {item.highlight && (
                                <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                            )}
                        </NavLink>
                    );
                })}
            </div>

            {/* User Footer */}
            <div className="p-4 border-t border-border/60 bg-muted/20">
                <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2.5 min-w-0">
                        <div className="size-9 rounded-full bg-gradient-to-br from-neutral-200 to-neutral-400 dark:from-neutral-700 dark:to-neutral-900 flex items-center justify-center font-bold text-xs uppercase text-foreground">
                            {user?.fullName ? user.fullName.charAt(0) : "U"}
                        </div>
                        <div className="min-w-0">
                            <p className="text-xs font-semibold text-foreground truncate">
                                {user?.fullName || "Người dùng"}
                            </p>
                            <Badge variant="outline" className="text-[10px] px-1 py-0 h-4 mt-0.5">
                                {user?.roles === "ROLE_STORE_ADMIN"
                                    ? "Admin"
                                    : user?.roles === "ROLE_BRANCH_MANAGER"
                                    ? "Quản lý"
                                    : "Thu ngân"}
                            </Badge>
                        </div>
                    </div>
                    <button
                        onClick={handleLogout}
                        title="Đăng xuất"
                        className="rounded-lg p-2 text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors cursor-pointer"
                    >
                        <LogOut className="size-4" />
                    </button>
                </div>
            </div>
        </aside>
    );
}
