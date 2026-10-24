# ColorTools 颜色处理测试指南

本指南专门用于测试 ColorTools 插件的十六进制颜色处理功能，特别是针对 Minecraft Team 系统和其他复杂场景。

---

## 🎯 **测试目标**

### 主要问题
你遇到的问题：Team 系统设置的带有 RGB 十六进制颜色占位符显示为：
```
[{"text":"&#55B1C2&["},{"text":"&#8B0000&莫斯科"},{"text":"&#55B1C2&] "}]
```

### 预期结果
应该显示为正确的颜色格式，而不是 JSON 文本。

---

## 🔧 **新版本改进**

### 1. **增强的Component处理**
- 新增 `processComponent()` 方法，直接处理 Adventure Component 对象
- 更好地处理复杂的 Component 结构
- 支持从 JSON 格式的 Component 中提取和处理颜色代码

### 2. **改进的十六进制颜色处理**
- 更精确的正则表达式匹配
- 支持多种格式：`&#rrggbb`、`&#rrggbb&`、`&#rrggbb-#rrggbb&`
- 正确的处理顺序（渐变色优先于单色）

### 3. **详细的调试日志**
- 显示原始 Component 内容
- 显示文本提取过程
- 显示颜色代码转换过程
- 显示最终处理结果

---

## 🧪 **测试步骤**

### 准备工作

1. **安装新版本插件**
   ```bash
   # 停止服务器
   # 替换插件文件
   cp target/ColorTools-1.0-SNAPSHOT.jar plugins/
   # 启动服务器
   ```

2. **启用详细调试**
   ```bash
   /colortools debug on
   ```

### 测试一：Team 系统颜色

#### 1. 创建带颜色的 Team
```bash
# 创建一个team
/team add testteam

# 设置team前缀（包含十六进制颜色）
/team modify testteam prefix "&#55B1C2&["

# 设置team后缀
/team modify testteam suffix "&#55B1C2&] "

# 设置team颜色
/team modify testteam color dark_red

# 将玩家加入team
/team join testteam <玩家名>
```

#### 2. 测试死亡消息
```bash
# 让玩家死亡，观察死亡消息
/kill <玩家名>
```

**预期结果**：
- 控制台显示详细的调试信息
- 死亡消息中的颜色代码被正确处理
- 不再显示 JSON 格式的文本

### 测试二：直接颜色代码测试

#### 1. 聊天消息测试
```bash
# 在聊天中输入各种格式
&#55B1C2&测试颜色1
&#8B0000&测试颜色2
&#55B1C2&[&#8B0000&玩家名&#55B1C2&]
```

#### 2. 命令消息测试
```bash
# 使用say命令
/say &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] 玩家消息

# 使用tellraw命令（如果支持）
/tellraw @a [{"text":"&#55B1C2&["},{"text":"&#8B0000&莫斯科"},{"text":"&#55B1C2&] 测试"}]
```

### 测试三：复杂格式组合

#### 1. 渐变色测试
```bash
# 渐变色格式
&#ff0000-#0000ff&渐变文本
&#55B1C2-#8B0000&蓝红渐变
```

#### 2. 格式化 + 颜色
```bash
**&#55B1C2&粗体蓝色**
##&#8B0000&斜体红色##
__&#55B1C2&下划线蓝色__
```

#### 3. 交互元素 + 颜色
```bash
[&#55B1C2&**点击链接**](https://example.com &#8B0000&悬停提示)
```

---

## 📊 **调试信息解读**

### 正常的调试日志应该显示：

#### 1. Component 提取
```
[调试] 从Component提取文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
```

#### 2. 颜色处理过程
```
[调试] 处理十六进制颜色前: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
[调试] 处理十六进制颜色后: <#55B1C2>[<#8B0000>莫斯科<#55B1C2>] NSrank fell from a high place
```

#### 3. 预处理结果
```
[调试] 预处理文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place -> <#55B1C2>[<#8B0000>莫斯科<#55B1C2>] NSrank fell from a high place
```

#### 4. 最终结果
```
[调试] 最终组件: §x§5§5§B§1§C§2[§x§8§B§0§0§0§0莫斯科§x§5§5§B§1§C§2] NSrank fell from a high place
```

---

## 🔍 **问题诊断**

### 如果颜色仍然不显示

#### 1. 检查调试日志
```bash
# 查看完整的调试输出
tail -f logs/latest.log | grep "调试"
```

#### 2. 检查 MineDown 处理
```bash
# 测试 MineDown 格式
/colortools test <#55B1C2>测试颜色
```

#### 3. 检查配置
```bash
# 确认配置正确
/colortools status
```

### 常见问题和解决方案

#### 1. **颜色代码格式问题**
```bash
# 确保使用正确的格式
正确：&#55B1C2&
错误：#55B1C2、&55B1C2、&#55B1C2（缺少&）
```

#### 2. **Team 系统兼容性**
```bash
# 检查 Paper 版本
/version

# 确保使用 Paper 1.20.1+
```

#### 3. **插件冲突**
```bash
# 暂时禁用其他聊天插件
# 测试 ColorTools 是否正常工作
```

---

## 📋 **测试检查清单**

### ✅ 基础功能
- [ ] 插件正常加载，无错误
- [ ] 调试模式正常工作
- [ ] `/colortools` 命令正常

### ✅ 颜色处理
- [ ] 十六进制颜色正确显示
- [ ] Team 前缀/后缀颜色正确
- [ ] 死亡消息颜色正确
- [ ] 聊天消息颜色正确

### ✅ 调试信息
- [ ] 显示 Component 提取信息
- [ ] 显示颜色处理过程
- [ ] 显示预处理结果
- [ ] 显示最终组件

### ✅ 高级功能
- [ ] 渐变色正常工作
- [ ] 格式化组合正常
- [ ] 交互元素正常

---

## 🎯 **特定测试用例**

### 针对你的问题

#### 测试用例 1：Team 死亡消息
```bash
# 设置
/team add colortest
/team modify colortest prefix "&#55B1C2&["
/team modify colortest suffix "&#55B1C2&] "
/team join colortest <玩家名>

# 测试
/kill <玩家名>

# 预期：颜色正确显示，不是 JSON 文本
```

#### 测试用例 2：复杂颜色组合
```bash
# 在聊天中输入
&#55B1C2&[&#8B0000&莫斯科&#55B1C2&] 测试消息

# 预期：蓝色括号，红色"莫斯科"，蓝色括号
```

#### 测试用例 3：渐变色
```bash
# 在聊天中输入
&#55B1C2-#8B0000&从蓝色到红色的渐变

# 预期：平滑的颜色渐变效果
```

---

## 📞 **获取支持**

如果测试后仍有问题，请提供：

1. **完整的调试日志**（启用调试模式后的输出）
2. **具体的测试步骤**和**实际结果**
3. **服务器版本信息**（`/version`）
4. **Team 配置信息**（`/team list`）
5. **其他相关插件列表**（`/plugins`）

---

通过这些详细的测试，我们应该能够确定 ColorTools 插件是否正确处理了十六进制颜色代码，特别是在 Team 系统和复杂消息格式中的表现。
