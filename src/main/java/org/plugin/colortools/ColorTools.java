package org.plugin.colortools;

import org.bukkit.plugin.java.JavaPlugin;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.listeners.EventManager;
import org.plugin.colortools.commands.ColorToolsCommand;
import org.plugin.colortools.utils.MessageProcessor;
import org.plugin.colortools.utils.LanguageManager;
import org.plugin.colortools.utils.PerformanceMonitor;
import org.plugin.colortools.utils.CompatibilityManager;
import org.plugin.colortools.packet.PacketInterceptor;

/**
 * ColorTools 主插件类
 * 让游戏内的各种信息自动支持MineDown格式
 *
 * @author NSrank & Augment
 * @version 1.0.0
 */
public final class ColorTools extends JavaPlugin {

    private static ColorTools instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private MessageProcessor messageProcessor;
    private EventManager eventManager;
    private PerformanceMonitor performanceMonitor;
    private CompatibilityManager compatibilityManager;
    private PacketInterceptor packetInterceptor;

    @Override
    public void onEnable() {
        // 设置实例
        instance = this;

        // 初始化组件
        try {
            initializeComponents();
            registerCommands();
            registerPacketInterceptors();
            // registerEvents();  // 暂时禁用事件监听器，使用数据包拦截

            // 启动性能监控
            if (configManager.isPerformanceMonitoringEnabled()) {
                performanceMonitor.start();
            }

            // 检查兼容性
            compatibilityManager.checkCompatibility();

            getLogger().info("===================================");
            getLogger().info("ColorTools v1.0 已加载");
            getLogger().info("作者：NSrank & Augment");
            getLogger().info("MineDown格式处理已激活");
            getLogger().info("===================================");

        } catch (Exception e) {
            getLogger().severe("插件启用失败：" + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // 停止性能监控
            if (performanceMonitor != null) {
                performanceMonitor.stop();
            }

            // 注销数据包拦截器
            if (packetInterceptor != null) {
                packetInterceptor.unregisterPacketListeners();
            }

            // 注销事件监听器
            if (eventManager != null) {
                eventManager.unregisterAll();
            }

            // 清理缓存
            if (messageProcessor != null) {
                messageProcessor.clearCache();
            }

            getLogger().info("ColorTools 插件已安全关闭。");

        } catch (Exception e) {
            getLogger().warning("插件关闭时发生错误：" + e.getMessage());
        } finally {
            instance = null;
        }
    }

    /**
     * 初始化所有组件
     */
    private void initializeComponents() {
        getLogger().info("正在初始化 ColorTools 组件...");

        // 配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 语言管理器
        languageManager = new LanguageManager(this, configManager);
        languageManager.loadLanguage();

        // 消息处理器
        messageProcessor = new MessageProcessor(this, configManager);

        // 事件管理器
        eventManager = new EventManager(this, configManager, messageProcessor);

        // 性能监控器
        performanceMonitor = new PerformanceMonitor(this, configManager);

        // 兼容性管理器
        compatibilityManager = new CompatibilityManager(this, configManager);

        // 数据包拦截器
        packetInterceptor = new PacketInterceptor(this, configManager);

        getLogger().info("所有组件初始化完成。");
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        ColorToolsCommand command = new ColorToolsCommand(this, configManager, languageManager, messageProcessor);
        getCommand("colortools").setExecutor(command);
        getCommand("colortools").setTabCompleter(command);
    }

    /**
     * 注册数据包拦截器
     */
    private void registerPacketInterceptors() {
<<<<<<< HEAD
        try {
            if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
                getLogger().warning("ProtocolLib 未找到！数据包拦截功能将不可用。");
                getLogger().warning("请安装 ProtocolLib 插件以启用高级颜色处理功能。");
                return;
            }

            packetInterceptor.registerPacketListeners();
            getLogger().info("数据包拦截器已注册。");
        } catch (Exception e) {
            getLogger().warning("注册数据包拦截器失败: " + e.getMessage());
            getLogger().warning("数据包拦截功能将不可用，但插件将继续运行。");
        }
=======
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib 未找到！数据包拦截功能需要 ProtocolLib 插件。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        packetInterceptor.registerPacketListeners();
        getLogger().info("数据包拦截器已注册。");
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
    }

    /**
     * 注册事件监听器（已禁用，使用数据包拦截）
     */
    @SuppressWarnings("unused")
    private void registerEvents() {
        eventManager.registerAll();
    }

    /**
     * 重载插件配置
     */
    public void reloadPlugin() {
        try {
            getLogger().info("正在重载 ColorTools 配置...");

            // 重载配置
            configManager.reloadConfig();
            languageManager.loadLanguage();

            // 重新注册事件监听器
            eventManager.unregisterAll();
            eventManager.updateConfig(configManager);
            eventManager.registerAll();

            // 更新消息处理器配置
            messageProcessor.updateConfig(configManager);

            // 重启性能监控
            if (configManager.isPerformanceMonitoringEnabled()) {
                performanceMonitor.restart();
            } else {
                performanceMonitor.stop();
            }

            getLogger().info("配置重载完成。");

        } catch (Exception e) {
            getLogger().severe("重载配置时发生错误：" + e.getMessage());
            throw new RuntimeException("配置重载失败", e);
        }
    }

    // Getter 方法
    public static ColorTools getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public CompatibilityManager getCompatibilityManager() {
        return compatibilityManager;
    }
}
