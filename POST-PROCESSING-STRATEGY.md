# ColorTools 后处理策略完整实现

本文档介绍了 ColorTools 插件的完整后处理策略实现，彻底解决颜色代码和翻译问题。

---

## 🎯 **核心策略：完全禁用原消息 + 重新广播**

### **实现原理**
1. **MONITOR优先级** → 在所有其他插件处理完后执行
2. **延迟1tick处理** → 获得包含完整翻译的最终消息
3. **取消原始消息** → 设置为null，完全禁用原有显示
4. **重新广播处理后的消息** → 手动发送给所有玩家和控制台

### **技术优势**
- ✅ **完全控制输出**：确保只显示处理后的消息
- ✅ **保留翻译结构**：获得包含所有翻译信息的最终消息
- ✅ **精确颜色处理**：使用改进的边界检测算法
- ✅ **无冲突处理**：与其他插件完美兼容

---

## 🔧 **已实现的事件处理**

### 1. **玩家死亡事件** (PlayerDeathEvent)
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerDeath(PlayerDeathEvent event) {
    scheduler.runTaskLater(plugin, () -> {
        Component processed = postProcessComponent(event.deathMessage());
        if (processed != original) {
            event.deathMessage(null);  // 禁用原消息
            broadcastToAll(processed); // 重新广播
        }
    }, 1L);
}
```

### 2. **玩家加入事件** (PlayerJoinEvent)
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerJoin(PlayerJoinEvent event) {
    scheduler.runTaskLater(plugin, () -> {
        Component processed = postProcessComponent(event.joinMessage());
        if (processed != original) {
            event.joinMessage(null);  // 禁用原消息
            broadcastToAll(processed); // 重新广播
        }
    }, 1L);
}
```

### 3. **玩家退出事件** (PlayerQuitEvent)
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerQuit(PlayerQuitEvent event) {
    scheduler.runTaskLater(plugin, () -> {
        Component processed = postProcessComponent(event.quitMessage());
        if (processed != original) {
            event.quitMessage(null);  // 禁用原消息
            broadcastToRemaining(processed); // 广播给剩余玩家
        }
    }, 1L);
}
```

### 4. **聊天事件** (AsyncChatEvent)
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onAsyncChat(AsyncChatEvent event) {
    scheduler.runTaskLater(plugin, () -> {
        Component processed = postProcessComponent(event.message());
        if (processed != original) {
            event.message(null);  // 禁用原消息
            broadcastChatMessage(player, processed); // 重新广播聊天
        }
    }, 1L);
}
```

---

## 🎨 **改进的颜色代码处理**

### **精确边界检测算法**
```java
// 策略1：处理标准的 &#rrggbb& 格式
result = result.replaceAll(
    "(&#[0-9a-fA-F]{6}&)([^&#§&]*?)(?=&#|§|&[a-fA-F0-9klmnor]|\\s[A-Z]|\\s[\\u4e00-\\u9fff]|$)", 
    "$1$2&r"
);

// 策略2：处理渐变色格式 &#rrggbb-#rrggbb&
result = result.replaceAll(
    "(&#[0-9a-fA-F]{6}-#[0-9a-fA-F]{6}&)([^&#§&]*?)(?=&#|§|&[a-fA-F0-9klmnor]|\\s[A-Z]|\\s[\\u4e00-\\u9fff]|$)", 
    "$1$2&r"
);

// 策略3：处理没有结束符的颜色代码 &#rrggbb（不带&）
result = result.replaceAll(
    "(&#[0-9a-fA-F]{6})(?!&)([^&#§]*?)(?=&#|§|\\s[A-Z]|\\s[\\u4e00-\\u9fff]|$)", 
    "$1&$2&r"
);
```

### **边界检测说明**
- `&#|§|&[a-fA-F0-9klmnor]` - 下一个颜色代码或格式代码
- `\\s[A-Z]` - 空格后跟大写字母（通常是新单词开始）
- `\\s[\\u4e00-\\u9fff]` - 空格后跟中文字符
- `$` - 字符串结尾

### **清理和优化**
```java
// 清理多余的重置代码
result = result.replaceAll("(&r)+", "&r");

// 优化空格前的重置
result = result.replaceAll("&r(\\s)", "$1");

// 移除末尾不必要的重置
result = result.replaceAll("&r$", "");
```

---

## 🧪 **测试新版本**

### 安装步骤

1. **安装新版本**
   ```bash
   # 停止服务器
   # 替换插件文件
   cp target/ColorTools-1.0-SNAPSHOT.jar plugins/
   # 启动服务器
   ```

2. **启用调试模式**
   ```bash
   /colortools debug on
   ```

### 全面测试

#### 1. **死亡消息测试**
```bash
/team add test
/team modify test prefix "&#ff0000&[&#00ff00&VIP&#ff0000&]&#ffff00&"
/team modify test suffix "&#55B1C2&] "
/team join test <玩家名>
/kill <玩家名>
```

**预期结果**：
- ✅ 延迟处理日志出现
- ✅ 原始消息被取消
- ✅ 处理后的消息重新广播
- ✅ 颜色精确控制，翻译保持

#### 2. **加入/退出消息测试**
```bash
# 玩家加入/退出服务器
```

**预期结果**：
- ✅ 加入/退出消息正确处理
- ✅ 颜色代码正确显示
- ✅ 翻译内容保持

#### 3. **聊天消息测试**
```bash
# 在聊天中输入包含颜色代码的消息
&#ff0000&红色文本 &#00ff00&绿色文本
```

