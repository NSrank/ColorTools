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
<<<<<<< HEAD
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import de.themoep.minedown.adventure.MineDown;

import java.util.ArrayList;
import java.util.List;
=======
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
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
<<<<<<< HEAD

        ProtocolManager tempManager = null;
        try {
            tempManager = ProtocolLibrary.getProtocolManager();
        } catch (Exception e) {
            plugin.getLogger().warning("无法初始化ProtocolLib管理器: " + e.getMessage());
        }
        this.protocolManager = tempManager;
=======
        this.protocolManager = ProtocolLibrary.getProtocolManager();
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
    }

    /**
     * 注册数据包监听器
     */
    public void registerPacketListeners() {
<<<<<<< HEAD
        if (protocolManager == null) {
            plugin.getLogger().warning("ProtocolManager未初始化，跳过数据包监听器注册");
            return;
        }
=======
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
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

<<<<<<< HEAD
        // 注册玩家战斗死亡数据包监听器（死亡消息的真实来源！）
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_COMBAT_KILL) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!configManager.isEnabled()) {
                    return;
                }

                try {
                    processPlayerCombatKillPacket(event);
                } catch (Exception e) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().warning("[数据包拦截] 处理战斗死亡数据包时发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        // 添加特定数据包调试器（仅在调试模式下注册，避免ProtocolLib警告）
        if (configManager.isDebugMode()) {
            try {
                plugin.getLogger().info("[数据包拦截] 正在注册特定数据包调试器...");

                // 只监听我们关心的数据包类型，避免ProtocolLib警告
                protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR,
                    PacketType.Play.Server.PLAYER_COMBAT_KILL,
                    PacketType.Play.Server.SYSTEM_CHAT,
                    PacketType.Play.Server.CHAT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        try {
                            // 只在调试模式下处理
                            if (!configManager.isDebugMode()) {
                                return;
                            }

                            // 检查所有出站数据包
                            String packetType = event.getPacketType().name();

                            // 尝试获取文本内容
                            String content = extractPacketText(event);

                            // 激进搜索：寻找任何包含死亡消息关键词的数据包
                            if (content != null && (content.contains("fell from a high place") ||
                                                   content.contains("NSrank") ||
                                                   content.contains("莫斯科") ||
                                                   content.contains("&#"))) {

                                // 详细检查数据包字段
                                int chatComponentsSize = 0;
                                int stringsSize = 0;

                                try {
                                    chatComponentsSize = event.getPacket().getChatComponents().size();
                                } catch (Exception e) {
                                    // 忽略字段访问错误
                                }

                                try {
                                    stringsSize = event.getPacket().getStrings().size();
                                } catch (Exception e) {
                                    // 忽略字段访问错误
                                }

                                plugin.getLogger().info("[数据包调试] *** 发现相关数据包 ***");
                                plugin.getLogger().info("[数据包调试] - 类型: " + packetType);
                                plugin.getLogger().info("[数据包调试] - 内容: " + content);
                                plugin.getLogger().info("[数据包调试] - ChatComponents大小: " + chatComponentsSize);
                                plugin.getLogger().info("[数据包调试] - Strings大小: " + stringsSize);
                                plugin.getLogger().info("[数据包调试] - 完整类型: " + event.getPacketType().toString());

                                // 详细检查Strings字段
                                if (stringsSize > 0) {
                                    for (int i = 0; i < stringsSize; i++) {
                                        try {
                                            String stringContent = event.getPacket().getStrings().read(i);
                                            plugin.getLogger().info("[数据包调试] - String[" + i + "]: " + stringContent);
                                            if (stringContent != null && stringContent.contains("&#")) {
                                                plugin.getLogger().info("[数据包调试] *** 在Strings字段中发现颜色代码! ***");
                                            }
                                        } catch (Exception e) {
                                            plugin.getLogger().info("[数据包调试] - String[" + i + "]: 读取失败 - " + e.getMessage());
                                        }
                                    }
                                }

                                // 详细检查ChatComponents字段
                                if (chatComponentsSize > 0) {
                                    for (int i = 0; i < chatComponentsSize; i++) {
                                        try {
                                            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(i);
                                            if (chatComponent != null) {
                                                String json = chatComponent.getJson();
                                                plugin.getLogger().info("[数据包调试] - ChatComponent[" + i + "]: " + json);
                                                if (json != null && json.contains("&#")) {
                                                    plugin.getLogger().info("[数据包调试] *** 在ChatComponents字段中发现颜色代码! ***");
                                                }
                                            }
                                        } catch (Exception e) {
                                            plugin.getLogger().info("[数据包调试] - ChatComponent[" + i + "]: 读取失败 - " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 忽略调试器中的错误，但记录一下
                            if (configManager.isDebugMode()) {
                                plugin.getLogger().info("[数据包调试] 处理数据包时出错: " + e.getMessage());
                            }
                        }
                    }
            });

                plugin.getLogger().info("[数据包拦截] 特定数据包调试器已注册");
            } catch (Exception e) {
                plugin.getLogger().warning("注册调试数据包监听器失败: " + e.getMessage());
            }
        }

=======
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 数据包监听器已注册");
        }
    }

    /**
     * 注销数据包监听器
     */
    public void unregisterPacketListeners() {
<<<<<<< HEAD
        if (protocolManager == null) {
            return;
        }

=======
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
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
<<<<<<< HEAD
            // 检查ChatComponents字段是否存在且不为空
            if (event.getPacket().getChatComponents().size() == 0) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 系统聊天数据包无ChatComponent字段，跳过处理");
                }
                return;
            }

            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 系统聊天ChatComponent为null，跳过处理");
                }
