# ColorTools 安装指南

本指南将帮助你快速安装和配置 ColorTools 插件。

---

## 📋 系统要求

### 必需条件
- **Minecraft 版本**：1.20.1 或更高
- **服务器核心**：Paper（强烈推荐）或 Spigot
- **Java 版本**：17 或更高

### 推荐配置
- **内存**：至少 2GB RAM
- **CPU**：双核或更高
- **存储**：至少 100MB 可用空间

---

## 🚀 快速安装

### 方法一：使用预构建版本

1. **下载插件**
   ```bash
   # 从 releases 页面下载最新版本
   # 或者使用构建脚本构建
   ```

2. **安装插件**
   ```bash
   # 将 ColorTools-1.0-SNAPSHOT.jar 复制到服务器的 plugins 目录
   cp ColorTools-1.0-SNAPSHOT.jar /path/to/server/plugins/
   ```

3. **重启服务器**
   ```bash
   # 重启 Minecraft 服务器
   # 插件将自动生成配置文件
   ```

### 方法二：从源码构建

1. **克隆项目**
   ```bash
   git clone https://github.com/your-repo/ColorTools.git
   cd ColorTools
   ```

2. **构建插件**
   ```bash
   # Windows
   build.bat
   
   # Linux/Mac
   chmod +x build.sh
   ./build.sh
   ```

3. **安装插件**
   ```bash
   # 复制生成的 jar 文件到服务器
   cp target/ColorTools-1.0-SNAPSHOT.jar /path/to/server/plugins/
   ```

---

## ⚙️ 配置设置

### 首次启动

插件首次启动时会自动生成以下文件：

```
plugins/ColorTools/
├── config.yml              # 主配置文件
├── messages_zh_CN.yml      # 中文语言文件
├── messages_en_US.yml      # 英文语言文件
└── colortools.log          # 日志文件（如果启用）
```

### 基础配置

编辑 `config.yml` 文件：

```yaml
# 基本设置
settings:
  enabled: true              # 启用插件
  debug: false              # 调试模式（生产环境建议关闭）
  language: zh_CN           # 语言设置
  verbose-logging: false    # 详细日志

# MineDown 处理设置
minedown:
  enabled: true                    # 启用 MineDown 处理
  process-legacy-colors: true      # 处理传统颜色代码
  process-hex-colors: true         # 处理十六进制颜色
  process-gradients: true          # 处理渐变色
  process-rainbow: true            # 处理彩虹色
  process-formatting: true         # 处理格式化
  process-events: true             # 处理点击和悬停事件
```

### 事件配置

```yaml
# 事件处理设置
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
  
  quit:
    enabled: true                  # 启用退出事件处理
    process-quit-messages: true    # 处理退出消息
    priority: NORMAL
  
  death:
    enabled: true                  # 启用死亡事件处理
    process-death-messages: true   # 处理死亡消息
    priority: NORMAL
```

---

## 🔧 权限配置

### 权限节点

在你的权限插件中添加以下权限：

```yaml
permissions:
  # 管理员权限
  colortools.admin:
    description: ColorTools 管理员权限
    default: op
  
  # 重载配置权限
  colortools.reload:
    description: 重载配置权限
    default: op
  
  # 测试格式权限
  colortools.test:
    description: 测试 MineDown 格式权限
    default: true
```

### LuckPerms 示例

```bash
# 给予管理员完整权限
/lp group admin permission set colortools.* true

# 给予普通玩家测试权限
/lp group default permission set colortools.test true

# 给予特定玩家重载权限
/lp user <玩家名> permission set colortools.reload true
```

---

## 🔍 验证安装

### 检查插件状态

1. **查看插件列表**
   ```
   /plugins
   ```
   应该看到 ColorTools 在列表中且为绿色

2. **检查插件状态**
   ```
   /colortools status
   ```
   显示插件详细状态信息

3. **测试格式化**
   ```
   /colortools test
   ```
   显示 MineDown 格式示例

### 测试功能

1. **聊天测试**
   ```
   在聊天中输入: **粗体文本** 和 &a绿色文本
   ```

2. **命令测试**
   ```
   /say **&c重要公告**：服务器将在 &e10分钟 &c后重启
   ```

3. **交互测试**
   ```
   /colortools test [点击这里](command:/help 查看帮助)
   ```

---

## 🔧 兼容性配置

### 与其他插件的兼容

#### EssentialsX
```yaml
compatibility:
  plugins:
    essentialsx: true
```

#### HuskChat
```yaml
compatibility:
  plugins:
    huskchat: true
  process-after-other-plugins: true
```

#### PlaceholderAPI
```yaml
compatibility:
  plugins:
    placeholderapi: true
```

### 事件优先级调整

如果与其他聊天插件冲突，调整事件优先级：

```yaml
events:
  chat:
    priority: HIGH    # 在其他插件之后处理
  join:
    priority: HIGH
  quit:
    priority: HIGH
  death:
    priority: HIGH
```

---

## 📊 性能优化

### 缓存设置

```yaml
performance:
  async-processing: true         # 启用异步处理
  cache-size: 1000              # 缓存大小（根据服务器内存调整）
  cache-expire-time: 300        # 缓存过期时间
  enable-preprocessing: true     # 启用预处理
```

### 内存优化

```yaml
# 对于小型服务器
performance:
  cache-size: 500
  cache-expire-time: 180

# 对于大型服务器
performance:
  cache-size: 2000
  cache-expire-time: 600
```

---

## 🐛 故障排除

### 常见问题

#### 1. 插件无法加载
```bash
# 检查 Java 版本
java -version

# 检查服务器核心
# 确保使用 Paper 1.20.1+

# 查看控制台错误日志
tail -f logs/latest.log
```

#### 2. 格式化不生效
```bash
# 检查插件状态
/colortools status

# 启用调试模式
/colortools debug on

# 测试格式化
/colortools test **测试文本**
```

#### 3. 与其他插件冲突
```bash
# 查看兼容性检测
/colortools status

# 调整事件优先级
# 编辑 config.yml 中的 events.*.priority
```

### 调试步骤

1. **启用调试模式**
   ```
   /colortools debug on
   ```

2. **查看详细日志**
   ```
   tail -f plugins/ColorTools/colortools.log
   ```

3. **测试特定功能**
   ```
   /colortools test <你的测试消息>
   ```

4. **检查缓存状态**
   ```
   /colortools cache stats
   ```

---

## 📈 监控和维护

### 性能监控

```bash
# 查看插件状态
/colortools status

# 查看缓存统计
/colortools cache stats

# 清理缓存（如果需要）
/colortools cache clear
```

### 定期维护

1. **定期重载配置**
   ```
   /colortools reload
   ```

2. **监控日志文件大小**
   ```bash
   ls -lh plugins/ColorTools/colortools.log
   ```

3. **备份配置文件**
   ```bash
   cp -r plugins/ColorTools/ backups/ColorTools-$(date +%Y%m%d)/
   ```

---

## 🎉 安装完成

恭喜！ColorTools 插件已成功安装。现在你的服务器支持：

- ✅ 自动 MineDown 格式处理
- ✅ 丰富的文本格式化
- ✅ 交互式消息元素
- ✅ 智能插件兼容性
- ✅ 高性能消息处理

开始享受更加丰富多彩的 Minecraft 服务器体验吧！

---

## 📞 获取帮助

如果遇到问题，可以：

1. 查看 [README.md](README.md) 了解详细功能
2. 查看 [EXAMPLES.md](EXAMPLES.md) 学习使用示例
3. 在 GitHub 上提交 Issue
4. 加入我们的社区讨论

**祝你使用愉快！** 🎮
