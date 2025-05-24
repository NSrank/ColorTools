package org.plugin.colortools.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.plugin.colortools.ColorTools;
import org.plugin.colortools.config.ConfigManager;

/**
 * 数据包拦截器 - 拦截聊天消息数据包并处理颜色代码
 */
public class PacketInterceptor {

    private final ColorTools plugin;
    private final ConfigManager configManager;
    private final ProtocolManager protocolManager;

    public PacketInterceptor(ColorTools plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    /**
     * 注册数据包监听器
     */
    public void registerPacketListeners() {
        // 拦截系统聊天消息数据包（死亡消息、加入/退出消息等）
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!configManager.isEnabled()) {
                    return;
                }

                try {
                    processSystemChatPacket(event);
                } catch (Exception e) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().warning("[数据包拦截] 处理系统聊天数据包时发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        // 拦截玩家聊天消息数据包
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!configManager.isEnabled()) {
                    return;
                }

                try {
                    processChatPacket(event);
                } catch (Exception e) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().warning("[数据包拦截] 处理聊天数据包时发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 数据包监听器已注册");
        }
    }

    /**
     * 注销数据包监听器
     */
    public void unregisterPacketListeners() {
        protocolManager.removePacketListeners(plugin);

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 数据包监听器已注销");
        }
    }

    /**
     * 处理系统聊天数据包（死亡消息、加入/退出消息等）
     */
    private void processSystemChatPacket(PacketEvent event) {
        try {
            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
                return;
            }

            // 将WrappedChatComponent转换为Adventure Component
            String json = chatComponent.getJson();
            Component originalComponent = GsonComponentSerializer.gson().deserialize(json);

            // 调试日志
            if (configManager.isDebugMode()) {
                String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
                plugin.getLogger().info("[数据包拦截] 系统聊天原始消息: " + legacyText);
            }

            // 检查是否包含需要处理的颜色代码
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
            if (!containsHexColorCodes(legacyText)) {
                return;
            }

            // 处理颜色代码
            Component processedComponent = processPacketComponent(originalComponent);

            // 如果处理后有变化，更新数据包
            if (processedComponent != originalComponent) {
                String processedJson = GsonComponentSerializer.gson().serialize(processedComponent);
                WrappedChatComponent newChatComponent = WrappedChatComponent.fromJson(processedJson);
                event.getPacket().getChatComponents().write(0, newChatComponent);

                // 调试日志
                if (configManager.isDebugMode()) {
                    String processedText = LegacyComponentSerializer.legacySection().serialize(processedComponent);
                    plugin.getLogger().info("[数据包拦截] 系统聊天处理后消息: " + processedText);
                }
            }

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] 处理系统聊天数据包失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理聊天数据包
     */
    private void processChatPacket(PacketEvent event) {
        try {
            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
                return;
            }

            // 将WrappedChatComponent转换为Adventure Component
            String json = chatComponent.getJson();
            Component originalComponent = GsonComponentSerializer.gson().deserialize(json);

            // 调试日志
            if (configManager.isDebugMode()) {
                String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
                plugin.getLogger().info("[数据包拦截] 聊天原始消息: " + legacyText);
            }

            // 检查是否包含需要处理的颜色代码
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
            if (!containsHexColorCodes(legacyText)) {
                return;
            }

            // 处理颜色代码
            Component processedComponent = processPacketComponent(originalComponent);

            // 如果处理后有变化，更新数据包
            if (processedComponent != originalComponent) {
                String processedJson = GsonComponentSerializer.gson().serialize(processedComponent);
                WrappedChatComponent newChatComponent = WrappedChatComponent.fromJson(processedJson);
                event.getPacket().getChatComponents().write(0, newChatComponent);

                // 调试日志
                if (configManager.isDebugMode()) {
                    String processedText = LegacyComponentSerializer.legacySection().serialize(processedComponent);
                    plugin.getLogger().info("[数据包拦截] 聊天处理后消息: " + processedText);
                }
            }

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] 处理聊天数据包失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理数据包中的Component
     */
    private Component processPacketComponent(Component component) {
        if (component == null) {
            return Component.empty();
        }

        // 获取legacy格式的文本
        String legacyText = LegacyComponentSerializer.legacySection().serialize(component);

        // 处理颜色代码
        String processedText = processHexColors(legacyText);

        if (!processedText.equals(legacyText)) {
            try {
                // 使用MineDown处理颜色代码
                return new MineDown(processedText).toComponent();
            } catch (Exception e) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().warning("[数据包拦截] MineDown处理失败: " + e.getMessage());
                }
                return component;
            }
        }

        return component;
    }

    /**
     * 处理十六进制颜色代码 - 简化策略：在空格处截断颜色
     */
    private String processHexColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 颜色处理输入: " + text);
        }

        // 简化的颜色截断策略：在空格处添加&r
        if (result.contains("&#")) {
            // 策略：在每个空格前添加&r来截断颜色
            // 这样可以确保颜色不会泄露到空格后的内容
            result = result.replaceAll("(?<=&#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "&r ");

            // 处理渐变色格式
            result = result.replaceAll("(?<=&#[0-9a-fA-F]{6}-#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "&r ");

            // 处理没有结束符的颜色代码
            result = result.replaceAll("(?<=&#[0-9a-fA-F]{6}[^\\s]*?)\\s", "&r ");

            // 清理多余的重置代码
            result = result.replaceAll("(&r)+", "&r");
        }

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] 颜色处理输出: " + result);
        }

        return result;
    }

    /**
     * 检查是否包含十六进制颜色代码
     */
    private boolean containsHexColorCodes(String text) {
        return text != null && text.contains("&#");
    }
}
