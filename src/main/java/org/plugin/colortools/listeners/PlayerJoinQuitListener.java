package org.plugin.colortools.listeners;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.MessageProcessor;

/**
 * 玩家加入/退出事件监听器
 * 处理玩家加入和退出消息，自动转换MineDown格式
 */
public class PlayerJoinQuitListener implements Listener {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final MessageProcessor messageProcessor;

    public PlayerJoinQuitListener(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 处理玩家加入事件 - 使用后处理策略
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configManager.isEnabled() || !configManager.isJoinEventEnabled() || !configManager.isProcessJoinMessages()) {
            return;
        }

        // 使用延迟处理，让其他插件先处理完
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                processJoinMessageDelayed(event);
            } catch (Exception e) {
                plugin.getLogger().warning("延迟处理加入消息时发生错误: " + e.getMessage());
                if (configManager.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }, 1L); // 延迟1tick
    }

    /**
     * 延迟处理加入消息
     */
    private void processJoinMessageDelayed(PlayerJoinEvent event) {
        Component originalMessage = event.joinMessage();
        if (originalMessage == null) {
            return;
        }

        // 调试日志 - 显示原始消息
        if (configManager.isDebugMode()) {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);
            plugin.getLogger().info("[调试] 延迟处理 - 原始加入消息: " + legacyText);
        }

        // 使用后处理策略
        Component processedMessage = messageProcessor.postProcessComponent(originalMessage);

        // 如果消息被处理了，重新广播
        if (processedMessage != originalMessage) {
            // 取消原始消息
            event.joinMessage(null);

            // 手动广播处理后的消息
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(processedMessage);
            }

            // 发送到控制台
            plugin.getServer().getConsoleSender().sendMessage(processedMessage);

            // 调试日志
            if (configManager.isDebugMode()) {
                String processedText = LegacyComponentSerializer.legacySection().serialize(processedMessage);
                plugin.getLogger().info("[调试] 延迟处理 - 处理后加入消息: " + processedText);
            }
        }
    }

    /**
     * 处理玩家退出事件 - 使用后处理策略
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!configManager.isEnabled() || !configManager.isQuitEventEnabled() || !configManager.isProcessQuitMessages()) {
            return;
        }

        // 使用延迟处理，让其他插件先处理完
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                processQuitMessageDelayed(event);
            } catch (Exception e) {
                plugin.getLogger().warning("延迟处理退出消息时发生错误: " + e.getMessage());
                if (configManager.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }, 1L); // 延迟1tick
    }

    /**
     * 延迟处理退出消息
     */
    private void processQuitMessageDelayed(PlayerQuitEvent event) {
        Component originalMessage = event.quitMessage();
        if (originalMessage == null) {
            return;
        }

        // 调试日志 - 显示原始消息
        if (configManager.isDebugMode()) {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);
            plugin.getLogger().info("[调试] 延迟处理 - 原始退出消息: " + legacyText);
        }

        // 使用后处理策略
        Component processedMessage = messageProcessor.postProcessComponent(originalMessage);

        // 如果消息被处理了，重新广播
        if (processedMessage != originalMessage) {
            // 取消原始消息
            event.quitMessage(null);

            // 手动广播处理后的消息
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                // 注意：退出事件时玩家可能已经离线，所以要排除退出的玩家
                if (!player.equals(event.getPlayer())) {
                    player.sendMessage(processedMessage);
                }
            }

            // 发送到控制台
            plugin.getServer().getConsoleSender().sendMessage(processedMessage);

            // 调试日志
            if (configManager.isDebugMode()) {
                String processedText = LegacyComponentSerializer.legacySection().serialize(processedMessage);
                plugin.getLogger().info("[调试] 延迟处理 - 处理后退出消息: " + processedText);
            }
        }
    }
}
