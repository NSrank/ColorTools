package org.plugin.colortools.utils;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import java.util.Map;

/**
 * 消息处理器
 * 负责将普通文本转换为支持MineDown格式的Component
 */
public class MessageProcessor {

    private final ColorTools plugin;
    private ConfigManager configManager;
    private final Map<String, Component> cache;
    private final Pattern urlPattern;
    private final Pattern colorCodePattern;

    // 统计信息
    private long processedMessages = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    public MessageProcessor(ColorTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cache = new ConcurrentHashMap<>();

        // 编译正则表达式
        this.urlPattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        this.colorCodePattern = Pattern.compile("&[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
    }

    /**
     * 处理消息文本，转换为Component
     *
     * @param text 原始文本
     * @return 处理后的Component
     */
    public Component processMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 如果插件或MineDown处理被禁用，返回原始文本
        if (!configManager.isEnabled() || !configManager.isMineDownEnabled()) {
            return Component.text(text);
        }

        // 检查缓存
        Component cached = cache.get(text);
        if (cached != null) {
            cacheHits++;
            return cached;
        }

        cacheMisses++;
        processedMessages++;

        try {
            // 预处理文本
            String processedText = preprocessText(text);

            // 调试日志 - 显示预处理结果
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[调试] 预处理文本: " + text + " -> " + processedText);
            }

            // 使用MineDown解析
            Component component = new MineDown(processedText).toComponent();

            // 缓存结果
            if (cache.size() < configManager.getCacheSize()) {
                cache.put(text, component);
            }

            // 调试日志 - 显示最终结果
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[调试] 最终组件: " +
                    LegacyComponentSerializer.legacySection().serialize(component));
            }

            return component;

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "processor.message", "处理消息时发生错误", e);

            // 发生错误时返回原始文本
            return Component.text(text);
        }
    }

    /**
     * 处理Component对象，使用全新的简化策略
     *
     * @param component 原始Component
     * @return 处理后的Component
     */
    public Component processComponent(Component component) {
        if (component == null) {
            return Component.empty();
        }

        try {
            // 使用全新的简化处理策略
            return processComponentSimple(component);

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "processor.component", "处理Component时发生错误", e);
            return component; // 发生错误时返回原始Component
        }
    }

    /**
     * 后处理Component - 全新的处理策略
     * 这个方法使用完全不同的方法来处理颜色代码
     *
     * @param component 原始Component
     * @return 处理后的Component
     */
    public Component postProcessComponent(Component component) {
        if (component == null) {
            return Component.empty();
        }

        try {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[调试] 开始后处理Component");
            }

            // 策略：将整个Component转换为legacy文本，处理颜色代码，然后重新解析
            // 但是保留原始的翻译信息
            return postProcessComponentAdvanced(component);

        } catch (Exception e) {
            LogLimiter.warn(plugin.getLogger(), "processor.postProcess", "后处理Component时发生错误", e);
            return component; // 发生错误时返回原始Component
        }
    }

    /**
     * 简化的Component处理方法 - 全新策略
     */
    private Component processComponentSimple(Component component) {
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 开始简化Component处理");
        }

        // 策略：只处理包含十六进制颜色代码的文本，保留所有其他结构
        return processComponentRecursive(component);
    }

    /**
     * 递归处理Component，只替换包含颜色代码的文本部分
     */
    private Component processComponentRecursive(Component component) {
        // 如果是文本组件且包含颜色代码，处理它
        if (component instanceof TextComponent) {
            TextComponent textComp = (TextComponent) component;
            String content = textComp.content();

            if (containsHexColorCodes(content)) {
                // 只处理这个文本组件的内容
                String processed = processHexColorsSimple(content);
                if (!processed.equals(content)) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[调试] 处理文本: " + content + " -> " + processed);
                    }

                    // 创建新的文本组件，保留原有样式
                    Component newComponent = new MineDown(processed).toComponent();

                    // 如果有子组件，递归处理它们
                    if (!textComp.children().isEmpty()) {
                        java.util.List<Component> processedChildren = new java.util.ArrayList<>();
                        for (Component child : textComp.children()) {
                            processedChildren.add(processComponentRecursive(child));
                        }
                        newComponent = newComponent.children(processedChildren);
                    }

                    return newComponent.style(textComp.style());
                }
            }
        }

        // 处理子组件（对于非文本组件或不包含颜色代码的文本组件）
        if (!component.children().isEmpty()) {
            java.util.List<Component> processedChildren = new java.util.ArrayList<>();
            boolean hasChanges = false;

            for (Component child : component.children()) {
                Component processedChild = processComponentRecursive(child);
                processedChildren.add(processedChild);
                if (processedChild != child) {
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                return component.children(processedChildren);
            }
        }

        return component; // 没有变化
    }



    /**
     * 全新方案：直接文本替换，完全保护翻译结构
     */
    private Component postProcessComponentAdvanced(Component component) {
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 直接文本替换开始");
        }

        // 全新策略：不重新构建Component，直接处理文本内容
        return processComponentDirectly(component);
    }

    /**
     * 直接处理Component，不破坏翻译结构
     */
    private Component processComponentDirectly(Component component) {
        if (component == null) {
            return Component.empty();
        }

        // 递归处理所有子组件
        Component result = component;

        // 如果是文本组件，处理其内容
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            net.kyori.adventure.text.TextComponent textComp = (net.kyori.adventure.text.TextComponent) component;
            String content = textComp.content();

            if (containsHexColorCodes(content)) {
                String processedContent = postProcessHexColorsImproved(content);

                if (!processedContent.equals(content)) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[调试] 直接替换文本: " + content + " -> " + processedContent);
                    }

                    try {
                        // 使用MineDown处理转换后的文本
                        Component coloredComponent = new MineDown(processedContent).toComponent();

                        // 保留原始样式和子组件
                        result = coloredComponent
                            .style(textComp.style())
                            .children(textComp.children());

                        if (configManager.isDebugMode()) {
                            plugin.getLogger().info("[调试] MineDown处理成功");
                        }

                    } catch (Exception e) {
                        if (configManager.isDebugMode()) {
                            plugin.getLogger().warning("[调试] MineDown处理失败，使用原始文本: " + e.getMessage());
                        }

                        // 备用方案：使用原始文本组件
                        result = Component.text(processedContent)
                            .style(textComp.style())
                            .children(textComp.children());
                    }
                }
            }
        }

        // 处理子组件
        if (!component.children().isEmpty()) {
            java.util.List<Component> processedChildren = new java.util.ArrayList<>();
            boolean hasChanges = false;

            for (Component child : component.children()) {
                Component processedChild = processComponentDirectly(child);
                processedChildren.add(processedChild);
                if (processedChild != child) {
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                result = result.children(processedChildren);
            }
        }

        return result;
    }

    /**
     * 直接文本颜色处理 - 转换为MineDown格式
     */
    private String postProcessHexColorsImproved(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 直接文本颜色处理 - 输入: " + text);
        }

        // 全新策略：将颜色代码转换为MineDown格式，然后让MineDown自然处理
        if (result.contains("&#")) {
            // 策略1：将 &#rrggbb& 转换为 <#rrggbb>text</color>
            result = result.replaceAll("(&#([0-9a-fA-F]{6})&)([^&#§&]*?)(?=\\s|&#|§|&[a-fA-F0-9klmnor]|$)", "<#$2>$3</color>");

            // 策略2：处理渐变色 &#rrggbb-#rrggbb& 转换为 <gradient:#rrggbb:#rrggbb>text</gradient>
            result = result.replaceAll("(&#([0-9a-fA-F]{6})-#([0-9a-fA-F]{6})&)([^&#§&]*?)(?=\\s|&#|§|&[a-fA-F0-9klmnor]|$)", "<gradient:#$2:#$3>$4</gradient>");

            // 策略3：处理没有结束符的颜色代码
            result = result.replaceAll("(&#([0-9a-fA-F]{6}))(?!&)([^&#§\\s]*?)(?=\\s|&#|§|$)", "<#$2>$3</color>");
        }

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[调试] 直接文本颜色处理 - 输出: " + result);
        }

        return result;
    }



    /**
     * 简化的十六进制颜色处理 - 精确控制颜色边界
     */
    private String processHexColorsSimple(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 简化颜色处理输入: " + text);
        }

        String result = text;

        // 处理十六进制颜色代码，但要精确控制边界
        // 策略：在每个颜色代码后面添加一个空的重置，让MineDown自然处理边界

        // 处理 &#rrggbb& 格式
        if (result.contains("&#")) {
            // 在颜色代码的文本后添加一个微妙的边界标记
            result = result.replaceAll("(&#[0-9a-fA-F]{6}&)([^&#§&]*?)(?=&#|§|&|\\s|$)", "$1$2&r");

            // 处理渐变色格式
            result = result.replaceAll("(&#[0-9a-fA-F]{6}-#[0-9a-fA-F]{6}&)([^&#§&]*?)(?=&#|§|&|\\s|$)", "$1$2&r");

            // 清理多余的重置代码
            result = result.replaceAll("(&r)+", "&r");

            // 如果文本以颜色代码结尾，移除末尾的重置
            result = result.replaceAll("&r$", "");
        }

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[调试] 简化颜色处理输出: " + result);
        }

        return result;
    }

    /**
     * 检查文本是否包含十六进制颜色代码
     */
    private boolean containsHexColorCodes(String text) {
        return text != null && text.contains("&#");
    }



    /**
     * 异步处理消息
     *
     * @param text 原始文本
     * @return CompletableFuture<Component>
     */
    public CompletableFuture<Component> processMessageAsync(String text) {
        if (!configManager.isAsyncProcessing()) {
            return CompletableFuture.completedFuture(processMessage(text));
        }

        return CompletableFuture.supplyAsync(() -> processMessage(text));
    }

    /**
     * 预处理文本
     *
     * @param text 原始文本
     * @return 预处理后的文本
     */
    private String preprocessText(String text) {
        String result = text;

        // 处理传统颜色代码
        if (configManager.isProcessLegacyColors()) {
            result = processLegacyColors(result);
        }

        // 处理十六进制颜色
        if (configManager.isProcessHexColors()) {
            result = processHexColors(result);
        }

        // 处理URL自动链接
        if (configManager.isProcessEvents()) {
            result = processAutoLinks(result);
        }

        // 应用过滤器
        if (configManager.isFiltersEnabled()) {
            result = applyFilters(result);
        }

        return result;
    }

    /**
     * 处理传统颜色代码
     */
    private String processLegacyColors(String text) {
        // MineDown已经支持传统颜色代码，这里可以做一些额外的处理
        return text;
    }

    /**
     * 处理十六进制颜色代码
     * MineDown 原生支持 &#rrggbb& 格式，所以我们不需要转换
     */
    private String processHexColors(String text) {
        // 调试日志 - 显示原始文本
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 十六进制颜色处理 - 原始文本: " + text);
        }

        // MineDown 原生支持以下格式，无需转换：
        // &#ff00ff& - 完整格式
        // &#f0f& - 短格式
        // &#f0f-#fff& - 渐变格式
        // &rainbow& - 彩虹格式

        // 直接返回原文本，让 MineDown 处理
        return text;
    }

    /**
     * 处理自动链接
     */
    private String processAutoLinks(String text) {
        // 自动将URL转换为可点击的链接
        return urlPattern.matcher(text).replaceAll("[$0]($0)");
    }

    /**
     * 应用过滤器
     */
    private String applyFilters(String text) {
        String result = text;

        // 移除空的MineDown标记
        result = result.replaceAll("\\[\\]\\([^\\)]*\\)", "");

        // 限制过长的链接文本
        result = result.replaceAll("\\[([^\\]]{100,})\\]\\(([^\\)]+)\\)", "[链接过长已省略]($2)");

        return result;
    }

    /**
     * 检查文本是否包含MineDown格式
     */
    public boolean containsMineDownFormat(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 调试日志 - 显示检测的文本
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] 检测MineDown格式: " + text);
        }

        // 检查是否包含MineDown格式标记
        boolean hasFormat = text.contains("**") || text.contains("##") || text.contains("__") ||
               text.contains("~~") || text.contains("??") || text.contains("[") ||
               text.contains("&rainbow&") || text.contains("&#") ||
               text.contains("&") || // 包含任何颜色代码
               colorCodePattern.matcher(text).find();

        // 调试日志 - 显示检测结果
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[调试] MineDown格式检测结果: " + hasFormat);
        }

        return hasFormat;
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        cache.clear();
        plugin.getLogger().info("消息处理缓存已清理。");
    }

    /**
     * 更新配置
     */
    public void updateConfig(ConfigManager configManager) {
        this.configManager = configManager;

        // 如果缓存大小改变，清理缓存
        if (cache.size() > configManager.getCacheSize()) {
            clearCache();
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(cache.size(), cacheHits, cacheMisses, processedMessages);
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final int cacheSize;
        private final long hits;
        private final long misses;
        private final long processedMessages;

        public CacheStats(int cacheSize, long hits, long misses, long processedMessages) {
            this.cacheSize = cacheSize;
            this.hits = hits;
            this.misses = misses;
            this.processedMessages = processedMessages;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getProcessedMessages() {
            return processedMessages;
        }

        public double getHitRatio() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        processedMessages = 0;
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * 测试MineDown格式
     */
    public Component testMineDownFormat(String format) {
        try {
            return new MineDown(format).toComponent();
        } catch (Exception e) {
            plugin.getLogger().warning("测试MineDown格式失败: " + e.getMessage());
            return Component.text("格式错误: " + format);
        }
    }
}
