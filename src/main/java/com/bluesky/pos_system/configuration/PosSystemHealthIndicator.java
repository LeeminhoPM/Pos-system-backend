package com.bluesky.pos_system.configuration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Component("posSystemHealth")
public class PosSystemHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        double usagePercent = (heapMax > 0) ? ((double) heapUsed / heapMax) * 100 : 0;

        if (usagePercent > 92) {
            return Health.down()
                    .withDetail("memory_status", "CRITICAL_HEAP_EXHAUSTION")
                    .withDetail("heap_used_mb", heapUsed)
                    .withDetail("heap_max_mb", heapMax)
                    .withDetail("heap_usage_pct", String.format("%.1f%%", usagePercent))
                    .build();
        }

        return Health.up()
                .withDetail("status", "OPERATIONAL")
                .withDetail("service", "SkyPOS Enterprise Engine")
                .withDetail("heap_used_mb", heapUsed)
                .withDetail("heap_max_mb", heapMax)
                .withDetail("heap_usage_pct", String.format("%.1f%%", usagePercent))
                .withDetail("threads_active", Thread.activeCount())
                .build();
    }
}
