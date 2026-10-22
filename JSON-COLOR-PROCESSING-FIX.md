# JSON颜色代码处理修复

本文档介绍了针对PLAYER_COMBAT_KILL数据包中JSON颜色代码处理的修复。

---

## 🎯 **问题分析**

从你的调试输出可以看出：

### **颜色代码处理没有生效**
```
[数据包拦截] JSON颜色处理输入: {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] JSON颜色处理输出: {"translate":"death.fell.accident.generic","with":[...]}
```
**输入和输出完全一样**，说明我们的正则表达式没有匹配到！

### **ProtocolLib警告问题**
```
[ProtocolLib] Plugin ColorTools tried to register listener for unknown packet ADD_RESOURCE_PACK
[ProtocolLib] Plugin ColorTools tried to register listener for unknown packet REMOVE_RESOURCE_PACK
```
**原因**：我们的"监听所有数据包"策略导致了很多未知数据包的警告

### **JSON结构分析**
```json
{
  "extra": [
    {"text": "&#55B1C2&["},      // 🔴 需要在这里添加&r
    {"text": "&#8B0000&莫斯科"},  // 🔴 需要在这里添加&r  
    {"text": "&#55B1C2&] "}      // 🔴 需要在这里添加&r
  ]
}
```

**问题根源**：
1. **正则表达式错误**：之前的正则要求在空格前添加&r，但JSON中的text字段末尾没有空格
2. **匹配位置错误**：需要在引号前添加&r，而不是在空格前

---

## 🔧 **修复方案**

### **1. 修复JSON颜色处理正则表达式**

#### **修复前（错误的正则）**
```java
// 要求在空格前添加&r，但JSON中text字段末尾没有空格
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"\\s]*?)\\s", "$1&r ");
```

#### **修复后（正确的正则）**
```java
// 在包含颜色代码的text字段末尾添加&r（在引号前）
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"]*?)\"", "$1&r\"");
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}&[^\"]*?)\"", "$1&r\"");

// 处理没有结束符的格式
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}[^\"]*?)\"", "$1&r\"");
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}[^\"]*?)\"", "$1&r\"");

// 特殊处理 - 如果text字段只包含颜色代码和符号，在末尾添加&r
result = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6}&\\[[^\"]*?)\"", "$1&r\"");
result = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6}&\\][^\"]*?)\"", "$1&r\"");
```

### **2. 修复ProtocolLib警告**

#### **修复前（监听所有数据包）**
```java
// 监听所有出站数据包（激进调试模式）
protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, 
    PacketType.Play.Server.getInstance()) {
```

#### **修复后（只监听特定数据包）**
```java
// 添加特定数据包调试器（仅在调试模式下注册，避免ProtocolLib警告）
if (configManager.isDebugMode()) {
    // 只监听我们关心的数据包类型，避免ProtocolLib警告
    protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR,
        PacketType.Play.Server.PLAYER_COMBAT_KILL,
        PacketType.Play.Server.SYSTEM_CHAT,
        PacketType.Play.Server.CHAT) {
```

---

## 🧪 **预期修复效果**

### **JSON处理前后对比**

#### **修复前**
```json
输入: {"text":"&#55B1C2&["}
输出: {"text":"&#55B1C2&["}  // 没有变化
```

#### **修复后**
```json
输入: {"text":"&#55B1C2&["}
输出: {"text":"&#55B1C2&[&r"}  // 添加了&r重置
```

### **完整的JSON修复示例**

#### **原始JSON**
```json
{
  "extra": [
    {"text": "&#55B1C2&["},
    {"text": "&#8B0000&莫斯科"},  
    {"text": "&#55B1C2&] "}
  ]
}
```

#### **修复后JSON**
```json
{
  "extra": [
    {"text": "&#55B1C2&[&r"},      // 添加了&r
    {"text": "&#8B0000&莫斯科&r"},  // 添加了&r
    {"text": "&#55B1C2&] &r"}      // 添加了&r
  ]
}
```

### **最终显示效果**

#### **修复前**
```
&#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
```
**问题**：颜色泄露到"NSrank fell from a high place"

#### **修复后**
```
&#55B1C2&[&r&#8B0000&莫斯科&r&#55B1C2&]&r NSrank fell from a high place
```
**效果**：颜色被正确截断，"NSrank fell from a high place"保持默认颜色

---

## 🧪 **测试新的修复**

