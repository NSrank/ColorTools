package org.plugin.colortools.listeners;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.MessageProcessor;

/**
 * 玩家死亡事件监听器
 * 处理玩家死亡消息，自动转换MineDown格式
 */
public class PlayerDeathListener implements Listener {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final MessageProcessor messageProcessor;

    public PlayerDeathListener(ColorTools plugin, ConfigManager configManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 处理玩家死亡事件 - 使用后处理策略
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isEnabled() || !configManager.isDeathEventEnabled() || !configManager.isProcessDeathMessages()) {
            return;
        }

        // 使用延迟处理，让其他插件先处理完
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                processDeathMessageDelayed(event);
            } catch (Exception e) {
                plugin.getLogger().warning("延迟处理死亡消息时发生错误: " + e.getMessage());
                if (configManager.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }, 1L); // 延迟1tick
    }

    /**
     * 延迟处理死亡消息
     */
    private void processDeathMessageDelayed(PlayerDeathEvent event) {
        Component originalMessage = event.deathMessage();
        if (originalMessage == null) {
            return;
        }

        // 调试日志 - 显示原始消息
        if (configManager.isDebugMode()) {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);
            plugin.getLogger().info("[调试] 延迟处理 - 原始死亡消息: " + legacyText);
        }

        // 检查是否包含需要处理的颜色代码
        String legacyText = LegacyComponentSerializer.legacySection().serialize(originalMessage);
        if (!legacyText.contains("&#")) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[调试] 无颜色代码，跳过处理");
            }
            return;
        }

        // 使用改进的后处理策略
        Component processedMessage = messageProcessor.postProcessComponent(originalMessage);

        // 强制取消原始消息并重新广播（无论是否有变化）
        if (legacyText.contains("&#")) {
            // 取消原始消息
            event.deathMessage(null);

            // 手动广播处理后的消息
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(processedMessage);
            }

            // 发送到控制台
            plugin.getServer().getConsoleSender().sendMessage(processedMessage);

            // 调试日志
            if (configManager.isDebugMode()) {
                String processedText = LegacyComponentSerializer.legacySection().serialize(processedMessage);
                plugin.getLogger().info("[调试] 延迟处理 - 处理后死亡消息: " + processedText);
                plugin.getLogger().info("[调试] 原始消息已取消，重新广播完成");
            }
        }
    }
}
