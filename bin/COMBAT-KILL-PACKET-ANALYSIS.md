# PLAYER_COMBAT_KILL 数据包分析

## 🎯 **发现的JSON结构**

```json
{
  "translate": "death.fell.accident.generic",
  "with": [
    {
      "insertion": "NSrank",
      "clickEvent": {"action": "suggest_command", "value": "/tell NSrank "},
      "hoverEvent": {
        "action": "show_entity",
        "contents": {
          "type": "minecraft:player",
          "id": "705d78e8-4df2-3d7e-aafd-7342dbd47918",
          "name": {"text": "NSrank"}
        }
      },
      "extra": [
        {
          "extra": [
            {"text": "&#8B0000&莫斯科"},  // 🔴 颜色代码在这里！
            {"text": "&#55B1C2&] "}      // 🔴 颜色代码在这里！
          ],
          "text": "&#55B1C2&["           // 🔴 颜色代码在这里！
        },
        {"text": "NSrank"}
      ],
      "text": ""
    }
  ]
}
```

## 🔧 **问题分析**

### **颜色代码位置**
- `"text": "&#55B1C2&["`
- `"text": "&#8B0000&莫斯科"`
- `"text": "&#55B1C2&] "`

### **为什么现有处理器无效**
1. **数据包类型错误**：我们监听的是 `SYSTEM_CHAT`，但死亡消息使用 `PLAYER_COMBAT_KILL`
2. **JSON结构复杂**：颜色代码分散在多个嵌套的 `text` 字段中
3. **MineDown解析失败**：原始的 `&#` 格式没有被转换为Minecraft颜色代码

## 🚀 **解决方案**

### **1. 添加 PLAYER_COMBAT_KILL 监听器**
```java
// 注册玩家战斗死亡数据包监听器（死亡消息的真实来源！）
protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST,
    PacketType.Play.Server.PLAYER_COMBAT_KILL) {
    @Override
    public void onPacketSending(PacketEvent event) {
        processPlayerCombatKillPacket(event);
    }
});
```

### **2. 专门的JSON颜色处理**
```java
private String processJsonHexColors(String json) {
    // 处理JSON中的颜色代码 - 在text字段中的颜色代码后添加重置
    // 匹配 "text":"&#xxxxxx&xxx" 格式，在空格前添加&r
    result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"\\s]*?)\\s", "$1&r ");
    result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}&[^\"\\s]*?)\\s", "$1&r ");

    // 处理没有结束符的格式
    result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}[^\"\\s]*?)\\s", "$1&r ");
    result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}[^\"\\s]*?)\\s", "$1&r ");

    return result;
}
```

### **3. 实现颜色截断逻辑**
基于发现的JSON结构，我们需要：
- 直接在JSON字符串中处理颜色代码
- 在每个包含颜色代码的text字段后添加&r重置
- 确保颜色不会泄露到后续文本

## 🧪 **测试新的修复**

### **预期效果**
```
原始: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
修复后: &#55B1C2&[&r &#8B0000&莫斯科&r &#55B1C2&]&r NSrank fell from a high place
```

### **关键改进**
1. **正确的数据包类型**: 监听 PLAYER_COMBAT_KILL 而不是 SYSTEM_CHAT
2. **JSON级别处理**: 直接在JSON字符串中处理颜色代码
3. **精确的正则表达式**: 匹配JSON中的text字段格式
4. **颜色截断**: 在空格前添加&r重置代码
