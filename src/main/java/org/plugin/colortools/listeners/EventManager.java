package org.plugin.colortools.listeners;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.MessageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件管理器
 * 负责注册和管理所有事件监听器
 */
public class EventManager {
    
    private final ColorTools plugin;
    private ConfigManager configManager;
    private final MessageProcessor messageProcessor;
    private final List<Listener> registeredListeners;
    
    public EventManager(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
        this.registeredListeners = new ArrayList<>();
    }
    
    /**
     * 注册所有事件监听器
     */
    public void registerAll() {
        plugin.getLogger().info("正在注册事件监听器...");
        
        try {
            // 聊天事件监听器
            if (configManager.isChatEventEnabled()) {
                ChatEventListener chatListener = new ChatEventListener(plugin, configManager, messageProcessor);
                registerListener(chatListener);
                plugin.getLogger().info("已注册聊天事件监听器");
            }
            
            // 玩家加入/退出事件监听器
            if (configManager.isJoinEventEnabled() || configManager.isQuitEventEnabled()) {
                PlayerJoinQuitListener joinQuitListener = new PlayerJoinQuitListener(plugin, configManager, messageProcessor);
                registerListener(joinQuitListener);
                plugin.getLogger().info("已注册玩家加入/退出事件监听器");
            }
            
            // 死亡事件监听器
            if (configManager.isDeathEventEnabled()) {
                PlayerDeathListener deathListener = new PlayerDeathListener(plugin, configManager, messageProcessor);
                registerListener(deathListener);
                plugin.getLogger().info("已注册玩家死亡事件监听器");
            }
            
            // 踢出事件监听器
            if (configManager.isKickEventEnabled()) {
                PlayerKickListener kickListener = new PlayerKickListener(plugin, configManager, messageProcessor);
                registerListener(kickListener);
                plugin.getLogger().info("已注册玩家踢出事件监听器");
            }
            
            // 服务器事件监听器
            if (configManager.isServerEventEnabled()) {
                ServerEventListener serverListener = new ServerEventListener(plugin, configManager, messageProcessor);
                registerListener(serverListener);
                plugin.getLogger().info("已注册服务器事件监听器");
            }
            
            plugin.getLogger().info("所有事件监听器注册完成，共注册 " + registeredListeners.size() + " 个监听器");
            
        } catch (Exception e) {
            plugin.getLogger().severe("注册事件监听器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 注册单个监听器
     */
    private void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }
    
    /**
     * 注销所有事件监听器
     */
    public void unregisterAll() {
        try {
            // 注销所有监听器
            for (Listener listener : registeredListeners) {
                HandlerList.unregisterAll(listener);
            }
            
            registeredListeners.clear();
            plugin.getLogger().info("所有事件监听器已注销");
            
        } catch (Exception e) {
            plugin.getLogger().warning("注销事件监听器时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * 获取已注册的监听器数量
     */
    public int getRegisteredListenerCount() {
        return registeredListeners.size();
    }
    
    /**
     * 检查特定类型的监听器是否已注册
     */
    public boolean isListenerRegistered(Class<? extends Listener> listenerClass) {
        return registeredListeners.stream()
                .anyMatch(listener -> listener.getClass().equals(listenerClass));
    }
    
    /**
     * 重新注册所有监听器
     */
    public void reregisterAll() {
        unregisterAll();
        registerAll();
    }
}