### **安装修复版本**

1. **安装新版本**
   ```bash
   # 停止服务器
   # 替换插件文件
   cp target/ColorTools-1.0-SNAPSHOT.jar plugins/
   # 启动服务器
   ```

2. **确认调试模式**
   ```bash
   /colortools debug on
   ```

3. **触发死亡消息**
   ```bash
   /team add test
   /team modify test prefix "&#55B1C2&[&#8B0000&莫斯科&#55B1C2&]"
   /team join test <你的用户名>
   /kill <你的用户名>
   ```

### **预期调试输出**

#### **成功的JSON处理**
```
[数据包拦截] 战斗死亡原始消息 (JSON): {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] 战斗死亡消息JSON中检测到颜色代码，开始处理
[数据包拦截] JSON颜色处理输入: {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] JSON颜色处理输出: {"translate":"death.fell.accident.generic","with":[...]} (包含&r)
[数据包拦截] 战斗死亡处理后消息: &#55B1C2&[&r &#8B0000&莫斯科&r &#55B1C2&]&r NSrank fell from a high place
```

#### **关键检查点**
- [ ] JSON处理输入和输出**不同**（说明正则表达式生效）
- [ ] 处理后的消息包含**&r重置代码**
- [ ] 最终显示的死亡消息颜色被正确截断

#### **ProtocolLib警告消失**
- [ ] 启动时没有"unknown packet"警告
- [ ] 只在调试模式下注册调试器

---

## 📊 **技术细节**

### **正则表达式解析**

#### **主要匹配模式**
```java
// 匹配: "text":"&#55B1C2&[任意内容]"
// 替换为: "text":"&#55B1C2&[任意内容]&r"
"(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"]*?)\"" -> "$1&r\""
```

#### **匹配组件说明**
- `\"text\":\"` - 匹配JSON中的text字段开始
- `[^\"]*` - 匹配引号内的任意字符（除了引号）
- `&#[0-9a-fA-F]{6}&` - 匹配6位十六进制颜色代码
- `[^\"]*?` - 匹配后续的任意字符（非贪婪）
- `\"` - 匹配结束引号
- `$1&r\"` - 在原内容后添加&r，然后加上结束引号

### **多策略处理**

#### **策略1：标准颜色代码**
```java
// 处理 &#rrggbb& 格式
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}&[^\"]*?)\"", "$1&r\"");
```

#### **策略2：短格式颜色代码**
```java
// 处理 &#rgb& 格式
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{3}&[^\"]*?)\"", "$1&r\"");
```

#### **策略3：无结束符格式**
```java
// 处理 &#rrggbb 格式（没有结束&）
result = result.replaceAll("(\"text\":\"[^\"]*&#[0-9a-fA-F]{6}[^\"]*?)\"", "$1&r\"");
```

#### **策略4：特殊符号处理**
```java
// 特殊处理包含[]符号的情况
result = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6}&\\[[^\"]*?)\"", "$1&r\"");
result = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6}&\\][^\"]*?)\"", "$1&r\"");
```

---

## 🎯 **预期结果**

### **成功的修复效果**
这个修复版本应该能够：
1. **正确处理JSON中的颜色代码**：正则表达式能够匹配并添加&r
2. **消除ProtocolLib警告**：只监听特定的数据包类型
3. **实现颜色截断**：防止颜色泄露到后续文本
4. **提供详细调试信息**：显示JSON处理的完整过程

### **可能的发现**
- JSON处理输入和输出不同，说明正则表达式生效
- 处理后的消息包含&r重置代码
- 最终的死亡消息颜色被正确截断
- 启动时没有ProtocolLib警告

---

## 🚀 **总结**

这个修复解决了两个关键问题：

1. **JSON颜色处理修复**
   - 修正了正则表达式的匹配逻辑
   - 在正确的位置添加&r重置代码
   - 支持多种颜色代码格式

2. **ProtocolLib警告消除**
   - 移除了"监听所有数据包"的策略
   - 只监听我们需要的特定数据包类型
   - 只在调试模式下注册调试器

现在这个修复版本应该能够：
- ✅ **正确处理JSON中的颜色代码**
- ✅ **实现有效的颜色截断**
- ✅ **消除ProtocolLib警告**
- ✅ **提供清晰的调试信息**

现在去测试这个修复版本，我们应该能看到JSON处理真正生效，颜色泄露问题得到解决！🔍💪
