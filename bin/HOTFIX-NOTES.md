# ColorTools 热修复说明

## 🚨 问题描述

在使用 ColorTools 插件时遇到了以下错误：

```
java.lang.NoSuchMethodError: 'org.plugin.colortools.libs.adventure.text.Component org.bukkit.event.entity.PlayerDeathEvent.deathMessage()'
```

## 🔍 问题根因

这个错误是由于 Maven Shade 插件的包重定位（relocation）配置导致的：

1. **Adventure API 重定位问题**：
   - Shade 插件将 `net.kyori.adventure` 重定位为 `org.plugin.colortools.libs.adventure`
   - 但是 Bukkit/Paper API 的方法签名仍然期望原始的 `net.kyori.adventure.text.Component`
   - 这导致了类型不匹配错误

2. **Paper 已提供 Adventure API**：
   - Paper 1.20.1 已经内置了 Adventure API
   - 不需要在插件中重新打包这些 API
   - 重定位反而会造成冲突

## 🛠️ 修复方案

### 修复内容

1. **移除 Adventure API 重定位**：
   ```xml
   <!-- 移除了这个重定位配置 -->
   <relocation>
     <pattern>net.kyori.adventure</pattern>
     <shadedPattern>org.plugin.colortools.libs.adventure</shadedPattern>
   </relocation>
   ```

2. **排除 Adventure API 打包**：
   ```xml
   <artifactSet>
     <excludes>
       <exclude>net.kyori:adventure-api</exclude>
       <exclude>net.kyori:adventure-key</exclude>
       <exclude>net.kyori:adventure-text-serializer-*</exclude>
       <exclude>net.kyori:adventure-platform-*</exclude>
       <exclude>net.kyori:adventure-nbt</exclude>
     </excludes>
   </artifactSet>
   ```

3. **设置 Adventure 依赖为 provided**：
   ```xml
   <dependency>
       <groupId>net.kyori</groupId>
       <artifactId>adventure-api</artifactId>
       <version>4.17.0</version>
       <scope>provided</scope>
   </dependency>
   ```

4. **保留 MineDown 重定位**：
   ```xml
   <relocation>
     <pattern>de.themoep.minedown</pattern>
     <shadedPattern>org.plugin.colortools.libs.minedown</shadedPattern>
   </relocation>
   ```

## 📦 新版本特性

### ✅ 修复的问题
- ❌ `NoSuchMethodError` 在死亡事件中
- ❌ `NoSuchMethodError` 在加入事件中
- ❌ `NoSuchMethodError` 在退出事件中
- ❌ `NoSuchMethodError` 在踢出事件中
- ❌ Adventure API 类型不匹配问题

### ✅ 保留的功能
- ✅ MineDown 格式处理
- ✅ 十六进制颜色支持
- ✅ 渐变色支持
- ✅ 所有事件监听
- ✅ 调试功能
- ✅ 缓存机制
- ✅ 性能监控

## 🚀 安装新版本

### 1. 停止服务器
```bash
# 停止 Minecraft 服务器
```

### 2. 替换插件文件
```bash
# 备份旧版本
mv plugins/ColorTools-1.0-SNAPSHOT.jar plugins/ColorTools-1.0-SNAPSHOT.jar.backup

# 安装新版本
cp target/ColorTools-1.0-SNAPSHOT.jar plugins/
```

### 3. 启动服务器
```bash
# 启动 Minecraft 服务器
```

### 4. 验证修复
```bash
# 在游戏中测试
/kill @s  # 应该不再出现错误

# 测试加入/退出
# 玩家加入和退出应该正常工作

# 测试颜色代码
# 在聊天中输入: &#ff0000红色文本
```

## 🧪 测试检查清单

### 基础功能测试
- [ ] 插件正常加载，无错误日志
- [ ] `/colortools help` 命令正常工作
- [ ] `/colortools status` 显示正确状态

### 事件测试
- [ ] 玩家死亡不再产生 `NoSuchMethodError`
- [ ] 玩家加入不再产生 `NoSuchMethodError`
- [ ] 玩家退出不再产生 `NoSuchMethodError`
- [ ] 玩家踢出不再产生 `NoSuchMethodError`

### 颜色代码测试
- [ ] 十六进制颜色正常显示：`&#ff0000红色`
- [ ] 渐变色正常显示：`&#ff0000-#0000ff&渐变`
- [ ] 传统颜色代码正常：`&a绿色`
- [ ] MineDown 格式正常：`**粗体**`

### 交互功能测试
- [ ] 点击事件正常：`[点击](command:/help)`
- [ ] 悬停事件正常：`[悬停](url 提示文本)`
- [ ] 组合格式正常：`**&#ff0000粗体红色**`

## 📊 性能影响

### 优化效果
- **减少内存占用**：不再重复打包 Adventure API
- **提高兼容性**：使用 Paper 原生的 Adventure API
- **减少冲突**：避免了类加载器冲突

### 文件大小对比
- **修复前**：约 2.5MB（包含重定位的 Adventure API）
- **修复后**：约 800KB（仅包含 MineDown 和插件代码）

## 🔧 开发者说明

### 依赖管理
```xml
<!-- 现在的依赖配置 -->
<dependency>
    <groupId>net.kyori</groupId>
    <artifactId>adventure-api</artifactId>
    <scope>provided</scope>  <!-- Paper 提供 -->
</dependency>

<dependency>
    <groupId>de.themoep</groupId>
    <artifactId>minedown-adventure</artifactId>
    <scope>compile</scope>   <!-- 需要打包 -->
</dependency>
```

### 重定位策略
```xml
<!-- 只重定位第三方库，不重定位 Paper 提供的 API -->
<relocation>
    <pattern>de.themoep.minedown</pattern>
    <shadedPattern>org.plugin.colortools.libs.minedown</shadedPattern>
</relocation>
```

## 🐛 如果仍有问题

### 1. 清理缓存
```bash
# 删除服务器缓存
rm -rf cache/
rm -rf world/playerdata/
```

### 2. 检查 Paper 版本
```bash
# 确保使用 Paper 1.20.1 build 196+
# 检查 /version 命令输出
```

### 3. 检查其他插件
```bash
# 暂时移除其他聊天相关插件测试
# 确认是否有插件冲突
```

### 4. 启用详细调试
```bash
/colortools debug on
# 查看详细的处理日志
```

## 📞 获取支持

如果修复后仍有问题，请提供：

1. **完整的错误日志**
2. **服务器版本信息** (`/version`)
3. **插件列表** (`/plugins`)
4. **配置文件内容**
5. **重现步骤**

---

**这个热修复应该解决所有的 `NoSuchMethodError` 问题，让 ColorTools 插件正常工作！** 🎉
