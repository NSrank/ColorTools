package org.plugin.colortools.utils;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控器
 * 监控插件性能和资源使用情况
 */
public class PerformanceMonitor {
    
    private final ColorTools plugin;
    private final ConfigManager configManager;
    
    // 性能统计
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong processedMessages = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    // 监控任务
    private BukkitTask monitorTask;
    private boolean isRunning = false;
    
    public PerformanceMonitor(ColorTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }
    
    /**
     * 启动性能监控
     */
    public void start() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        
        // 每5分钟输出一次性能统计
        monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (configManager.isPerformanceMonitoringEnabled()) {
                    logPerformanceStats();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L); // 5分钟 = 6000 ticks
        
        plugin.getLogger().info("性能监控已启动");
    }
    
    /**
     * 停止性能监控
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        
        plugin.getLogger().info("性能监控已停止");
    }
    
    /**
     * 重启性能监控
     */
    public void restart() {
        stop();
        start();
    }
    
    /**
     * 记录消息处理时间
     */
    public void recordProcessingTime(long timeMs) {
        totalProcessingTime.addAndGet(timeMs);
        processedMessages.incrementAndGet();
    }
    
    /**
     * 记录错误
     */
    public void recordError() {
        errorCount.incrementAndGet();
    }
    
    /**
     * 输出性能统计
     */
    private void logPerformanceStats() {
        try {
            long processed = processedMessages.get();
            long totalTime = totalProcessingTime.get();
            long errors = errorCount.get();
            
            if (processed > 0) {
                double avgTime = (double) totalTime / processed;
                
                plugin.getLogger().info("=== 性能统计 ===");
                plugin.getLogger().info("已处理消息: " + processed);
                plugin.getLogger().info("总处理时间: " + totalTime + "ms");
                plugin.getLogger().info("平均处理时间: " + String.format("%.2f", avgTime) + "ms");
                plugin.getLogger().info("错误次数: " + errors);
                
                // 获取内存使用情况
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
                long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
                long usedMemory = totalMemory - freeMemory;
                
                plugin.getLogger().info("内存使用: " + usedMemory + "MB / " + totalMemory + "MB");
                
                // 获取缓存统计
                MessageProcessor.CacheStats cacheStats = plugin.getMessageProcessor().getCacheStats();
                plugin.getLogger().info("缓存命中率: " + String.format("%.2f%%", cacheStats.getHitRatio()));
                
                plugin.getLogger().info("===============");
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("输出性能统计时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取性能统计信息
     */
    public PerformanceStats getStats() {
        return new PerformanceStats(
            processedMessages.get(),
            totalProcessingTime.get(),
            errorCount.get(),
            isRunning
        );
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalProcessingTime.set(0);
        processedMessages.set(0);
        errorCount.set(0);
        plugin.getLogger().info("性能统计已重置");
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 性能统计信息类
     */
    public static class PerformanceStats {
        private final long processedMessages;
        private final long totalProcessingTime;
        private final long errorCount;
        private final boolean isRunning;
        
        public PerformanceStats(long processedMessages, long totalProcessingTime, 
                              long errorCount, boolean isRunning) {
            this.processedMessages = processedMessages;
            this.totalProcessingTime = totalProcessingTime;
            this.errorCount = errorCount;
            this.isRunning = isRunning;
        }
        
        public long getProcessedMessages() {
            return processedMessages;
        }
        
        public long getTotalProcessingTime() {
            return totalProcessingTime;
        }
        
        public long getErrorCount() {
            return errorCount;
        }
        
        public boolean isRunning() {
            return isRunning;
        }
        
        public double getAverageProcessingTime() {
            return processedMessages > 0 ? (double) totalProcessingTime / processedMessages : 0;
        }
        
        public double getErrorRate() {
            return processedMessages > 0 ? (double) errorCount / processedMessages * 100 : 0;
        }
    }
}
