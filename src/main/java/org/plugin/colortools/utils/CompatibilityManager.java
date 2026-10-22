package org.plugin.colortools.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 兼容性管理器
 * 检测和管理与其他插件的兼容性
 */
public class CompatibilityManager {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final Map<String, Boolean> detectedPlugins;

    public CompatibilityManager(ColorTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.detectedPlugins = new HashMap<>();
    }

    /**
     * 检查兼容性
     */
    public void checkCompatibility() {
        plugin.getLogger().info("正在检查插件兼容性...");

        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // 检查常见的聊天插件
        checkPlugin("EssentialsX", pluginManager);
        checkPlugin("Essentials", pluginManager);
        checkPlugin("ChatManager", pluginManager);
        checkPlugin("HuskChat", pluginManager);
        checkPlugin("VentureChat", pluginManager);
        checkPlugin("ChatControl", pluginManager);
        checkPlugin("DeluxeChat", pluginManager);
        checkPlugin("TownyChat", pluginManager);

        // 检查其他相关插件
        checkPlugin("PlaceholderAPI", pluginManager);
        checkPlugin("Vault", pluginManager);
        checkPlugin("LuckPerms", pluginManager);
        checkPlugin("PermissionsEx", pluginManager);
        checkPlugin("GroupManager", pluginManager);

        // 检查可能冲突的插件
        checkConflictingPlugins(pluginManager);

        // 输出检测结果
        logDetectionResults();
    }

    /**
     * 检查单个插件
     */
    private void checkPlugin(String pluginName, PluginManager pluginManager) {
        Plugin detectedPlugin = pluginManager.getPlugin(pluginName);
        boolean isEnabled = detectedPlugin != null && detectedPlugin.isEnabled();

        detectedPlugins.put(pluginName, isEnabled);

        if (isEnabled && detectedPlugin != null) {
            plugin.getLogger().info("检测到插件: " + pluginName + " v" + detectedPlugin.getPluginMeta().getVersion());

            // 根据插件类型给出建议
            provideSuggestions(pluginName);
        }
    }

    /**
     * 检查可能冲突的插件
     */
    private void checkConflictingPlugins(PluginManager pluginManager) {
        // 检查可能与MineDown格式冲突的插件
        String[] potentialConflicts = {
            "ColoredSigns",
            "SignColors",
            "ChatColor",
            "ColorChat",
            "RainbowChat"
        };

        for (String conflictPlugin : potentialConflicts) {
            Plugin plugin = pluginManager.getPlugin(conflictPlugin);
            if (plugin != null && plugin.isEnabled()) {
                this.plugin.getLogger().warning("警告: 检测到可能冲突的插件 " + conflictPlugin +
                    "，可能会影响颜色格式处理！");
            }
        }
    }

    /**
     * 根据检测到的插件提供建议
     */
    private void provideSuggestions(String pluginName) {
        switch (pluginName.toLowerCase()) {
            case "essentialsx":
            case "essentials":
                if (configManager.isEssentialsXCompatible()) {
                    plugin.getLogger().info("已启用 EssentialsX 兼容模式");
                } else {
                    plugin.getLogger().info("建议在配置中启用 EssentialsX 兼容模式");
                }
                break;

            case "huskchat":
                if (configManager.isHuskChatCompatible()) {
                    plugin.getLogger().info("已启用 HuskChat 兼容模式");
                } else {
                    plugin.getLogger().info("建议在配置中启用 HuskChat 兼容模式");
                }
                break;

            case "placeholderapi":
                if (configManager.isPlaceholderAPICompatible()) {
                    plugin.getLogger().info("已启用 PlaceholderAPI 兼容模式");
                } else {
                    plugin.getLogger().info("建议在配置中启用 PlaceholderAPI 兼容模式");
                }
                break;

            case "chatmanager":
                if (configManager.isChatManagerCompatible()) {
                    plugin.getLogger().info("已启用 ChatManager 兼容模式");
                } else {
                    plugin.getLogger().info("建议在配置中启用 ChatManager 兼容模式");
                }
                break;

            case "venturechat":
            case "chatcontrol":
            case "deluxechat":
                plugin.getLogger().info("检测到聊天插件 " + pluginName + "，建议调整事件优先级以避免冲突");
                break;
        }
    }

    /**
     * 输出检测结果
     */
    private void logDetectionResults() {
        int detectedCount = (int) detectedPlugins.values().stream().mapToInt(b -> b ? 1 : 0).sum();

        if (detectedCount > 0) {
            plugin.getLogger().info("兼容性检查完成，检测到 " + detectedCount + " 个相关插件");

            // 如果检测到多个聊天插件，给出警告
            long chatPluginCount = detectedPlugins.entrySet().stream()
                .filter(entry -> entry.getValue() && isChatPlugin(entry.getKey()))
                .count();

            if (chatPluginCount > 1) {
                plugin.getLogger().warning("警告: 检测到多个聊天插件，可能会发生冲突！");
                plugin.getLogger().warning("建议只保留一个主要的聊天插件，或调整插件优先级。");
            }
        } else {
            plugin.getLogger().info("兼容性检查完成，未检测到相关插件");
        }
    }

    /**
     * 检查是否为聊天插件
     */
    private boolean isChatPlugin(String pluginName) {
        String[] chatPlugins = {
            "EssentialsX", "Essentials", "ChatManager", "HuskChat",
            "VentureChat", "ChatControl", "DeluxeChat", "TownyChat"
        };

        for (String chatPlugin : chatPlugins) {
            if (chatPlugin.equalsIgnoreCase(pluginName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查特定插件是否存在
     */
    public boolean isPluginEnabled(String pluginName) {
        return detectedPlugins.getOrDefault(pluginName, false);
    }

    /**
     * 获取所有检测到的插件
     */
    public Map<String, Boolean> getDetectedPlugins() {
        return new HashMap<>(detectedPlugins);
    }

    /**
     * 检查是否应该在其他插件之后处理
     */
    public boolean shouldProcessAfterOtherPlugins() {
        if (!configManager.isProcessAfterOtherPlugins()) {
            return false;
        }

        // 如果检测到聊天插件，建议在其之后处理
        return detectedPlugins.entrySet().stream()
            .anyMatch(entry -> entry.getValue() && isChatPlugin(entry.getKey()));
    }

    /**
     * 获取建议的事件优先级
     */
    public org.bukkit.event.EventPriority getRecommendedPriority() {
        if (shouldProcessAfterOtherPlugins()) {
            return org.bukkit.event.EventPriority.HIGH;
        } else {
            return org.bukkit.event.EventPriority.NORMAL;
        }
    }
}
