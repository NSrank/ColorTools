# 详细正则表达式调试

本文档介绍了增强的正则表达式调试功能，用于找出为什么颜色代码处理没有生效。

---

## 🎯 **问题分析**

从你的调试输出可以看出：
```
[数据包拦截] JSON颜色处理输入: {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] JSON颜色处理输出: {"translate":"death.fell.accident.generic","with":[...]}
```

**输入和输出完全一样**，这说明我们的正则表达式没有匹配到任何内容！

### **实际的JSON片段**
```json
{"text":"&#8B0000&莫斯科"}
{"text":"&#55B1C2&] "}
{"text":"&#55B1C2&["}
```

### **我们的正则表达式**
```java
// 策略1：处理完整格式
"(\"text\":\"&#[0-9a-fA-F]{6}&[^\"]*?)\"" -> "$1&r\""

// 策略2：处理不完整格式
"(\"text\":\"&#[0-9a-fA-F]{6})([^&\"]+?)\"" -> "$1&$2&r\""

// 策略3：只有颜色代码
"(\"text\":\"&#[0-9a-fA-F]{6})\"" -> "$1&r\""
```

---

## 🔧 **增强调试功能**

### **逐个测试正则表达式**

我添加了详细的调试代码，会逐个测试每个正则表达式策略：

```java
// 详细调试：逐个测试正则表达式
if (configManager.isDebugMode()) {
    // 测试策略1
    String test1 = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6}&[^\"]*?)\"", "$1&r\"");
    if (!test1.equals(result)) {
        plugin.getLogger().info("[数据包拦截] 策略1匹配成功: " + test1);
    } else {
        plugin.getLogger().info("[数据包拦截] 策略1没有匹配");
    }
    
    // 测试策略2
    String test2 = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6})([^&\"]+?)\"", "$1&$2&r\"");
    if (!test2.equals(result)) {
        plugin.getLogger().info("[数据包拦截] 策略2匹配成功: " + test2);
    } else {
        plugin.getLogger().info("[数据包拦截] 策略2没有匹配");
    }
    
    // 测试策略3
    String test3 = result.replaceAll("(\"text\":\"&#[0-9a-fA-F]{6})\"", "$1&r\"");
    if (!test3.equals(result)) {
        plugin.getLogger().info("[数据包拦截] 策略3匹配成功: " + test3);
    } else {
        plugin.getLogger().info("[数据包拦截] 策略3没有匹配");
    }
}
```

---

## 🧪 **测试新的调试版本**

### **安装调试版本**

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

#### **成功的调试输出**
```
[数据包拦截] JSON颜色处理输入: {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] 策略1匹配成功: {"translate":"death.fell.accident.generic","with":[...]} (包含&r)
[数据包拦截] 策略2没有匹配
[数据包拦截] 策略3没有匹配
[数据包拦截] JSON颜色处理输出: {"translate":"death.fell.accident.generic","with":[...]} (包含&r)
```

#### **失败的调试输出**
```
[数据包拦截] JSON颜色处理输入: {"translate":"death.fell.accident.generic","with":[...]}
[数据包拦截] 策略1没有匹配
[数据包拦截] 策略2没有匹配
[数据包拦截] 策略3没有匹配
[数据包拦截] JSON颜色处理输出: {"translate":"death.fell.accident.generic","with":[...]} (没有变化)
```

---

## 📊 **可能的调试结果分析**

### **情况1：策略1匹配成功**
**说明**：正则表达式正确匹配了完整格式的颜色代码
**结果**：JSON处理输出应该包含&r重置代码
**下一步**：检查最终的死亡消息是否正确显示

### **情况2：策略2匹配成功**
**说明**：正则表达式匹配了不完整格式的颜色代码
**结果**：JSON处理输出应该包含添加的&和&r
**下一步**：检查颜色代码是否被正确修复

### **情况3：策略3匹配成功**
**说明**：正则表达式匹配了只有颜色代码的情况
**结果**：JSON处理输出应该包含&r
**下一步**：检查颜色重置是否生效

