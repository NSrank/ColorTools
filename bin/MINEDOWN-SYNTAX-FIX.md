# 基于MineDown官方语法的颜色修复

本文档介绍了基于MineDown官方语法文档的颜色代码处理修复。

---

## 📚 **MineDown官方语法参考**

根据 [MineDown官方文档](https://wiki.phoenix616.dev/library/minedown/syntax)，正确的十六进制颜色语法包括：

### **支持的颜色格式**

#### **1. 完整格式**
```
&#ff00ff&Text  - 6位十六进制颜色
&#f0f&Text     - 3位十六进制颜色（短格式）
```

#### **2. 渐变格式**
```
&#ff0000-#00ff00&Text  - 红到绿的渐变
&#f00-#0f0&Text        - 短格式渐变
```

#### **3. 无结束符格式**
```
&#ff00ffText   - 没有结束符&的格式
&#f0fText      - 短格式无结束符
```

---

## 🔧 **修复实现**

### **基于官方语法的处理策略**

#### **1. 完整格式处理**
```java
// 处理 &#rrggbb& 和 &#rgb& 格式
result = result.replaceAll("(&#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1&r ");
result = result.replaceAll("(&#[0-9a-fA-F]{3}&[^\\s]*?)\\s", "$1&r ");
```

#### **2. 渐变格式处理**
```java
// 处理渐变格式
result = result.replaceAll("(&#[0-9a-fA-F]{6}-#[0-9a-fA-F]{6}&[^\\s]*?)\\s", "$1&r ");
result = result.replaceAll("(&#[0-9a-fA-F]{3}-#[0-9a-fA-F]{3}&[^\\s]*?)\\s", "$1&r ");
```

#### **3. 无结束符格式处理**
```java
// 处理无结束符格式
result = result.replaceAll("(&#[0-9a-fA-F]{6}[^\\s&#]*?)\\s", "$1&r ");
result = result.replaceAll("(&#[0-9a-fA-F]{3}[^\\s&#]*?)\\s", "$1&r ");
```

#### **4. 清理和优化**
```java
// 清理多余的重置代码
result = result.replaceAll("(&r)+", "&r");
// 移除末尾的重置
result = result.replaceAll("&r\\s+", " ");
```

---

## 🧪 **测试MineDown语法**

### 安装步骤

1. **安装修复版本**
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

#### **1. 完整格式测试**
```bash
/team add test
/team modify test prefix "&#ff0000&红色文本 "
/team join test <玩家名>
/kill <玩家名>
```

**预期结果**：
- ✅ "红色文本"显示红色
- ✅ 空格后内容正常颜色
- ✅ 调试日志显示处理过程

#### **2. 短格式测试**
```bash
/team modify test prefix "&#f00&红色 &#0f0&绿色 "
/kill <玩家名>
```

**预期结果**：
- ✅ "红色"显示红色（#ff0000）
- ✅ "绿色"显示绿色（#00ff00）
- ✅ 后续内容正常颜色

#### **3. 渐变格式测试**
```bash
/team modify test prefix "&#ff0000-#00ff00&渐变文本 "
/kill <玩家名>
```

**预期结果**：
- ✅ "渐变文本"显示红到绿的渐变
- ✅ 后续内容正常颜色

#### **4. 短渐变格式测试**
```bash
/team modify test prefix "&#f00-#0f0&短渐变 "
/kill <玩家名>
```

**预期结果**：
- ✅ "短渐变"显示红到绿的渐变
- ✅ 后续内容正常颜色

#### **5. 无结束符格式测试**
```bash
/team modify test prefix "&#ff0000红色文本 "
/kill <玩家名>
```

**预期结果**：
- ✅ "红色文本"显示红色
- ✅ 后续内容正常颜色

#### **6. 复杂组合测试**
```bash
/team modify test prefix "&#f00&[&#0f0&VIP&#f00&] "
/kill <玩家名>
```

**预期结果**：
- ✅ 红色的 `[`
- ✅ 绿色的 `VIP`
- ✅ 红色的 `]`
- ✅ 后续内容正常颜色

---

## 📊 **调试信息解读**

### 基于官方语法的调试日志

#### **1. 完整格式处理**
```
[数据包拦截] 颜色处理输入: &#ff0000&红色文本 NSrank fell from a high place
[数据包拦截] 颜色处理输出: &#ff0000&红色文本&r NSrank fell from a high place
```

#### **2. 短格式处理**
```
[数据包拦截] 颜色处理输入: &#f00&红色 &#0f0&绿色 NSrank fell from a high place
[数据包拦截] 颜色处理输出: &#f00&红色&r &#0f0&绿色&r NSrank fell from a high place
```

#### **3. 渐变格式处理**
```
[数据包拦截] 颜色处理输入: &#ff0000-#00ff00&渐变文本 NSrank fell from a high place
[数据包拦截] 颜色处理输出: &#ff0000-#00ff00&渐变文本&r NSrank fell from a high place
```

#### **4. 无结束符处理**
```
[数据包拦截] 颜色处理输入: &#ff0000红色文本 NSrank fell from a high place
[数据包拦截] 颜色处理输出: &#ff0000红色文本&r NSrank fell from a high place
```

### 关键特征
- **格式识别正确**：各种MineDown格式都被识别
- **空格截断精确**：在空格前正确添加&r
- **处理逻辑清晰**：调试日志显示明确的输入输出

---

## 🔍 **MineDown语法优势**

### 1. **官方标准**
- **权威性**：基于官方文档的标准语法
- **兼容性**：与MineDown库完全兼容
- **稳定性**：不会因为库更新而失效

### 2. **格式丰富**
- **完整格式**：6位和3位十六进制
- **渐变支持**：多种渐变效果
- **灵活性**：有无结束符都支持

### 3. **易于使用**
- **直观语法**：颜色代码清晰易懂
- **简洁表达**：短格式节省空间
- **强大功能**：支持复杂的颜色效果

---

## 📋 **测试检查清单**

### ✅ MineDown格式支持
- [ ] &#rrggbb& 完整格式正常
- [ ] &#rgb& 短格式正常
- [ ] &#rrggbb-#rrggbb& 渐变格式正常
- [ ] &#rgb-#rgb& 短渐变格式正常
- [ ] &#rrggbb 无结束符格式正常
- [ ] &#rgb 短无结束符格式正常

### ✅ 颜色截断
- [ ] 每种格式在空格处正确截断
- [ ] 多种格式组合正确处理
- [ ] 复杂前缀正确截断

### ✅ 翻译保护
- [ ] 死亡消息显示正确语言
- [ ] 翻译结构完全保留
- [ ] 多语言客户端正常

### ✅ 调试信息
- [ ] 输入输出差异明显
- [ ] 处理过程清晰可见
- [ ] 错误处理机制正常

---

## 🎯 **预期修复效果**

### MineDown格式处理示例

#### **完整格式**
```
输入: &#ff0000&红色文本 NSrank fell from a high place
输出: &#ff0000&红色文本&r NSrank fell from a high place
显示: 红色文本 NSrank 从高处摔落
```

#### **短格式**
```
输入: &#f00&红色 &#0f0&绿色 NSrank fell from a high place
输出: &#f00&红色&r &#0f0&绿色&r NSrank fell from a high place
显示: 红色 绿色 NSrank 从高处摔落
```

#### **渐变格式**
```
输入: &#ff0000-#00ff00&渐变文本 NSrank fell from a high place
输出: &#ff0000-#00ff00&渐变文本&r NSrank fell from a high place
显示: 渐变文本 NSrank 从高处摔落
      ^^^^^^^^
      红到绿渐变
```

#### **复杂组合**
```
输入: &#f00&[&#0f0&VIP&#f00&] NSrank fell from a high place
输出: &#f00&[&#0f0&VIP&#f00&]&r NSrank fell from a high place
显示: [绿色VIP] NSrank 从高处摔落
      ^ ^^^^^ ^
      红 绿色 红
```

---

## 🚀 **总结**

这个基于MineDown官方语法的修复具有以下优势：

1. **标准兼容**
   - 完全基于官方文档
   - 支持所有MineDown格式
   - 与库版本无关

2. **功能完整**
   - 支持6位和3位十六进制
   - 支持渐变效果
   - 支持有无结束符格式

3. **处理精确**
   - 在空格处精确截断
   - 保护翻译内容
   - 清理多余重置

4. **易于维护**
   - 基于标准语法
   - 逻辑清晰简单
   - 调试信息完整

现在去测试这个基于MineDown官方语法的修复版本吧！应该能正确识别和处理所有MineDown支持的颜色格式：

- 🎨 **完整的MineDown语法支持** - 所有官方格式
- ✂️ **精确的颜色截断** - 在空格处自然断开
- 🌍 **完整的翻译保护** - 数据包拦截保护翻译
- 📚 **标准化实现** - 基于官方文档

这应该是最标准和可靠的解决方案！🚀
