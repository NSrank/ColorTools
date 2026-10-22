# ColorTools - Minecraft MineDown 格式支持插件

![Java](https://img.shields.io/badge/Java-17-green) ![Paper](https://img.shields.io/badge/Paper-1.20.1+-green) ![License](https://img.shields.io/badge/License-MIT-green.svg)

**ColorTools** 是一个强大的 Minecraft 服务器插件，让游戏内的各种聊天信息和系统消息自动支持 [MineDown](https://github.com/Phoenix616/MineDown) 格式，无需额外配置即可享受丰富的文本格式化功能。

---

## 🎯 核心特性

### ✨ **自动格式化支持**
- **聊天消息**：玩家聊天自动支持 MineDown 格式
- **系统消息**：加入/退出、死亡、踢出消息自动格式化
- **服务器消息**：广播消息、MOTD 自动支持格式化
- **命令消息**：/say、/tell、/broadcast 等命令自动格式化

### 🎨 **丰富的格式化选项**
- **传统颜色代码**：`&a`、`&c`、`&e` 等
- **十六进制颜色**：`&#ff0000`、`&#00ff00` 等
- **文本格式**：`**粗体**`、`##斜体##`、`__下划线__`、`~~删除线~~`、`??混淆??`
- **渐变色**：`&#ff0000-#0000ff&渐变文本`
- **彩虹色**：`&rainbow&彩虹文本`
- **点击事件**：`[点击文本](command:/help)`
- **悬停事件**：`[悬停文本](https://example.com 悬停提示)`

### 🔧 **智能兼容性**
- **自动检测**：自动检测其他聊天插件并调整兼容性
- **优先级管理**：智能调整事件处理优先级避免冲突
- **插件集成**：支持 EssentialsX、HuskChat、PlaceholderAPI 等

### ⚡ **高性能设计**
- **异步处理**：支持异步消息处理，不影响服务器性能
- **智能缓存**：缓存处理结果，提高重复消息处理速度
- **性能监控**：内置性能监控，实时了解插件运行状态

---

## 🚀 快速开始

### 系统要求
- **Minecraft 版本**：1.20.1+
- **服务器核心**：Paper（推荐）或 Spigot
- **Java 版本**：17+

### 安装步骤

#### 1. 下载插件
```bash
# 克隆项目
git clone https://github.com/your-repo/ColorTools.git
cd ColorTools

# 构建插件
# Windows
build.bat
# Linux/Mac
chmod +x build.sh && ./build.sh
```

#### 2. 安装到服务器
1. 将 `target/ColorTools-1.0-SNAPSHOT.jar` 复制到服务器的 `plugins/` 目录
2. 重启服务器
3. 插件将自动生成配置文件

#### 3. 验证安装
```
/colortools help    # 查看命令帮助
/colortools status  # 查看插件状态
/colortools test    # 测试 MineDown 格式
```

---

## 📋 命令使用

### 主命令：`/colortools` (别名: `/ct`, `/colors`)

#### 基础命令
```bash
/colortools help           # 显示帮助信息
/colortools status         # 显示插件状态和统计信息
/colortools reload         # 重新加载配置文件
/colortools toggle         # 切换插件启用/禁用状态
```

#### 测试命令
```bash
/colortools test           # 显示预设的格式化示例
/colortools test <消息>    # 测试自定义消息的格式化效果
```

#### 管理命令
```bash
/colortools cache clear    # 清理消息处理缓存
/colortools cache stats    # 显示缓存统计信息
/colortools debug on/off   # 启用/禁用调试模式
```

---

## 🎨 MineDown 格式示例

### 基础格式化
```
**粗体文本**                    # 粗体
##斜体文本##                    # 斜体
__下划线文本__                  # 下划线
~~删除线文本~~                  # 删除线
??混淆文本??                    # 混淆效果
```

### 颜色支持
```
&a绿色文本                      # 传统颜色代码
&#ff0000红色文本                # 十六进制颜色
&#ff0000-#0000ff&渐变文本       # 渐变色
&rainbow&彩虹文本               # 彩虹色
```

### 交互元素
```
[点击执行命令](command:/help)                    # 点击执行命令
[点击访问网站](https://example.com)              # 点击打开链接
[悬停显示提示](https://example.com 这是提示文本)  # 悬停显示提示
```

### 组合使用
```
**&a粗体绿色文本**
[**&c点击这里**](command:/spawn &7点击传送到出生点)
&#ff0000-#ffff00&**渐变粗体文本**
```

---

## ⚙️ 配置文件

### 主配置文件 (`config.yml`)

#### 基本设置
```yaml
settings:
  enabled: true              # 是否启用插件
  debug: false              # 调试模式
  language: zh_CN           # 语言设置
  verbose-logging: false    # 详细日志
```

#### MineDown 处理设置
```yaml
minedown:
  enabled: true                    # 启用 MineDown 处理
  process-legacy-colors: true      # 处理传统颜色代码
  process-hex-colors: true         # 处理十六进制颜色
  process-gradients: true          # 处理渐变色
  process-rainbow: true            # 处理彩虹色
  process-formatting: true         # 处理格式化代码
  process-events: true             # 处理点击和悬停事件
```

#### 事件处理设置
```yaml
events:
  chat:
    enabled: true                  # 启用聊天事件处理
    process-player-chat: true      # 处理玩家聊天
    process-system-messages: true  # 处理系统消息
    priority: NORMAL              # 事件优先级
  
  join:
    enabled: true                  # 启用加入事件处理
    process-join-messages: true    # 处理加入消息
    priority: NORMAL
  
  # ... 其他事件设置
```

#### 性能设置
```yaml
performance:
  async-processing: true         # 异步处理
  cache-size: 1000              # 缓存大小
  cache-expire-time: 300        # 缓存过期时间（秒）
  enable-preprocessing: true     # 启用预处理
```

#### 兼容性设置
```yaml
compatibility:
  auto-detect-chat-plugins: true           # 自动检测聊天插件
  process-after-other-plugins: true        # 在其他插件之后处理
  plugins:
    essentialsx: true                      # EssentialsX 兼容
    huskchat: true                         # HuskChat 兼容
    placeholderapi: true                   # PlaceholderAPI 兼容
```

---

## 🔧 权限节点

```yaml
permissions:
  colortools.*:
    description: ColorTools 所有权限
    default: op
    children:
      colortools.admin: true
      colortools.reload: true
      colortools.test: true
      
  colortools.admin:
    description: 管理员权限
    default: op
    
  colortools.reload:
    description: 重载配置权限
    default: op
    
  colortools.test:
    description: 测试格式权限
    default: true
```

---

## 🔌 插件兼容性

### 完全兼容
- ✅ **EssentialsX** - 自动检测并兼容
- ✅ **HuskChat** - 支持跨服聊天格式化
- ✅ **PlaceholderAPI** - 支持占位符处理
- ✅ **LuckPerms** - 权限系统兼容
- ✅ **Vault** - 经济和权限兼容

### 部分兼容
- ⚠️ **ChatManager** - 需要调整事件优先级
- ⚠️ **VentureChat** - 建议禁用其格式化功能
- ⚠️ **DeluxeChat** - 可能需要手动配置

### 可能冲突
- ❌ **ColoredSigns** - 可能冲突，建议选择其一
- ❌ **ChatColor** - 功能重叠，建议选择其一

---

## 📊 性能监控

### 内置统计
- **处理消息数量**：实时统计处理的消息数量
- **平均处理时间**：监控消息处理性能
- **缓存命中率**：优化缓存使用效率
- **错误统计**：跟踪处理错误

### 查看统计
```bash
/colortools status         # 基本状态信息
/colortools cache stats    # 详细缓存统计
```

### 性能优化建议
1. **启用缓存**：保持缓存启用以提高性能
2. **异步处理**：启用异步处理避免阻塞主线程
3. **合理配置**：根据服务器负载调整缓存大小
4. **定期清理**：定期清理缓存释放内存

---

## 🐛 故障排除

### 常见问题

#### 1. 插件无法加载
- 检查 Java 版本是否为 17+
- 确认服务器核心为 Paper 1.20.1+
- 查看控制台错误日志

#### 2. 格式化不生效
- 确认插件已启用：`/colortools status`
- 检查 MineDown 处理是否启用
- 验证消息格式是否正确

#### 3. 与其他插件冲突
- 查看兼容性检测结果
- 调整事件处理优先级
- 禁用冲突插件的格式化功能

#### 4. 性能问题
- 启用异步处理
- 调整缓存大小
- 检查性能监控数据

### 调试模式
```bash
/colortools debug on       # 启用调试模式
/colortools debug off      # 禁用调试模式
```

启用调试模式后，插件会输出详细的处理日志，帮助诊断问题。

---

## 🤝 贡献指南

### 开发环境
- Java 17+
- Maven 3.6+
- IntelliJ IDEA 或 Eclipse

### 构建项目
```bash
git clone https://github.com/your-repo/ColorTools.git
cd ColorTools
mvn clean package
```

### 提交贡献
1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request

---

## 📄 许可证

本项目基于 MIT 许可证开源。详见 [LICENSE](LICENSE) 文件。

---

## 🔗 相关链接

- [MineDown 格式文档](https://github.com/Phoenix616/MineDown)
- [Paper 服务器](https://papermc.io/)
- [Adventure API](https://docs.adventure.kyori.net/)
- [问题反馈](https://github.com/NSrank/ColorTools/issues)

---

**让你的 Minecraft 服务器消息更加丰富多彩！** 🎉
