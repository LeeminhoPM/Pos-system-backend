import React, { useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import { useAuthStore } from "@/store/useAuthStore";
import AppRoutes from "@/routes/AppRoutes";
import ToastContainer from "@/components/common/ToastContainer";
import ErrorBoundary from "@/components/common/ErrorBoundary";

function App() {
    const { checkAuth } = useAuthStore();

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    return (
        <ErrorBoundary>
            <BrowserRouter>
                <AppRoutes />
                <ToastContainer />
            </BrowserRouter>
        </ErrorBoundary>
    );
}

export default App;
