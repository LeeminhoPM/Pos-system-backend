import React, { useEffect } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "@/store/useAuthStore";

export default function ProtectedRoute({ allowedRoles }) {
    const { isAuthenticated, user, checkAuth, isLoading } = useAuthStore();

    useEffect(() => {
        if (!user && isAuthenticated) {
            checkAuth();
        }
    }, [isAuthenticated, user, checkAuth]);

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && user && !allowedRoles.includes(user.roles)) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
}
