package org.plugin.colortools.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.MessageProcessor;

/**
 * 聊天事件监听器
 * 处理玩家聊天消息，自动转换MineDown格式
 */
public class ChatEventListener implements Listener {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final MessageProcessor messageProcessor;

    public ChatEventListener(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 处理异步聊天事件 (Paper 1.19+) - 使用后处理策略
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!configManager.isEnabled() || !configManager.isChatEventEnabled() || !configManager.isProcessPlayerChat()) {
            return;
        }

        // 对于聊天消息，我们可以直接处理，因为通常不涉及复杂的翻译
        // 但仍然使用延迟处理以保持一致性
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                processChatMessageDelayed(event);
            } catch (Exception e) {
                plugin.getLogger().warning("延迟处理聊天消息时发生错误: " + e.getMessage());
                if (configManager.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }, 1L); // 延迟1tick
    }

    /**
     * 延迟处理聊天消息
     */
    private void processChatMessageDelayed(AsyncChatEvent event) {
        Component originalMessage = event.message();
        if (originalMessage == null) {
            return;
        }

        // 调试日志 - 显示原始消息
        if (configManager.isDebugMode()) {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);
            plugin.getLogger().info("[调试] 延迟处理 - 原始聊天消息: " + legacyText);
        }

        // 使用后处理策略
        Component processedMessage = messageProcessor.postProcessComponent(originalMessage);

        // 如果消息被处理了，重新广播
        if (processedMessage != originalMessage) {
            // 取消原始消息（通过设置为null）
            event.message(null);

            // 手动广播聊天消息（需要包含玩家名称和格式）
            Component playerDisplayName = event.getPlayer().displayName();
            Component chatFormat = Component.text("<").append(playerDisplayName).append(Component.text("> ")).append(processedMessage);

            // 广播给所有玩家
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(chatFormat);
            }

            // 发送到控制台
            plugin.getServer().getConsoleSender().sendMessage(chatFormat);

            // 调试日志
            if (configManager.isDebugMode()) {
                String processedText = LegacyComponentSerializer.legacySection().serialize(processedMessage);
                plugin.getLogger().info("[调试] 延迟处理 - 处理后聊天消息: " + processedText);
            }
        }
    }

    /**
     * 处理命令预处理事件
     * 某些命令可能包含需要格式化的文本
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!configManager.isEnabled() || !configManager.isChatEventEnabled() || !configManager.isProcessSystemMessages()) {
            return;
        }

        try {
            String command = event.getMessage().toLowerCase();

            // 处理可能包含格式化文本的命令
            if (shouldProcessCommand(command)) {
                String originalCommand = event.getMessage();
                String processedCommand = processCommandMessage(originalCommand);

                if (!originalCommand.equals(processedCommand)) {
                    event.setMessage(processedCommand);

                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[调试] 处理命令消息: " + event.getPlayer().getName() + " -> " + originalCommand);
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("处理命令预处理事件时发生错误: " + e.getMessage());
            if (configManager.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否应该处理此命令
     */
    private boolean shouldProcessCommand(String command) {
        // 处理可能包含格式化文本的命令
        return command.startsWith("/say ") ||
               command.startsWith("/me ") ||
               command.startsWith("/tell ") ||
               command.startsWith("/msg ") ||
               command.startsWith("/w ") ||
               command.startsWith("/whisper ") ||
               command.startsWith("/reply ") ||
               command.startsWith("/r ") ||
               command.startsWith("/broadcast ") ||
               command.startsWith("/bc ") ||
               command.startsWith("/announce ");
    }

    /**
     * 处理命令中的消息部分
     */
    private String processCommandMessage(String command) {
        try {
            String[] parts = command.split(" ", 3);

            if (parts.length < 3) {
                return command;
            }

            // String commandName = parts[0];  // 暂时不使用
            // String target = parts[1];       // 暂时不使用
            String message = parts[2];

            // 检查消息是否包含MineDown格式
            if (!messageProcessor.containsMineDownFormat(message)) {
                return command;
            }

            // 对于某些命令，我们不直接修改命令，而是让游戏处理
            // 这里主要是为了记录和调试
            return command;

        } catch (Exception e) {
            plugin.getLogger().warning("处理命令消息时发生错误: " + e.getMessage());
            return command;
        }
    }
}