=======
            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
                return;
            }

            // 将WrappedChatComponent转换为Adventure Component
            String json = chatComponent.getJson();
            Component originalComponent = GsonComponentSerializer.gson().deserialize(json);

<<<<<<< HEAD
            // 详细调试日志 - 查看实际收到的数据格式
            if (configManager.isDebugMode()) {
                String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
                String plainText = PlainTextComponentSerializer.plainText().serialize(originalComponent);
                plugin.getLogger().info("[数据包拦截] 系统聊天原始消息 (Legacy): " + legacyText);
                plugin.getLogger().info("[数据包拦截] 系统聊天原始消息 (Plain): " + plainText);
                plugin.getLogger().info("[数据包拦截] 系统聊天原始消息 (JSON): " + json);
=======
            // 调试日志
            if (configManager.isDebugMode()) {
                String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
                plugin.getLogger().info("[数据包拦截] 系统聊天原始消息: " + legacyText);
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
            }

            // 检查是否包含需要处理的颜色代码
            String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
<<<<<<< HEAD

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 检查颜色代码，文本: " + legacyText);
                plugin.getLogger().info("[数据包拦截] 是否包含颜色代码: " + containsHexColorCodes(legacyText));
            }

            if (!containsHexColorCodes(legacyText)) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 未检测到颜色代码，跳过处理");
                }
                return;
            }

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 检测到颜色代码，开始处理");
            }

=======
            if (!containsHexColorCodes(legacyText)) {
                return;
            }

>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
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
<<<<<<< HEAD
            // 检查ChatComponents字段是否存在且不为空
            if (event.getPacket().getChatComponents().size() == 0) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 聊天数据包无ChatComponent字段，跳过处理");
                }
                return;
            }

            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 聊天ChatComponent为null，跳过处理");
                }
=======
            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
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
<<<<<<< HEAD
     * 处理十六进制颜色代码 - 基于MineDown官方语法
=======
     * 处理十六进制颜色代码 - 简化策略：在空格处截断颜色
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
     */
    private String processHexColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 颜色处理输入: " + text);
        }

<<<<<<< HEAD
        // 基于MineDown官方语法的颜色处理
        if (result.contains("&#")) {
            // 策略1：处理完整格式 &#rrggbb& - 在空格前添加&r
            result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1&r ");

            // 策略2：处理短格式 &#rgb& - 在空格前添加&r
            result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\s]*?)\\s", "$1&r ");

            // 策略3：处理渐变格式 &#rrggbb-#rrggbb& - 在空格前添加&r
            result = result.replaceAll("(&#[0-9a-fA-F]{6}-#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1&r ");
            result = result.replaceAll("(&#[0-9a-fA-F]{3}-#[0-9a-fA-F]{3}&[^\\s]*?)\\s", "$1&r ");

            // 策略4：处理没有结束符的格式 &#rrggbb - 在空格前添加&r
            result = result.replaceAll("(&#[0-9a-fA-F]{6}[^\\s&#]*?)\\s", "$1&r ");
            result = result.replaceAll("(&#[0-9a-fA-F]{3}[^\\s&#]*?)\\s", "$1&r ");

            // 策略5：清理多余的重置代码
            result = result.replaceAll("(&r)+", "&r");

            // 策略6：移除末尾的重置（如果在空格前）
            result = result.replaceAll("&r\\s+", " ");
