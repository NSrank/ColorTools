# ColorTools 调试指南

本指南将帮助你诊断和解决 ColorTools 插件的颜色处理问题。

---

## 🔍 **问题分析**

### 你遇到的问题
Team 系统设置的带有 RGB 十六进制颜色占位符显示为：
```
[{"text":"&#55B1C2&["},{"text":"&#8B0000&莫斯科"},{"text":"&#55B1C2&] "}]
```

### 可能的原因
1. **插件未被触发**：消息可能在我们的事件监听器之前就被处理了
2. **检测逻辑问题**：我们的格式检测可能有误
3. **MineDown 处理问题**：MineDown 库可能没有正确处理这种格式
4. **事件优先级问题**：其他插件可能在我们之前处理了消息

---

## 🛠️ **新版本改进**

### 1. **简化颜色处理**
- **移除不必要的转换**：MineDown 原生支持 `&#rrggbb&` 格式
- **直接使用 MineDown**：让 MineDown 库处理所有颜色格式
- **增强调试日志**：详细显示处理过程

### 2. **改进检测逻辑**
- **更宽松的检测**：检测任何包含 `&` 的文本
- **详细的调试输出**：显示每一步的处理过程

### 3. **新增测试命令**
- **`/colortools testcolor`**：专门测试颜色处理
- **详细的调试模式**：显示完整的处理流程

---

## 🧪 **调试步骤**

### 第一步：安装新版本

1. **停止服务器**
2. **替换插件文件**
   ```bash
   cp target/ColorTools-1.0-SNAPSHOT.jar plugins/
   ```
3. **启动服务器**

### 第二步：启用调试模式

```bash
/colortools debug on
```

### 第三步：测试基础功能

#### 1. 测试插件状态
```bash
/colortools status
```
**预期输出**：显示插件状态和配置信息

#### 2. 测试颜色命令
```bash
/colortools testcolor
```
**预期输出**：显示各种颜色格式的测试

#### 3. 测试自定义颜色
```bash
/colortools testcolor &#55B1C2&测试颜色
```
**预期输出**：显示原始文本和处理后的效果

### 第四步：测试 Team 系统

#### 1. 创建测试 Team
```bash
/team add debugteam
/team modify debugteam prefix "&#55B1C2&["
/team modify debugteam suffix "&#55B1C2&] "
/team join debugteam <你的用户名>
```

#### 2. 测试死亡消息
```bash
/kill <你的用户名>
```

#### 3. 观察调试日志
查看控制台输出，应该看到类似：
```
[调试] 从Component提取文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] <玩家名> fell from a high place
[调试] 检测MineDown格式: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] <玩家名> fell from a high place
[调试] MineDown格式检测结果: true
[调试] 开始处理Component文本
[调试] 预处理文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] <玩家名> fell from a high place -> &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] <玩家名> fell from a high place
[调试] 最终组件: <处理后的文本>
```

---

## 📊 **调试信息解读**

### 正常情况下的日志

#### 1. Component 提取成功
```
[调试] 从Component提取文本: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
```
**说明**：成功从 Component 中提取了包含颜色代码的文本

#### 2. 格式检测成功
```
[调试] 检测MineDown格式: &#55B1C2&[&#8B0000&莫斯科&#55B1C2&] NSrank fell from a high place
[调试] MineDown格式检测结果: true
```
**说明**：正确识别了需要处理的格式

#### 3. 处理开始
```
[调试] 开始处理Component文本
```
**说明**：开始进行 MineDown 处理

#### 4. 预处理完成
```
[调试] 预处理文本: <原始> -> <处理后>
```
**说明**：预处理步骤完成

#### 5. 最终结果
```
[调试] 最终组件: <最终的Component文本>
```
**说明**：MineDown 处理完成

### 异常情况的诊断

#### 1. 如果没有看到任何调试日志
**可能原因**：
- 插件未正确加载
- 调试模式未启用
- 事件未被触发

**解决方案**：
```bash
/plugins  # 检查插件是否加载
/colortools debug on  # 确保调试模式启用
/colortools status  # 检查插件状态
```

#### 2. 如果看到"无需处理，返回原始Component"
**可能原因**：
- 格式检测逻辑有问题
- 文本中没有被识别的格式

**解决方案**：
检查提取的文本是否正确，调整检测逻辑

#### 3. 如果处理过程中出现错误
**可能原因**：
- MineDown 库处理失败
- 文本格式不正确

**解决方案**：
查看完整的错误堆栈，检查 MineDown 格式

---

## 🔧 **故障排除**

### 问题 1：插件没有处理任何消息

#### 检查步骤
1. **确认插件加载**
   ```bash
   /plugins
   ```
   ColorTools 应该显示为绿色

2. **确认配置正确**
   ```bash
   /colortools status
   ```
   所有相关设置应该为启用状态

3. **确认权限正确**
   检查是否有必要的权限

#### 解决方案
- 重新加载插件：`/colortools reload`
- 检查配置文件：`plugins/ColorTools/config.yml`
- 检查权限配置

### 问题 2：颜色代码没有被处理

#### 检查步骤
1. **测试基础颜色**
   ```bash
   /colortools testcolor &#ff0000&红色
   ```

2. **检查 MineDown 处理**
   ```bash
   /colortools test **粗体**
   ```

3. **检查事件优先级**
   查看是否有其他插件冲突

#### 解决方案
- 调整事件优先级
- 禁用冲突的插件进行测试
- 检查 MineDown 库版本

### 问题 3：Team 消息特殊处理

#### 可能的解决方案

如果 Team 系统的消息仍然有问题，可能需要：

1. **监听更早的事件**
   - 监听 `PlayerDeathEvent` 的更早阶段
   - 使用 `LOWEST` 优先级

2. **直接处理 JSON 格式**
   - 解析 JSON 格式的 Component
   - 直接处理 Component 树结构

3. **使用 Paper 特定的 API**
   - 使用 Paper 的 Adventure API
   - 直接操作 Component 对象

---

## 📋 **测试检查清单**

### ✅ 基础功能测试
- [ ] 插件正确加载（绿色状态）
- [ ] 调试模式正常启用
- [ ] `/colortools testcolor` 正常工作
- [ ] 基础颜色代码正确显示

### ✅ Team 系统测试
- [ ] Team 前缀包含颜色代码
- [ ] 死亡消息触发事件处理
- [ ] 调试日志显示处理过程
- [ ] 最终颜色正确显示

### ✅ 调试信息测试
- [ ] Component 提取日志正常
- [ ] 格式检测日志正常
- [ ] 处理过程日志正常
- [ ] 最终结果日志正常

---

## 📞 **获取详细诊断信息**

如果问题仍然存在，请提供以下信息：

### 1. 服务器信息
```bash
/version
```

### 2. 插件状态
```bash
/colortools status
```

### 3. 调试日志
启用调试模式后，执行测试并提供完整的控制台输出

### 4. Team 配置
```bash
/team list
/team modify debugteam
```

### 5. 测试结果
```bash
/colortools testcolor &#55B1C2&测试
```

---

通过这个详细的调试过程，我们应该能够准确定位问题所在，并找到正确的解决方案！🔍
