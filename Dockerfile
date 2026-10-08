# ========================================================
# SkyPOS Production Dockerfile (Multi-stage build)
# Stage 1: Build Application Artifact
# ========================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 1. Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 2. Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========================================================
# Stage 2: Lightweight, Secure Production Runtime
# ========================================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Install curl for Docker healthcheck and tzdata for accurate timezone
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone \
    && addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

# Copy built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV PORT=5000 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:5000/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
