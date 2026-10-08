import React, { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import MainLayout from "@/components/layout/MainLayout";
import ProtectedRoute from "@/components/layout/ProtectedRoute";
import PageLoader from "@/components/common/PageLoader";
import ErrorBoundary from "@/components/common/ErrorBoundary";

// Lazy-loaded route components for fast initial load
const Login = lazy(() => import("@/pages/Auth/Login"));
const Register = lazy(() => import("@/pages/Auth/Register"));
const Dashboard = lazy(() => import("@/pages/Dashboard/Dashboard"));
const POS = lazy(() => import("@/pages/POS/POS"));
const Products = lazy(() => import("@/pages/Products/Products"));
const Inventory = lazy(() => import("@/pages/Inventory/Inventory"));
const Orders = lazy(() => import("@/pages/Orders/Orders"));
const Customers = lazy(() => import("@/pages/Customers/Customers"));
const ShiftReportPage = lazy(() => import("@/pages/ShiftReport/ShiftReportPage"));
const Employees = lazy(() => import("@/pages/Employees/Employees"));
const Settings = lazy(() => import("@/pages/Settings/Settings"));

export function AppRoutes() {
    return (
        <ErrorBoundary>
            <Suspense fallback={<PageLoader text="Đang khởi tạo ứng dụng POS..." />}>
                <Routes>
                    {/* Public Auth Routes */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Protected POS & Admin Routes */}
                    <Route element={<ProtectedRoute />}>
                        <Route element={<MainLayout />}>
                            <Route path="/" element={<Dashboard />} />
                            <Route path="/pos" element={<POS />} />
                            <Route path="/products" element={<Products />} />
                            <Route path="/inventory" element={<Inventory />} />
                            <Route path="/orders" element={<Orders />} />
                            <Route path="/customers" element={<Customers />} />
                            <Route path="/shifts" element={<ShiftReportPage />} />
                            <Route path="/settings" element={<Settings />} />

                            {/* Store Admin & Admin Restricted Routes */}
                            <Route
                                element={
                                    <ProtectedRoute
                                        allowedRoles={["ROLE_STORE_ADMIN", "ROLE_ADMIN"]}
                                    />
                                }
                            >
                                <Route path="/employees" element={<Employees />} />
                            </Route>
                        </Route>
                    </Route>

                    {/* Catch-all Fallback */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </Suspense>
        </ErrorBoundary>
    );
}

export default AppRoutes;
