package org.plugin.colortools.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.LogLimiter;
import org.plugin.colortools.utils.MessageProcessor;

/**
 * 玩家踢出事件监听器
 * 处理玩家踢出消息，自动转换MineDown格式
 */
public class PlayerKickListener implements Listener {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final MessageProcessor messageProcessor;

    public PlayerKickListener(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 处理玩家踢出事件
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        if (!configManager.isEnabled() || !configManager.isKickEventEnabled() || !configManager.isProcessKickMessages()) {
            return;
        }

        try {
            // 处理踢出原因
            Component originalReason = event.reason();
            if (originalReason != null) {
                String legacyReason = LegacyComponentSerializer.legacySection().serialize(originalReason);

                // 调试日志 - 显示原始踢出原因
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[调试] 原始踢出原因: " + legacyReason);
                }

                if (messageProcessor.containsMineDownFormat(legacyReason) || legacyReason.contains("&#")) {
                    Component processedReason = messageProcessor.processMessage(legacyReason);
                    event.reason(processedReason);

                    if (configManager.isDebugMode()) {
                        String processedText = LegacyComponentSerializer.legacySection().serialize(processedReason);
                        plugin.getLogger().info("[调试] 处理后踢出原因: " + event.getPlayer().getName() + " -> " + processedText);
                    }
                }
            }

            // 处理踢出消息（显示给其他玩家的消息）
            Component originalLeaveMessage = event.leaveMessage();
            if (originalLeaveMessage != null) {
                String legacyLeaveMessage = LegacyComponentSerializer.legacySection().serialize(originalLeaveMessage);

                // 调试日志 - 显示原始踢出消息
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[调试] 原始踢出消息: " + legacyLeaveMessage);
                }

                if (messageProcessor.containsMineDownFormat(legacyLeaveMessage) || legacyLeaveMessage.contains("&#")) {
                    Component processedLeaveMessage = messageProcessor.processMessage(legacyLeaveMessage);
                    event.leaveMessage(processedLeaveMessage);

                    if (configManager.isDebugMode()) {
                        String processedText = LegacyComponentSerializer.legacySection().serialize(processedLeaveMessage);
                        plugin.getLogger().info("[调试] 处理后踢出消息: " + event.getPlayer().getName() + " -> " + processedText);
                    }
                }
            }

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "kick.event", "处理玩家踢出事件时发生错误", e);
        }
    }
}
