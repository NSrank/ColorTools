package org.plugin.colortools.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.LogLimiter;
import org.plugin.colortools.utils.MessageProcessor;

/**
 * 服务器事件监听器
 * 处理服务器广播消息等，自动转换MineDown格式
 */
public class ServerEventListener implements Listener {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final MessageProcessor messageProcessor;

    public ServerEventListener(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 处理广播消息事件
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        if (!configManager.isEnabled() || !configManager.isServerEventEnabled() || !configManager.isProcessBroadcastMessages()) {
            return;
        }

        try {
            // 获取原始广播消息
            Component originalMessage = event.message();
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);

            // 调试日志 - 显示原始消息
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[调试] 原始广播消息: " + legacyText);
            }

            // 检查是否包含需要处理的格式
            if (!messageProcessor.containsMineDownFormat(legacyText) && !legacyText.contains("&#")) {
                return;
            }

            // 处理消息
            Component processedMessage = messageProcessor.processMessage(legacyText);

            // 设置处理后的消息
            event.message(processedMessage);

            // 调试日志 - 显示处理后的消息
            if (configManager.isDebugMode()) {
                String processedText = LegacyComponentSerializer.legacySection().serialize(processedMessage);
                plugin.getLogger().info("[调试] 处理后广播消息: " + processedText);
            }

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "server.broadcast", "处理广播消息事件时发生错误", e);
        }
    }

    /**
     * 处理服务器命令事件
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onServerCommand(ServerCommandEvent event) {
        if (!configManager.isEnabled() || !configManager.isServerEventEnabled() || !configManager.isProcessBroadcastMessages()) {
            return;
        }

        try {
            String command = event.getCommand().toLowerCase();

            // 处理可能包含格式化文本的服务器命令
            if (shouldProcessServerCommand(command)) {
                String originalCommand = event.getCommand();
                String processedCommand = processServerCommandMessage(originalCommand);

                if (!originalCommand.equals(processedCommand)) {
                    event.setCommand(processedCommand);

                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[调试] 处理服务器命令: " + originalCommand);
                    }
                }
            }

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "server.command", "处理服务器命令事件时发生错误", e);
        }
    }

    /**
     * 检查是否应该处理此服务器命令
     */
    private boolean shouldProcessServerCommand(String command) {
        // 处理可能包含格式化文本的服务器命令
        return command.startsWith("say ") ||
               command.startsWith("broadcast ") ||
               command.startsWith("bc ") ||
               command.startsWith("announce ") ||
               command.startsWith("title ") ||
               command.startsWith("tellraw ");
    }

    /**
     * 处理服务器命令中的消息部分
     */
    private String processServerCommandMessage(String command) {
        try {
            String[] parts = command.split(" ", 2);

            if (parts.length < 2) {
                return command;
            }

            // String commandName = parts[0];  // 暂时不使用
            String message = parts[1];

            // 检查消息是否包含MineDown格式
            if (!messageProcessor.containsMineDownFormat(message)) {
                return command;
            }

            // 对于某些命令，我们不直接修改命令，而是让游戏处理
            // 这里主要是为了记录和调试
            return command;

        } catch (Exception e) {
            plugin.getLogger().warning("处理服务器命令消息时发生错误: " + e.getMessage());
            return command;
        }
    }
}
