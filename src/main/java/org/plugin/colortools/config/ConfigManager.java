package org.plugin.colortools.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.plugin.colortools.ColorTools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * 配置管理器
 * 负责加载、保存和管理插件配置
 */
public class ConfigManager {
    
    private final ColorTools plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(ColorTools plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        try {
            // 确保数据文件夹存在
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // 如果配置文件不存在，创建默认配置
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }
            
            // 加载配置
            config = YamlConfiguration.loadConfiguration(configFile);
            
            // 验证配置完整性
            validateConfig();
            
            plugin.getLogger().info("配置文件加载完成。");
            
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "加载配置文件时发生错误", e);
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件时发生错误：" + e.getMessage());
        }
    }
    
    /**
     * 验证配置完整性
     */
    private void validateConfig() {
        boolean needsSave = false;
        
        // 检查必要的配置项
        if (!config.contains("settings.enabled")) {
            config.set("settings.enabled", true);
            needsSave = true;
        }
        
        if (!config.contains("minedown.enabled")) {
            config.set("minedown.enabled", true);
            needsSave = true;
        }
        
        if (needsSave) {
            saveConfig();
            plugin.getLogger().info("配置文件已更新为最新版本。");
        }
    }
    
    // 基本设置
    public boolean isEnabled() {
        return config.getBoolean("settings.enabled", true);
    }
    
    public boolean isDebugMode() {
        return config.getBoolean("settings.debug", false);
    }
    
    public String getLanguage() {
        return config.getString("settings.language", "zh_CN");
    }
    
    public boolean isVerboseLogging() {
        return config.getBoolean("settings.verbose-logging", false);
    }
    
    // MineDown设置
    public boolean isMineDownEnabled() {
        return config.getBoolean("minedown.enabled", true);
    }
    
    public boolean isProcessLegacyColors() {
        return config.getBoolean("minedown.process-legacy-colors", true);
    }
    
    public boolean isProcessHexColors() {
        return config.getBoolean("minedown.process-hex-colors", true);
    }
    
    public boolean isProcessGradients() {
        return config.getBoolean("minedown.process-gradients", true);
    }
    
    public boolean isProcessRainbow() {
        return config.getBoolean("minedown.process-rainbow", true);
    }
    
    public boolean isProcessFormatting() {
        return config.getBoolean("minedown.process-formatting", true);
    }
    
    public boolean isProcessEvents() {
        return config.getBoolean("minedown.process-events", true);
    }
    
    // 事件设置
    public boolean isChatEventEnabled() {
        return config.getBoolean("events.chat.enabled", true);
    }
    
    public boolean isProcessPlayerChat() {
        return config.getBoolean("events.chat.process-player-chat", true);
    }
    
    public boolean isProcessSystemMessages() {
        return config.getBoolean("events.chat.process-system-messages", true);
    }
    
    public EventPriority getChatEventPriority() {
        String priority = config.getString("events.chat.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    public boolean isJoinEventEnabled() {
        return config.getBoolean("events.join.enabled", true);
    }
    
    public boolean isProcessJoinMessages() {
        return config.getBoolean("events.join.process-join-messages", true);
    }
    
    public EventPriority getJoinEventPriority() {
        String priority = config.getString("events.join.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    public boolean isQuitEventEnabled() {
        return config.getBoolean("events.quit.enabled", true);
    }
    
    public boolean isProcessQuitMessages() {
        return config.getBoolean("events.quit.process-quit-messages", true);
    }
    
    public EventPriority getQuitEventPriority() {
        String priority = config.getString("events.quit.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    public boolean isDeathEventEnabled() {
        return config.getBoolean("events.death.enabled", true);
    }
    
    public boolean isProcessDeathMessages() {
        return config.getBoolean("events.death.process-death-messages", true);
    }
    
    public EventPriority getDeathEventPriority() {
        String priority = config.getString("events.death.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    public boolean isKickEventEnabled() {
        return config.getBoolean("events.kick.enabled", true);
    }
    
    public boolean isProcessKickMessages() {
        return config.getBoolean("events.kick.process-kick-messages", true);
    }
    
    public EventPriority getKickEventPriority() {
        String priority = config.getString("events.kick.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    public boolean isServerEventEnabled() {
        return config.getBoolean("events.server.enabled", true);
    }
    
    public boolean isProcessBroadcastMessages() {
        return config.getBoolean("events.server.process-broadcast-messages", true);
    }
    
    public boolean isProcessMotd() {
        return config.getBoolean("events.server.process-motd", true);
    }
    
    public EventPriority getServerEventPriority() {
        String priority = config.getString("events.server.priority", "NORMAL");
        try {
            return EventPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }
    
    // 过滤器设置
    public boolean isFiltersEnabled() {
        return config.getBoolean("filters.enabled", true);
    }
    
    // 性能设置
    public boolean isAsyncProcessing() {
        return config.getBoolean("performance.async-processing", true);
    }
    
    public int getCacheSize() {
        return config.getInt("performance.cache-size", 1000);
    }
    
    public int getCacheExpireTime() {
        return config.getInt("performance.cache-expire-time", 300);
    }
    
    public boolean isPreprocessingEnabled() {
        return config.getBoolean("performance.enable-preprocessing", true);
    }
    
    public boolean isPerformanceMonitoringEnabled() {
        return config.getBoolean("logging.log-performance", false);
    }
    
    // 兼容性设置
    public boolean isAutoDetectChatPlugins() {
        return config.getBoolean("compatibility.auto-detect-chat-plugins", true);
    }
    
    public boolean isProcessAfterOtherPlugins() {
        return config.getBoolean("compatibility.process-after-other-plugins", true);
    }
    
    public boolean isEssentialsXCompatible() {
        return config.getBoolean("compatibility.plugins.essentialsx", true);
    }
    
    public boolean isChatManagerCompatible() {
        return config.getBoolean("compatibility.plugins.chatmanager", true);
    }
    
    public boolean isHuskChatCompatible() {
        return config.getBoolean("compatibility.plugins.huskchat", true);
    }
    
    public boolean isPlaceholderAPICompatible() {
        return config.getBoolean("compatibility.plugins.placeholderapi", true);
    }
    
    // 测试设置
    public boolean isTestingEnabled() {
        return config.getBoolean("testing.enabled", false);
    }
    
    public List<String> getTestMessages() {
        return config.getStringList("testing.test-messages");
    }
    
    // 日志设置
    public boolean isLogProcessedMessages() {
        return config.getBoolean("logging.log-processed-messages", false);
    }
    
    public boolean isLogErrors() {
        return config.getBoolean("logging.log-errors", true);
    }
    
    public String getLogFile() {
        return config.getString("logging.log-file", "colortools.log");
    }
    
    public Level getLogLevel() {
        String level = config.getString("logging.log-level", "INFO");
        try {
            return Level.parse(level);
        } catch (IllegalArgumentException e) {
            return Level.INFO;
        }
    }
    
    // 获取原始配置对象
    public FileConfiguration getConfig() {
        return config;
    }
}