=======
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
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
        }

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] 颜色处理输出: " + result);
        }

        return result;
    }

    /**
<<<<<<< HEAD
     * 检查是否包含十六进制颜色代码 - 简化检测
     */
    private boolean containsHexColorCodes(String text) {
        if (text == null) {
            return false;
        }

        // 简化检测：只检查&#格式
        boolean hasHex = text.contains("&#");

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 颜色代码检测详情:");
            plugin.getLogger().info("[数据包拦截] - 文本内容: " + text);
            plugin.getLogger().info("[数据包拦截] - 包含&#: " + hasHex);
            if (hasHex) {
                plugin.getLogger().info("[数据包拦截] - 检测到颜色代码，将进行处理");
            } else {
                plugin.getLogger().info("[数据包拦截] - 未检测到颜色代码，跳过处理");
            }
        }

        return hasHex;
    }

    /**
     * 处理玩家战斗死亡数据包（PLAYER_COMBAT_KILL）
     */
    private void processPlayerCombatKillPacket(PacketEvent event) {
        try {
            // 检查ChatComponents字段是否存在且不为空
            if (event.getPacket().getChatComponents().size() == 0) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 战斗死亡数据包无ChatComponent字段，跳过处理");
                }
                return;
            }

            // 获取数据包中的消息内容
            WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
            if (chatComponent == null) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 战斗死亡ChatComponent为null，跳过处理");
                }
                return;
            }

            // 新思路：直接使用MineDown处理WrappedChatComponent
            if (configManager.isDebugMode()) {
                String json = chatComponent.getJson();
                Component originalComponent = GsonComponentSerializer.gson().deserialize(json);
                String legacyText = LegacyComponentSerializer.legacySection().serialize(originalComponent);
                String plainText = PlainTextComponentSerializer.plainText().serialize(originalComponent);
                plugin.getLogger().info("[数据包拦截] 战斗死亡原始消息 (Legacy): " + legacyText);
                plugin.getLogger().info("[数据包拦截] 战斗死亡原始消息 (Plain): " + plainText);
                plugin.getLogger().info("[数据包拦截] 战斗死亡原始消息 (JSON): " + json);
            }

            // 检查是否包含颜色代码
            String json = chatComponent.getJson();
            if (!json.contains("&#")) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 战斗死亡消息JSON中未检测到颜色代码，跳过处理");
                }
                return;
            }

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 战斗死亡消息JSON中检测到颜色代码，开始处理");
            }

            // 破圈之法：基于Velocitab和MineDown官方语法的正确处理
            try {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 使用基于Velocitab经验的MineDown正确处理方法");
                }

                // 直接使用MineDown处理原始JSON中的颜色代码
                String processedJson = processJsonWithVelocitabApproach(json);

                if (!processedJson.equals(json)) {
                    // 创建新的数据包
                    WrappedChatComponent newChatComponent = WrappedChatComponent.fromJson(processedJson);
                    event.getPacket().getChatComponents().write(0, newChatComponent);

                    // 调试日志
                    if (configManager.isDebugMode()) {
                        Component processedComponent = GsonComponentSerializer.gson().deserialize(processedJson);
                        String processedText = LegacyComponentSerializer.legacySection().serialize(processedComponent);
                        plugin.getLogger().info("[数据包拦截] 战斗死亡处理后消息: " + processedText);
                        plugin.getLogger().info("[数据包拦截] 战斗死亡处理后JSON: " + processedJson);
                        plugin.getLogger().info("[数据包拦截] Velocitab方法处理成功！");
                    }
                } else if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] Velocitab方法处理没有变化");
                }

            } catch (Exception e) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().warning("[数据包拦截] Velocitab方法处理失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] 处理战斗死亡数据包失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 基于Velocitab经验的JSON处理方法
     */
    private String processJsonWithVelocitabApproach(String json) {
        if (json == null || !json.contains("&#")) {
            return json;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] Velocitab方法JSON处理输入: " + json);
        }

        try {
            // 步骤1：解析原始JSON为Component
            Component originalComponent = GsonComponentSerializer.gson().deserialize(json);

            // 步骤2：递归处理Component中的所有text字段，添加颜色重置
            Component processedComponent = processComponentRecursivelyWithColorReset(originalComponent);

            // 步骤3：序列化回JSON
            String processedJson = GsonComponentSerializer.gson().serialize(processedComponent);

            if (configManager.isDebugMode() && !processedJson.equals(json)) {
                plugin.getLogger().info("[数据包拦截] Velocitab方法JSON处理输出: " + processedJson);
            }

            return processedJson;

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] Velocitab方法JSON处理失败: " + e.getMessage());
            }
            return json;
        }
    }

    /**
     * 递归处理Component中的颜色代码，基于MineDown官方语法
     */
    private Component processComponentRecursivelyWithColorReset(Component component) {
        if (component == null) {
            return component;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 递归处理Component类型: " + component.getClass().getSimpleName());
        }

        // 处理TextComponent
        if (component instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) component;
            String content = textComponent.content();

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 发现TextComponent，内容: '" + content + "'");
            }

            if (content != null && content.contains("&#")) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] TextComponent包含颜色代码，开始处理: " + content);
                }

                // 使用MineDown解析颜色代码并添加重置
                String processedContent = processTextWithMineDownAndColorReset(content);

                if (!processedContent.equals(content)) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[数据包拦截] TextComponent处理结果: " + processedContent);
                    }

                    // 使用MineDown解析处理后的文本，然后提取纯文本内容
                    try {
                        Component mineDownComponent = new MineDown(processedContent).toComponent();

                        if (configManager.isDebugMode()) {
                            plugin.getLogger().info("[数据包拦截] MineDown解析成功，创建新的TextComponent");
                        }

                        // 如果MineDown解析成功，返回解析后的Component
                        // 递归处理子组件
                        if (!textComponent.children().isEmpty()) {
                            List<Component> processedChildren = new ArrayList<>();
                            for (Component child : textComponent.children()) {
                                processedChildren.add(processComponentRecursivelyWithColorReset(child));
                            }
                            return mineDownComponent.children(processedChildren);
                        } else {
                            return mineDownComponent;
                        }

                    } catch (Exception e) {
                        if (configManager.isDebugMode()) {
                            plugin.getLogger().warning("[数据包拦截] MineDown解析失败，使用原始方法: " + e.getMessage());
                        }

                        // 如果MineDown解析失败，回退到原始方法
                        TextComponent.Builder builder = Component.text()
                            .content(processedContent)
                            .style(textComponent.style());

                        // 递归处理子组件
                        for (Component child : textComponent.children()) {
                            builder.append(processComponentRecursivelyWithColorReset(child));
                        }

                        return builder.build();
                    }
                } else if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] TextComponent内容处理后没有变化");
                }
            } else if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] TextComponent不包含颜色代码");
            }
        }

        // 处理TranslatableComponent的arguments
        if (component instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent) component;

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 发现TranslatableComponent，翻译键: " + translatableComponent.key());
                // 使用已弃用但兼容的args()方法
                plugin.getLogger().info("[数据包拦截] TranslatableComponent有 " + translatableComponent.args().size() + " 个参数");
            }

            // 使用已弃用但兼容的args()方法
            if (!translatableComponent.args().isEmpty()) {
                List<Component> processedArguments = new ArrayList<>();
                boolean hasChanges = false;

                for (Component argument : translatableComponent.args()) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[数据包拦截] 处理TranslatableComponent参数: " + argument.getClass().getSimpleName());
                    }

                    Component processedArgument = processComponentRecursivelyWithColorReset(argument);
                    processedArguments.add(processedArgument);
                    if (processedArgument != argument) {
                        hasChanges = true;
                    }
                }

                if (hasChanges) {
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[数据包拦截] TranslatableComponent参数有变化，重新构建");
                    }

                    // 重新构建TranslatableComponent，使用已弃用但兼容的args()方法
                    TranslatableComponent.Builder builder = Component.translatable()
                        .key(translatableComponent.key())
                        .args(processedArguments)
                        .style(translatableComponent.style());

                    // 递归处理子组件
                    for (Component child : translatableComponent.children()) {
                        builder.append(processComponentRecursivelyWithColorReset(child));
                    }

                    return builder.build();
                } else if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] TranslatableComponent参数没有变化");
                }
            }
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 不是TextComponent或TranslatableComponent，类型: " + component.getClass().getSimpleName());
        }

        // 处理其他类型的Component，递归处理子组件
        if (!component.children().isEmpty()) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] Component有 " + component.children().size() + " 个子组件，开始递归处理");
            }

            List<Component> processedChildren = new ArrayList<>();
            boolean hasChanges = false;

            for (Component child : component.children()) {
                Component processedChild = processComponentRecursivelyWithColorReset(child);
                processedChildren.add(processedChild);
                if (processedChild != child) {
                    hasChanges = true;
                }
            }

            if (hasChanges) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 子组件有变化，重新构建Component");
                }
                // 重新构建Component，保持原有属性但使用处理后的子组件
                return component.children(processedChildren);
            } else if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 子组件没有变化");
            }
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] Component没有子组件");
        }

        return component;
    }

    /**
     * 使用MineDown处理文本并添加颜色重置
     */
    private String processTextWithMineDownAndColorReset(String text) {
        if (text == null || !text.contains("&#")) {
            return text;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] MineDown处理输入: " + text);
        }

        // 首先添加颜色重置
        String result = addColorResetToMineDownText(text);

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] MineDown处理输出: " + result);
        }

        return result;
    }

    /**
     * 基于MineDown官方语法为文本添加颜色重置
     */
    private String addColorResetToMineDownText(String text) {
        if (text == null || !text.contains("&#")) {
            return text;
        }

        String result = text;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] MineDown颜色重置处理输入: " + text);
            plugin.getLogger().info("[数据包拦截] 文本长度: " + text.length() + ", 内容: '" + text + "'");
        }

        // 基于MineDown官方语法：&#rrggbb&Text
        // 修复的策略：直接在每个颜色代码文本的末尾添加&r

        // 策略1：在颜色代码段后的空格前添加&r
        // 修复正则表达式：使用更精确的匹配
        String test1 = result.replaceAll("(&#[0-9a-fA-F]{6}&.*?)\\s", "$1&r ");
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略1测试: '" + result + "' -> '" + test1 + "'");
            plugin.getLogger().info("[数据包拦截] 策略1是否相等: " + test1.equals(result));
        }
        if (!test1.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略1生效: " + test1);
            }
            result = test1;
        }

        // 策略2：在颜色代码段后的]符号前添加&r
        // 修复正则表达式：使用更精确的匹配
        String test2 = result.replaceAll("(&#[0-9a-fA-F]{6}&.*?)\\]", "$1&r]");
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略2测试: '" + result + "' -> '" + test2 + "'");
            plugin.getLogger().info("[数据包拦截] 策略2是否相等: " + test2.equals(result));
        }
        if (!test2.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略2生效: " + test2);
            }
            result = test2;
        }

        // 策略3：在文本末尾添加&r（如果以颜色代码结尾）
        // 修复正则表达式：使用更精确的匹配
        String test3 = result.replaceAll("(&#[0-9a-fA-F]{6}&.*)$", "$1&r");
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略3测试: '" + result + "' -> '" + test3 + "'");
            plugin.getLogger().info("[数据包拦截] 策略3是否相等: " + test3.equals(result));

            // 额外调试：测试正则表达式匹配
            if (result.matches(".*&#[0-9a-fA-F]{6}&.*")) {
                plugin.getLogger().info("[数据包拦截] 策略3正则表达式匹配成功");
            } else {
                plugin.getLogger().info("[数据包拦截] 策略3正则表达式匹配失败");
            }
        }
        if (!test3.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略3生效: " + test3);
            }
            result = test3;
        }

        // 新策略4：直接在每个完整的颜色代码文本末尾添加&r（针对我们的具体情况）
        if (result.equals(text)) {
            // 如果前面的策略都没有生效，使用更简单直接的方法
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 前面策略都没生效，使用策略4");
            }

            // 直接在整个文本末尾添加&r（如果文本包含颜色代码）
            if (text.matches(".*&#[0-9a-fA-F]{6}&.*")) {
                result = text + "&r";
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 策略4生效: " + result);
                }
            }
        }

        // 清理多余的重置代码
        result = result.replaceAll("(&r)+", "&r");

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] MineDown颜色重置处理输出: " + result);
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] MineDown颜色重置处理没有变化");
        }

        return result;
    }

    /**
     * 处理Legacy文本中的颜色代码并添加重置（已弃用）
     */
    @Deprecated
    private String processLegacyTextWithColorReset(String legacyText) {
        if (legacyText == null || !legacyText.contains("&#")) {
            return legacyText;
        }

        String result = legacyText;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] Legacy颜色重置处理输入: " + legacyText);
        }

        // 在Legacy文本中添加颜色重置 - 在空格前添加§r
        // 策略1：在颜色代码段结束后的空格前添加§r
        result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1§r ");
        result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\s]*?)\\s", "$1§r ");

        // 策略2：在]符号前添加§r
        result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\]]*?)\\]", "$1§r]");
        result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\]]*?)\\]", "$1§r]");

        // 策略3：在文本末尾添加§r
        result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)$", "$1§r");
        result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\s]*?)$", "$1§r");

        // 清理多余的重置代码
        result = result.replaceAll("(§r)+", "§r");

        if (configManager.isDebugMode() && !result.equals(legacyText)) {
            plugin.getLogger().info("[数据包拦截] Legacy颜色重置处理输出: " + result);
        }

        return result;
    }

    /**
     * 保留翻译组件的颜色处理方法（已弃用）
     */
    @Deprecated
    private Component processComponentPreservingTranslation(Component component, String originalJson) {
        if (component == null) {
            return component;
        }

        try {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 保留翻译处理输入JSON: " + originalJson);
            }

            // 直接在JSON字符串中处理颜色代码，保留翻译结构
            String processedJson = processJsonColorCodes(originalJson);

            if (!processedJson.equals(originalJson)) {
                Component processedComponent = GsonComponentSerializer.gson().deserialize(processedJson);

                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 保留翻译处理输出JSON: " + processedJson);
                }

                return processedComponent;
            }

            return component;

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] 保留翻译处理失败: " + e.getMessage());
            }
            return component;
        }
    }

    /**
     * 在JSON字符串中直接处理颜色代码
     */
    private String processJsonColorCodes(String json) {
        if (json == null || !json.contains("&#")) {
            return json;
        }

        String result = json;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] JSON颜色代码处理输入: " + json);
        }

        // 修复的JSON颜色代码处理 - 简化正则表达式避免破坏JSON结构
        // 策略1：在text字段中包含颜色代码的末尾添加§r
        result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"]*?)\"", "$1§r\"");
        result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}&[^\"]*?)\"", "$1§r\"");

        // 清理多余的重置代码
        result = result.replaceAll("(§r)+", "§r");

        if (configManager.isDebugMode() && !result.equals(json)) {
            plugin.getLogger().info("[数据包拦截] JSON颜色代码处理输出: " + result);
        }

        return result;
    }

    /**
     * 使用MineDown处理Component中的颜色代码
     */
    private Component processComponentWithMineDown(Component component) {
        if (component == null) {
            return component;
        }

        try {
            // 将Component转换为Legacy文本
            String legacyText = LegacyComponentSerializer.legacySection().serialize(component);

            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] MineDown处理输入: " + legacyText);
            }

            // 检查是否包含颜色代码
            if (!legacyText.contains("&#")) {
                return component;
            }

            // 使用简单的字符串替换添加颜色重置
            String processedText = addColorResetToText(legacyText);

            if (configManager.isDebugMode() && !processedText.equals(legacyText)) {
                plugin.getLogger().info("[数据包拦截] MineDown处理输出: " + processedText);
            }

            // 如果文本有变化，使用MineDown重新解析
            if (!processedText.equals(legacyText)) {
                Component processedComponent = new MineDown(processedText).toComponent();
                return processedComponent;
            }

            return component;

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] MineDown Component处理失败: " + e.getMessage());
            }
            return component;
        }
    }

    /**
     * 在文本中添加颜色重置代码
     */
    private String addColorResetToText(String text) {
        if (text == null || !text.contains("&#")) {
            return text;
        }

        String result = text;

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 颜色重置处理输入: " + text);

            // 详细分析文本内容
            plugin.getLogger().info("[数据包拦截] 文本长度: " + text.length());
            plugin.getLogger().info("[数据包拦截] 是否包含&#: " + text.contains("&#"));

            // 查找所有的颜色代码
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("&#[0-9a-fA-F]{6}&[^\\s]*");
            java.util.regex.Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                plugin.getLogger().info("[数据包拦截] 找到颜色代码: '" + matcher.group() + "' 位置: " + matcher.start() + "-" + matcher.end());
            }
        }

        // 修复的颜色代码处理 - 在空格前添加§r来避免误截断
        // 策略1：在颜色代码段结束后的空格前添加§r
        String test1 = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1§r ");
        if (!test1.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略1匹配成功，添加了§r: " + test1);
            }
            result = test1;
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略1没有匹配");
        }

        // 策略2：在]符号前添加§r（避免在]后误截断）
        String test2 = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\]]*?)\\]", "$1§r]");
        if (!test2.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略2匹配成功，在]前添加了§r: " + test2);
            }
            result = test2;
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略2没有匹配");
        }

        // 策略3：在文本末尾添加§r（如果没有空格）
        String test3 = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)$", "$1§r");
        if (!test3.equals(result)) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] 策略3匹配成功，在末尾添加了§r: " + test3);
            }
            result = test3;
        } else if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] 策略3没有匹配");
        }

        // 清理多余的重置代码
        result = result.replaceAll("(§r)+", "§r");

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] 颜色重置处理输出: " + result);
        }

        return result;
    }

    /**
     * 处理JSON中的十六进制颜色代码 - 使用Component解析和重构方法（已弃用）
     */
    @Deprecated
    private String processJsonHexColors(String json) {
        if (json == null || !json.contains("&#")) {
            return json;
        }

        if (configManager.isDebugMode()) {
            plugin.getLogger().info("[数据包拦截] JSON颜色处理输入: " + json);
        }

        try {
            // 新方法：使用Adventure Component解析和重构
            Component originalComponent = GsonComponentSerializer.gson().deserialize(json);
            Component processedComponent = processComponentRecursively(originalComponent);

            String result = GsonComponentSerializer.gson().serialize(processedComponent);

            if (configManager.isDebugMode() && !result.equals(json)) {
                plugin.getLogger().info("[数据包拦截] JSON颜色处理输出: " + result);
                plugin.getLogger().info("[数据包拦截] Component处理成功！");
            } else if (configManager.isDebugMode()) {
                plugin.getLogger().info("[数据包拦截] Component处理没有变化");
            }

            return result;

        } catch (Exception e) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().warning("[数据包拦截] Component处理失败，回退到正则表达式方法: " + e.getMessage());
            }

            // 回退到正则表达式方法
            return processJsonHexColorsWithRegex(json);
        }
    }

    /**
     * 递归处理Component中的颜色代码
     */
    private Component processComponentRecursively(Component component) {
        if (component == null) {
            return Component.empty();
        }

        // 处理TextComponent
        if (component instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) component;
            String content = textComponent.content();

            if (content != null && content.contains("&#")) {
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("[数据包拦截] 处理TextComponent: " + content);
                }

                // 处理颜色代码并添加重置
                String processedContent = processTextWithColorReset(content);

                if (configManager.isDebugMode() && !processedContent.equals(content)) {
                    plugin.getLogger().info("[数据包拦截] TextComponent处理结果: " + processedContent);
                }

                // 创建新的TextComponent
                TextComponent.Builder builder = Component.text()
                    .content(processedContent)
                    .style(textComponent.style());

                // 递归处理子组件
                for (Component child : textComponent.children()) {
                    builder.append(processComponentRecursively(child));
                }

                return builder.build();
            }
        }

        // 处理其他类型的Component，递归处理子组件
        if (!component.children().isEmpty()) {
            // 简化处理：创建一个新的Component包含处理后的子组件
            Component processedComponent = component;
            for (Component child : component.children()) {
                Component processedChild = processComponentRecursively(child);
                // 如果子组件有变化，我们需要重新构建
                if (processedChild != child) {
                    // 对于复杂的Component重构，我们回退到正则表达式方法
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("[数据包拦截] 检测到复杂Component结构，回退到正则表达式方法");
                    }
                    throw new UnsupportedOperationException("Complex component structure");
                }
            }
            return processedComponent;
        }

        return component;
    }

    /**
     * 处理文本中的颜色代码并添加重置
     */
    private String processTextWithColorReset(String text) {
        if (text == null || !text.contains("&#")) {
            return text;
        }

        String result = text;

        // 基于MineDown语法处理颜色代码
        // 在每个颜色代码后添加&r来截断颜色

        // 处理完整格式 &#rrggbb&xxx
        result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*)", "$1&r");
        result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\s]*)", "$1&r");

        // 处理不完整格式 &#rrggbbxxx（没有结束&）
        result = result.replaceAll("(&#[0-9a-fA-F]{6})([^\\s&#]*)", "$1&$2&r");
        result = result.replaceAll("(&#[0-9a-fA-F]{3})([^\\s&#]*)", "$1&$2&r");

        // 清理多余的重置代码
        result = result.replaceAll("(&r)+", "&r");

        if (configManager.isDebugMode() && !result.equals(text)) {
            plugin.getLogger().info("[数据包拦截] 文本颜色处理: '" + text + "' -> '" + result + "'");
        }

        return result;
    }

    /**
     * 回退的正则表达式方法（保留作为备用）
     */
    private String processJsonHexColorsWithRegex(String json) {
        String result = json;

        // 简单的正则表达式替换
        result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"]*?)\"", "$1&r\"");
        result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}&[^\"]*?)\"", "$1&r\"");

        return result;
    }

    /**
     * 从数据包中提取文本内容（用于调试）
     */
    private String extractPacketText(PacketEvent event) {
        StringBuilder result = new StringBuilder();

        try {
            // 尝试从ChatComponents字段获取文本
            if (event.getPacket().getChatComponents().size() > 0) {
                for (int i = 0; i < event.getPacket().getChatComponents().size(); i++) {
                    try {
                        WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(i);
                        if (chatComponent != null) {
                            String json = chatComponent.getJson();
                            Component component = GsonComponentSerializer.gson().deserialize(json);
                            String text = LegacyComponentSerializer.legacySection().serialize(component);
                            if (result.length() > 0) result.append(" | ");
                            result.append("ChatComponent[").append(i).append("]: ").append(text);
                        }
                    } catch (Exception e) {
                        // 忽略单个组件的错误
                    }
                }
            }

            // 尝试从Strings字段获取文本
            if (event.getPacket().getStrings().size() > 0) {
                for (int i = 0; i < event.getPacket().getStrings().size(); i++) {
                    try {
                        String text = event.getPacket().getStrings().read(i);
                        if (text != null && !text.isEmpty()) {
                            if (result.length() > 0) result.append(" | ");
                            result.append("String[").append(i).append("]: ").append(text);
                        }
                    } catch (Exception e) {
                        // 忽略单个字符串的错误
                    }
                }
            }

            // 如果找到了任何内容，返回第一个ChatComponent的纯文本（用于简单检查）
            if (result.length() > 0) {
                try {
                    if (event.getPacket().getChatComponents().size() > 0) {
                        WrappedChatComponent chatComponent = event.getPacket().getChatComponents().read(0);
                        if (chatComponent != null) {
                            String json = chatComponent.getJson();
                            Component component = GsonComponentSerializer.gson().deserialize(json);
                            return LegacyComponentSerializer.legacySection().serialize(component);
                        }
                    }
                    if (event.getPacket().getStrings().size() > 0) {
                        return event.getPacket().getStrings().read(0);
                    }
                } catch (Exception e) {
                    // 忽略错误
                }
            }

            return result.length() > 0 ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
=======
     * 检查是否包含十六进制颜色代码
     */
    private boolean containsHexColorCodes(String text) {
        return text != null && text.contains("&#");
>>>>>>> c01c6def98bea63ce10b8204a905ddb23292e87b
    }
}