### **情况4：所有策略都没有匹配**
**说明**：我们的正则表达式有根本性问题
**可能原因**：
- JSON结构与我们的假设不同
- 正则表达式语法错误
- 字符编码问题
- 转义字符问题

---

## 🔍 **进一步调试策略**

### **如果所有策略都没有匹配**

#### **1. 检查实际的JSON结构**
```java
// 添加更详细的JSON分析
if (configManager.isDebugMode()) {
    plugin.getLogger().info("[数据包拦截] JSON长度: " + json.length());
    plugin.getLogger().info("[数据包拦截] 是否包含&#: " + json.contains("&#"));
    plugin.getLogger().info("[数据包拦截] 是否包含text: " + json.contains("text"));
    
    // 查找所有的text字段
    Pattern pattern = Pattern.compile("\"text\":\"[^\"]*\"");
    Matcher matcher = pattern.matcher(json);
    while (matcher.find()) {
        plugin.getLogger().info("[数据包拦截] 找到text字段: " + matcher.group());
    }
}
```

#### **2. 简化正则表达式测试**
```java
// 测试最简单的匹配
String simpleTest = result.replaceAll("\"text\":\"&#", "\"text\":\"&#TEST");
if (!simpleTest.equals(result)) {
    plugin.getLogger().info("[数据包拦截] 简单匹配成功");
} else {
    plugin.getLogger().info("[数据包拦截] 连简单匹配都失败");
}
```

#### **3. 检查字符编码问题**
```java
// 检查特殊字符
if (configManager.isDebugMode()) {
    for (int i = 0; i < json.length(); i++) {
        char c = json.charAt(i);
        if (c == '&' || c == '#') {
            plugin.getLogger().info("[数据包拦截] 位置" + i + "发现字符: " + c + " (Unicode: " + (int)c + ")");
        }
    }
}
```

---

## 📋 **调试检查清单**

### ✅ 基础检查
- [ ] 插件正常启动
- [ ] 调试模式已启用
- [ ] 死亡消息被正确拦截

### ✅ 正则表达式测试
- [ ] 策略1是否匹配？
- [ ] 策略2是否匹配？
- [ ] 策略3是否匹配？
- [ ] 如果都没匹配，原因是什么？

### ✅ JSON结构分析
- [ ] JSON是否包含&#字符？
- [ ] JSON是否包含text字段？
- [ ] text字段的实际格式是什么？
- [ ] 是否有字符编码问题？

### ✅ 结果验证
- [ ] JSON处理输入和输出是否不同？
- [ ] 处理后的JSON是否包含&r？
- [ ] 最终的死亡消息是否正确显示？

---

## 🎯 **预期结果**

### **成功的调试**
这个增强调试版本应该能够：
1. **精确定位问题**：找出哪个正则表达式策略有效
2. **提供详细信息**：显示匹配成功的具体结果
3. **指导修复方向**：基于实际匹配情况制定解决方案

### **可能的发现**
- 某个策略成功匹配，但结果不正确
- 所有策略都失败，需要重新设计正则表达式
- JSON结构与我们的假设不同
- 存在字符编码或转义问题

---

## 🚀 **总结**

这个增强调试版本将帮助我们：

1. **精确诊断问题**
   - 逐个测试每个正则表达式策略
   - 显示匹配成功的具体结果
   - 识别失败的根本原因

2. **提供详细信息**
   - JSON结构的详细分析
   - 字符级别的调试信息
   - 匹配过程的完整记录

3. **指导下一步行动**
   - 基于实际匹配结果制定修复方案
   - 识别需要调整的正则表达式
   - 确定问题的根本原因

现在去测试这个增强调试版本！**关键是要观察每个策略的匹配结果**：

- 如果某个策略匹配成功，我们就知道正则表达式是正确的
- 如果所有策略都失败，我们就需要重新分析JSON结构
- 基于实际的匹配情况，我们可以制定精确的修复方案

这次应该能找出正则表达式为什么没有生效的真正原因！🔍💪
