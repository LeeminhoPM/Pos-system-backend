import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "path";

// https://vite.dev/config/
export default defineConfig({
    base: process.env.VITE_CDN_URL || "/",
    plugins: [react(), tailwindcss()],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    build: {
        target: "es2020",
        cssCodeSplit: true,
        chunkSizeWarningLimit: 600,
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes("node_modules")) {
                        if (id.includes("react") || id.includes("react-dom") || id.includes("react-router-dom")) {
                            return "vendor-react";
                        }
                        if (id.includes("lucide-react")) {
                            return "vendor-icons";
                        }
                        if (id.includes("zod") || id.includes("react-hook-form") || id.includes("@hookform")) {
                            return "vendor-forms";
                        }
                        if (id.includes("axios") || id.includes("zustand")) {
                            return "vendor-state";
                        }
                        return "vendor-misc";
                    }
                },
                entryFileNames: "assets/[name]-[hash].js",
                chunkFileNames: "assets/[name]-[hash].js",
                assetFileNames: "assets/[name]-[hash].[ext]",
            },
        },
    },
});
