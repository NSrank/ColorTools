#!/bin/bash

echo "==================================="
echo "ColorTools 插件构建脚本"
echo "==================================="

echo
echo "正在清理项目..."
mvn clean -q
if [ $? -ne 0 ]; then
    echo "清理失败！"
    exit 1
fi

echo
echo "正在编译项目..."
mvn compile -q
if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

echo
echo "正在打包项目..."
mvn package -q
if [ $? -ne 0 ]; then
    echo "打包失败！"
    exit 1
fi

echo
echo "==================================="
echo "构建完成！"
echo "==================================="
echo "生成的插件文件: target/ColorTools-1.0-SNAPSHOT.jar"
echo
echo "安装说明:"
echo "1. 确保服务器运行 Paper 1.20.1+"
echo "2. 将 ColorTools-1.0-SNAPSHOT.jar 放入 plugins 目录"
echo "3. 重启服务器"
echo "4. 使用 /colortools help 查看命令帮助"
echo "5. 游戏内消息将自动支持 MineDown 格式！"
echo "==================================="
