package org.plugin.colortools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;
import org.plugin.colortools.utils.LanguageManager;
import org.plugin.colortools.utils.MessageProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ColorTools主命令处理器
 * 处理 /colortools 命令及其子命令
 */
public class ColorToolsCommand implements CommandExecutor, TabCompleter {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final MessageProcessor messageProcessor;

    public ColorToolsCommand(ColorTools plugin, ConfigManager configManager,
                           LanguageManager languageManager, MessageProcessor messageProcessor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.languageManager = languageManager;
        this.messageProcessor = messageProcessor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {

        // 检查基本权限
        if (!sender.hasPermission("colortools.admin")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return true;
        }

        // 如果没有参数，显示帮助
        if (args.length == 0) {
            languageManager.sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
            case "?":
                handleHelpCommand(sender);
                break;

            case "reload":
                handleReloadCommand(sender);
                break;

            case "test":
                handleTestCommand(sender, args);
                break;

            case "status":
                handleStatusCommand(sender);
                break;

            case "toggle":
                handleToggleCommand(sender);
                break;

            case "cache":
                handleCacheCommand(sender, args);
                break;

            case "debug":
                handleDebugCommand(sender, args);
                break;

            case "testcolor":
                handleTestColorCommand(sender, args);
                break;

            default:
                languageManager.sendPrefixedMessage(sender, "general.unknown-command");
                break;
        }

        return true;
    }

    /**
     * 处理帮助命令
     */
    private void handleHelpCommand(CommandSender sender) {
        languageManager.sendHelpMessage(sender);
    }

    /**
     * 处理重载命令
     */
    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("colortools.reload")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        try {
            plugin.reloadPlugin();
            languageManager.sendPrefixedMessage(sender, "commands.reload.success");

        } catch (Exception e) {
            languageManager.sendPrefixedMessage(sender, "commands.reload.error", "error", e.getMessage());
            plugin.getLogger().severe("重载配置时发生错误: " + e.getMessage());
        }
    }

