# ========================================================
# SkyPOS Frontend Production Dockerfile (Multi-stage)
# Stage 1: Build React Assets with Vite
# ========================================================
FROM node:20-alpine AS builder

WORKDIR /app

# Cache package dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code and build production distribution
COPY . .
RUN npm run build

# ========================================================
# Stage 2: Serve with High-Performance Nginx Alpine
# ========================================================
FROM nginx:alpine AS runner

# Remove default nginx html
RUN rm -rf /usr/share/nginx/html/*

# Copy built distribution from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