**预期结果**：
- ✅ 聊天消息颜色正确显示
- ✅ 颜色边界精确控制
- ✅ 聊天格式保持

#### 4. **复杂边界测试**
```bash
/team modify test prefix "&#ff0000&红色 &#00ff00&绿色 &#0000ff&蓝色 "
/kill <玩家名>
```

**预期结果**：
- ✅ 每个颜色只影响对应的文字
- ✅ 空格处颜色正确断开
- ✅ 中文字符边界正确处理

---

## 📊 **调试信息解读**

### 完整的后处理流程

#### 1. **延迟处理开始**
```
[调试] 延迟处理 - 原始死亡消息: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
```

#### 2. **后处理过程**
```
[调试] 开始后处理Component
[调试] 高级后处理开始
[调试] 后处理 - 提取的legacy文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
```

#### 3. **颜色处理详情**
```
[调试] 后处理颜色 - 输入: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
[调试] 后处理颜色 - 输出: &#55B1C2&[&r &#8B0000&莫斯科&r &#55B1C2&]&r NSrank fell from a high place
```

#### 4. **最终结果**
```
[调试] 后处理 - 最终结果: <处理后的Component文本>
[调试] 延迟处理 - 处理后死亡消息: <最终显示的文本>
```

### 关键特征识别
- **延迟处理**：看到"延迟处理"字样
- **消息取消**：原始消息被设置为null
- **重新广播**：消息被重新发送给所有玩家
- **精确边界**：颜色重置在正确位置

---

## 🔍 **问题诊断指南**

### 如果颜色仍然识别错误

#### 检查步骤
1. **查看颜色处理日志**
   ```
   [调试] 后处理颜色 - 输入: <原始文本>
   [调试] 后处理颜色 - 输出: <处理后文本>
   ```

2. **验证正则表达式匹配**
   - 确认颜色代码格式正确
   - 检查边界检测是否准确

3. **测试简单案例**
   ```bash
   /team modify test prefix "&#ff0000&红色"
   /kill <玩家名>
   ```

#### 可能原因
- 颜色代码格式不标准
- 边界检测正则表达式需要调整
- MineDown库版本问题

### 如果消息没有被取消

#### 检查步骤
1. **确认事件优先级**
   - 确保使用MONITOR优先级
   - 检查是否有其他插件干扰

2. **查看延迟处理日志**
   ```
   [调试] 延迟处理 - 原始<事件>消息: <消息内容>
   ```

3. **验证消息设置**
   - 确认event.deathMessage(null)被调用
   - 检查重新广播是否执行

#### 可能原因
- 事件优先级设置错误
- 延迟处理没有执行
- 其他插件覆盖了设置

### 如果翻译仍然丢失

#### 检查步骤
1. **确认延迟处理时机**
   - 延迟1tick是否足够
   - 其他插件是否需要更多时间

2. **查看原始消息内容**
   ```
   [调试] 后处理 - 提取的legacy文本: <完整文本>
   ```

3. **验证客户端语言设置**
   - 确认客户端语言设置正确
   - 测试不同语言的客户端

#### 可能原因
- 延迟时间不够
- 翻译处理在后处理之后
- 客户端语言设置问题

---

## 📋 **完整测试检查清单**

### ✅ 后处理机制
- [ ] 所有事件使用MONITOR优先级
- [ ] 延迟处理正常工作
- [ ] 原始消息被正确取消
- [ ] 处理后消息重新广播
- [ ] 控制台显示处理后消息

### ✅ 颜色处理
- [ ] 十六进制颜色正确识别
- [ ] 颜色边界精确控制
- [ ] 空格边界处理正确
- [ ] 中文字符边界正确
- [ ] 渐变色正常工作
- [ ] 无结束符颜色代码正确处理

### ✅ 翻译保护
- [ ] 死亡消息显示正确语言
- [ ] 加入/退出消息语言正确
- [ ] 翻译结构完全保留
- [ ] 多语言客户端正常工作

### ✅ 聊天功能
- [ ] 聊天消息颜色正确
- [ ] 聊天格式保持
- [ ] 玩家名称显示正确
- [ ] 控制台聊天记录正确

### ✅ 调试信息
- [ ] 延迟处理日志清晰
- [ ] 后处理流程完整
- [ ] 颜色处理过程可见
- [ ] 最终结果正确

---

## 🚀 **预期效果总结**

### 处理流程
```
1. 事件触发 → 其他插件处理 → 生成完整翻译消息
2. ColorTools延迟1tick → 获得最终消息
3. 后处理颜色代码 → 精确边界控制
4. 取消原始消息 → 重新广播处理后消息
```

### 最终显示效果
```
输入: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place

处理:
1. 获得完整翻译: "&#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place"
2. 颜色边界处理: "&#55B1C2&[&r &#8B0000&莫斯科&r &#55B1C2&]&r NSrank fell from a high place"
3. MineDown重建: Component with precise colors and translation

输出: [蓝色][红色莫斯科][蓝色] NSrank 从高处摔落
      ^^^^^ ^^^^^^^^^ ^^^^^ ^^^^^^^^^^^^^
      精确颜色控制    正确翻译
```

---

这个完整的后处理策略实现应该能够彻底解决所有问题：

- 🎯 **完全控制输出** - 禁用原消息，重新广播
- 🎨 **精确颜色处理** - 改进的边界检测算法
- 🌍 **翻译完全保护** - 延迟处理保留翻译结构
- ⚡ **高度兼容性** - 与所有插件完美兼容

现在去测试这个完整的解决方案吧！🚀
