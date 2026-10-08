package com.bluesky.pos_system.controllers;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/system/db-performance", "/api/system/db-performance"})
@Tag(name = "System Performance & Monitoring", description = "Giám sát hiệu năng Database, Connection Pool HikariCP và thống kê câu lệnh Hibernate")
public class DatabasePerformanceController {

    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    @Operation(summary = "Xem thống kê hiệu năng Database & HikariCP Pool", description = "Cung cấp số liệu real-time về active connections, slow queries, query execution time")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDatabasePerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 1. HikariCP Pool Metrics
        Map<String, Object> poolMetrics = new HashMap<>();
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
            if (poolBean != null) {
                poolMetrics.put("activeConnections", poolBean.getActiveConnections());
                poolMetrics.put("idleConnections", poolBean.getIdleConnections());
                poolMetrics.put("totalConnections", poolBean.getTotalConnections());
                poolMetrics.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
            }
            poolMetrics.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
            poolMetrics.put("minimumIdle", hikariDataSource.getMinimumIdle());
            poolMetrics.put("poolName", hikariDataSource.getPoolName());
            poolMetrics.put("leakDetectionThresholdMs", hikariDataSource.getLeakDetectionThreshold());
        }
        metrics.put("connectionPool", poolMetrics);

        // 2. Hibernate Query Statistics
        Map<String, Object> jpaMetrics = new HashMap<>();
        try {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            if (sessionFactory != null) {
                Statistics stats = sessionFactory.getStatistics();
                jpaMetrics.put("statisticsEnabled", stats.isStatisticsEnabled());
                jpaMetrics.put("queryExecutionCount", stats.getQueryExecutionCount());
                jpaMetrics.put("queryExecutionMaxTimeMs", stats.getQueryExecutionMaxTime());
                jpaMetrics.put("queryExecutionMaxTimeQuery", stats.getQueryExecutionMaxTimeQueryString());
                jpaMetrics.put("entityLoadCount", stats.getEntityLoadCount());
                jpaMetrics.put("entityInsertCount", stats.getEntityInsertCount());
                jpaMetrics.put("entityUpdateCount", stats.getEntityUpdateCount());
                jpaMetrics.put("entityDeleteCount", stats.getEntityDeleteCount());
                jpaMetrics.put("transactionCount", stats.getTransactionCount());
                jpaMetrics.put("successfulTransactionCount", stats.getSuccessfulTransactionCount());
            }
        } catch (Exception e) {
            jpaMetrics.put("error", e.getMessage());
        }
        metrics.put("queryStatistics", jpaMetrics);

        // 3. JVM Runtime Memory
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryMetrics = new HashMap<>();
        long maxMem = runtime.maxMemory();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long usedMem = totalMem - freeMem;

        memoryMetrics.put("maxMemoryMb", maxMem / (1024 * 1024));
        memoryMetrics.put("usedMemoryMb", usedMem / (1024 * 1024));
        memoryMetrics.put("freeMemoryMb", freeMem / (1024 * 1024));
        memoryMetrics.put("usagePercentage", String.format("%.2f%%", ((double) usedMem / maxMem) * 100));
        metrics.put("jvmMemory", memoryMetrics);

        return ResponseEntity.ok(metrics);
    }
}