    /**
     * 处理测试命令
     */
    private void handleTestCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("colortools.test")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        if (args.length == 1) {
            // 显示预设的测试消息
            languageManager.sendTestMessages(sender);
        } else {
            // 测试自定义消息
            String testMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            languageManager.sendPrefixedMessage(sender, "commands.test.header");
            languageManager.sendRawMessage(sender, "&7原始消息: &f" + testMessage);
            languageManager.sendRawMessage(sender, "&7处理后: ");
            languageManager.sendRawMessage(sender, testMessage);
        }
    }

    /**
     * 处理状态命令
     */
    private void handleStatusCommand(CommandSender sender) {
        languageManager.sendStatusMessage(sender);

        // 显示缓存统计
        MessageProcessor.CacheStats stats = messageProcessor.getCacheStats();
        languageManager.sendRawMessage(sender, "&7缓存大小: &e" + stats.getCacheSize());
        languageManager.sendRawMessage(sender, "&7缓存命中: &a" + stats.getHits());
        languageManager.sendRawMessage(sender, "&7缓存未命中: &c" + stats.getMisses());
        languageManager.sendRawMessage(sender, "&7命中率: &e" + String.format("%.2f%%", stats.getHitRatio()));
    }

    /**
     * 处理切换命令
     */
    private void handleToggleCommand(CommandSender sender) {
        if (!sender.hasPermission("colortools.admin")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        // 切换插件启用状态
        boolean currentState = configManager.isEnabled();
        configManager.getConfig().set("settings.enabled", !currentState);
        configManager.saveConfig();

        if (!currentState) {
            languageManager.sendPrefixedMessage(sender, "commands.toggle.enabled");
        } else {
            languageManager.sendPrefixedMessage(sender, "commands.toggle.disabled");
        }
    }

    /**
     * 处理缓存命令
     */
    private void handleCacheCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("colortools.admin")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        if (args.length < 2) {
            languageManager.sendRawMessage(sender, "&c用法: /colortools cache <clear|stats>");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "clear":
                messageProcessor.clearCache();
                languageManager.sendRawMessage(sender, "&a缓存已清理！");
                break;

            case "stats":
                MessageProcessor.CacheStats stats = messageProcessor.getCacheStats();
                languageManager.sendRawMessage(sender, "&6=== 缓存统计 ===");
                languageManager.sendRawMessage(sender, "&7缓存大小: &e" + stats.getCacheSize());
                languageManager.sendRawMessage(sender, "&7已处理消息: &e" + stats.getProcessedMessages());
                languageManager.sendRawMessage(sender, "&7缓存命中: &a" + stats.getHits());
                languageManager.sendRawMessage(sender, "&7缓存未命中: &c" + stats.getMisses());
                languageManager.sendRawMessage(sender, "&7命中率: &e" + String.format("%.2f%%", stats.getHitRatio()));
                break;

            default:
                languageManager.sendRawMessage(sender, "&c未知的缓存操作: " + action);
                break;
        }
    }

    /**
     * 处理颜色测试命令
     */
    private void handleTestColorCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("colortools.test")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        if (args.length == 1) {
            // 显示预设的颜色测试消息
            languageManager.sendRawMessage(sender, "&6=== 颜色测试 ===");

            // 测试十六进制颜色
            String[] testColors = {
                "&#ff0000&红色测试",
                "&#00ff00&绿色测试",
                "&#0000ff&蓝色测试",
                "&#55B1C2&青蓝色测试",
                "&#8B0000&深红色测试",
                "&#ff0000-#0000ff&红蓝渐变测试",
                "&rainbow&彩虹测试"
            };

            for (String testColor : testColors) {
                languageManager.sendRawMessage(sender, "&7原始: &f" + testColor);
                languageManager.sendRawMessage(sender, "&7效果: " + testColor);
                languageManager.sendRawMessage(sender, "");
            }
        } else {
            // 测试自定义颜色消息
            String testMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            languageManager.sendRawMessage(sender, "&6=== 自定义颜色测试 ===");
            languageManager.sendRawMessage(sender, "&7原始消息: &f" + testMessage);
            languageManager.sendRawMessage(sender, "&7处理后效果: " + testMessage);
        }
    }

    /**
     * 处理调试命令
     */
    private void handleDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("colortools.admin")) {
            languageManager.sendPrefixedMessage(sender, "general.no-permission");
            return;
        }

        if (args.length < 2) {
            boolean debugMode = configManager.isDebugMode();
            languageManager.sendRawMessage(sender, "&7调试模式: " + (debugMode ? "&a启用" : "&c禁用"));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
            case "enable":
                configManager.getConfig().set("settings.debug", true);
                configManager.saveConfig();
                languageManager.sendRawMessage(sender, "&a调试模式已启用！");
                break;

            case "off":
            case "disable":
                configManager.getConfig().set("settings.debug", false);
                configManager.saveConfig();
                languageManager.sendRawMessage(sender, "&c调试模式已禁用！");
                break;

            default:
                languageManager.sendRawMessage(sender, "&c用法: /colortools debug <on|off>");
                break;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("colortools.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // 第一级子命令
            List<String> subCommands = Arrays.asList("help", "reload", "test", "testcolor", "status", "toggle", "cache", "debug");
            String input = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // 第二级子命令
            String firstArg = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            switch (firstArg) {
                case "cache":
                    List<String> cacheActions = Arrays.asList("clear", "stats");
                    for (String action : cacheActions) {
                        if (action.startsWith(input)) {
                            completions.add(action);
                        }
                    }
                    break;

                case "debug":
                    List<String> debugActions = Arrays.asList("on", "off", "enable", "disable");
                    for (String action : debugActions) {
                        if (action.startsWith(input)) {
                            completions.add(action);
                        }
                    }
                    break;
            }
        }

        return completions;
    }
}
