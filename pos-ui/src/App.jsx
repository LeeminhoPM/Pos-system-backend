import React, { useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useAuthStore } from "@/store/useAuthStore";

import MainLayout from "@/components/layout/MainLayout";
import ProtectedRoute from "@/components/layout/ProtectedRoute";

import Login from "@/pages/Auth/Login";
import Register from "@/pages/Auth/Register";
import Dashboard from "@/pages/Dashboard/Dashboard";
import POS from "@/pages/POS/POS";
import Products from "@/pages/Products/Products";
import Inventory from "@/pages/Inventory/Inventory";
import Orders from "@/pages/Orders/Orders";
import Customers from "@/pages/Customers/Customers";
import ShiftReportPage from "@/pages/ShiftReport/ShiftReportPage";
import Employees from "@/pages/Employees/Employees";
import Settings from "@/pages/Settings/Settings";

function App() {
    const { checkAuth } = useAuthStore();

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    return (
        <BrowserRouter>
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

                        {/* Admin Only Routes */}
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

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
