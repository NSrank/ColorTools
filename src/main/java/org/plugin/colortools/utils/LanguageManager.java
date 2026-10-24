package org.plugin.colortools.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语言管理器
 * 负责多语言支持和消息发送
 */
public class LanguageManager {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private FileConfiguration messages;
    private final Map<String, String> messageCache;

    public LanguageManager(ColorTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageCache = new HashMap<>();
    }

    /**
     * 加载语言文件
     */
    public void loadLanguage() {
        try {
            String language = configManager.getLanguage();
            String fileName = "messages_" + language + ".yml";

            // 确保数据文件夹存在
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File messageFile = new File(plugin.getDataFolder(), fileName);

            // 如果语言文件不存在，从资源中复制
            if (!messageFile.exists()) {
                copyLanguageFile(fileName, messageFile);
            }

            // 加载语言文件
            messages = YamlConfiguration.loadConfiguration(messageFile);

            // 清理缓存
            messageCache.clear();

            plugin.getLogger().info("语言文件已加载: " + fileName);

        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "加载语言文件时发生错误", e);

            // 使用默认语言
            loadDefaultLanguage();
        }
    }

    /**
     * 复制语言文件从资源到数据文件夹
     */
    private void copyLanguageFile(String fileName, File targetFile) {
        try (InputStream inputStream = plugin.getResource(fileName)) {
            if (inputStream != null) {
                Files.copy(inputStream, targetFile.toPath());
                plugin.getLogger().info("已创建语言文件: " + fileName);
            } else {
                plugin.getLogger().warning("找不到语言文件资源: " + fileName + "，使用默认语言");
                loadDefaultLanguage();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("复制语言文件时发生错误: " + e.getMessage());
            loadDefaultLanguage();
        }
    }

    /**
     * 加载默认语言（中文）
     */
    private void loadDefaultLanguage() {
        try {
            String defaultFileName = "messages_zh_CN.yml";
            File defaultFile = new File(plugin.getDataFolder(), defaultFileName);

            if (!defaultFile.exists()) {
                copyLanguageFile(defaultFileName, defaultFile);
            }

            messages = YamlConfiguration.loadConfiguration(defaultFile);
            messageCache.clear();

        } catch (Exception e) {
            plugin.getLogger().severe("加载默认语言失败: " + e.getMessage());
            // 创建最基本的消息配置
            messages = new YamlConfiguration();
            setupBasicMessages();
        }
    }

    /**
     * 设置基本消息（当语言文件完全无法加载时使用）
     */
    private void setupBasicMessages() {
        messages.set("general.prefix", "&8[&6ColorTools&8]&r ");
        messages.set("general.no-permission", "&c你没有权限执行此命令！");
        messages.set("general.config-reloaded", "&a配置文件已重新加载！");
        messages.set("commands.help.header", "&6=== ColorTools 帮助 ===");
        messages.set("commands.reload.success", "&a配置文件重新加载成功！");
    }

    /**
     * 获取消息
     *
     * @param key 消息键
     * @param placeholders 占位符替换
     * @return 格式化后的消息
     */
    public String getMessage(String key, Object... placeholders) {
        // 检查缓存
        String cached = messageCache.get(key);
        if (cached == null) {
            cached = messages.getString(key, "消息未找到: " + key);
            messageCache.put(key, cached);
        }

        String message = cached;

        // 替换占位符
        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i++) {
                message = message.replace("{" + i + "}", String.valueOf(placeholders[i]));
            }

            // 支持命名占位符
            if (placeholders.length % 2 == 0) {
                for (int i = 0; i < placeholders.length; i += 2) {
                    String placeholder = String.valueOf(placeholders[i]);
                    String value = String.valueOf(placeholders[i + 1]);
                    message = message.replace("{" + placeholder + "}", value);
                }
            }
        }

        return message;
    }

    /**
     * 获取消息列表
     */
    public List<String> getMessageList(String key) {
        return messages.getStringList(key);
    }

    /**
     * 发送消息给命令发送者
     */
    public void sendMessage(CommandSender sender, String key, Object... placeholders) {
        String message = getMessage(key, placeholders);
        sendRawMessage(sender, message);
    }

    /**
     * 发送原始消息（支持MineDown格式）
     */
    public void sendRawMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        try {
            // 使用消息处理器处理MineDown格式
            MessageProcessor processor = plugin.getMessageProcessor();
            Component component = processor.processMessage(message);

            // 发送消息
            if (sender instanceof Player) {
                ((Player) sender).sendMessage(component);
            } else {
                // 控制台使用传统格式
                String legacyMessage = LegacyComponentSerializer.legacySection().serialize(component);
                sender.sendMessage(legacyMessage);
            }

        } catch (Exception e) {
            // 如果处理失败，发送原始消息
            sender.sendMessage(message);
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("发送消息时发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 发送带前缀的消息
     */
    public void sendPrefixedMessage(CommandSender sender, String key, Object... placeholders) {
        String prefix = getMessage("general.prefix");
        String message = getMessage(key, placeholders);
        sendRawMessage(sender, prefix + message);
    }

    /**
     * 发送消息列表
     */
    public void sendMessageList(CommandSender sender, String key, Object... placeholders) {
        List<String> messages = getMessageList(key);
        for (String message : messages) {
            // 替换占位符
            String processedMessage = message;
            if (placeholders.length > 0) {
                for (int i = 0; i < placeholders.length; i++) {
                    processedMessage = processedMessage.replace("{" + i + "}", String.valueOf(placeholders[i]));
                }
            }
            sendRawMessage(sender, processedMessage);
        }
    }

    /**
     * 广播消息给所有在线玩家
     */
    public void broadcastMessage(String key, Object... placeholders) {
        String message = getMessage(key, placeholders);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendRawMessage(player, message);
        }

        // 也发送给控制台
        sendRawMessage(plugin.getServer().getConsoleSender(), message);
    }

    /**
     * 发送帮助消息
     */
    public void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "commands.help.header");
        sendMessageList(sender, "commands.help.commands");
        sendMessage(sender, "commands.help.footer");
    }

    /**
     * 发送测试消息
     */
    public void sendTestMessages(CommandSender sender) {
        sendMessage(sender, "commands.test.header");
        sendMessage(sender, "commands.test.description");
        sendMessageList(sender, "commands.test.examples");
        sendMessage(sender, "commands.test.footer");
    }

    /**
     * 发送状态消息
     */
    public void sendStatusMessage(CommandSender sender) {
        sendMessage(sender, "commands.status.header");

        if (configManager.isEnabled()) {
            sendMessage(sender, "commands.status.enabled");
        } else {
            sendMessage(sender, "commands.status.disabled");
        }

        sendMessage(sender, "commands.status.version", "version", plugin.getPluginMeta().getVersion());

        if (configManager.isMineDownEnabled()) {
            sendMessage(sender, "commands.status.minedown-enabled");
        } else {
            sendMessage(sender, "commands.status.minedown-disabled");
        }

        // 获取统计信息
        MessageProcessor.CacheStats stats = plugin.getMessageProcessor().getCacheStats();
        sendMessage(sender, "commands.status.messages-processed", "count", stats.getProcessedMessages());

        sendMessage(sender, "commands.status.footer");
    }

    /**
     * 检查消息键是否存在
     */
    public boolean hasMessage(String key) {
        return messages.contains(key);
    }

    /**
     * 获取当前语言代码
     */
    public String getCurrentLanguage() {
        return configManager.getLanguage();
    }

    /**
     * 清理消息缓存
     */
    public void clearCache() {
        messageCache.clear();
    }
}
