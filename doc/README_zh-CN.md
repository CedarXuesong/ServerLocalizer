<div align="center">
  <img src="favicon.ico" alt="Logo" width="128" height="128">
</div>

# 🌐 ServerLocalizer

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9-brightgreen.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-11.15.1.2318-blue.svg)](https://files.minecraftforge.net)
[![Release](https://img.shields.io/github/v/release/CedarXuesong/ServerLocalizer?include_prereleases&color=orange)](https://github.com/CedarXuesong/ServerLocalizer/releases)
[![爱发电](https://img.shields.io/badge/%E7%88%B1%E5%8F%91%E7%94%B5-Sponsor%20me-pink.svg)](https://afdian.com/a/Cedaring)

> 🎮 一个强大的 Minecraft 服务器内容本地化AI翻译模组，让您轻松畅玩国际服务器！

## 📖 简介

ServerLocalizer 是一个专为 Minecraft 1.8.9 开发的 Forge 模组，通过实时拦截服务器数据包与调用AI大语言模型 API，为玩家提供高质量、可定制的游戏内容翻译，帮助您克服语言障碍，享受无缝的国际服游戏体验。

## ✨ 功能特点

### 核心翻译功能
- **物品翻译**: 自动翻译物品的名称（Name）和描述（Lore）。
- **聊天翻译**: 可通过点击聊天消息旁的 `[T]` 按钮，对任意聊天内容进行即时翻译。
- **样式保留**: 完整保留物品和聊天中的颜色代码与文本格式。

### 智能与效率
- **流式响应**: 开启后，翻译结果会逐字显示，无需等待，提供更流畅的交互体验。
- **智能缓存**: 自动缓存翻译结果，避免重复请求，节省资源。
- **并发处理**: 采用线程池处理多个翻译请求，确保游戏运行流畅。
- **动态内容识别**: 能够识别并保留文本中的动态部分（如玩家名、数值），仅翻译静态模板。

### 用户体验
- **可视化配置界面**: 提供现代化的GUI，方便用户调整所有配置。

## 📥 安装指南

1. **前置要求**:
   - Minecraft 1.8.9
   - Forge `11.15.1.2318` 或更高版本

2. **安装步骤**:
   1. 从 [Releases](https://github.com/CedarXuesong/ServerLocalizer/releases) 页面下载最新版本的 `ServerLocalizer-x.x.jar`。
   2. 将下载的 `.jar` 文件放入您的 `.minecraft/mods` 文件夹。
   3. 启动游戏即可使用。

## 🎮 使用教程

1. **打开配置**:
   - 在主菜单或暂停菜单点击 **"模组"** -> 找到 **"ServerLocalizer"** -> 点击 **"配置"** 按钮。
   - 或在游戏中输入命令 `/sl config`。
2. **配置API**:
   - 在 **"物品翻译"** 和 **"聊天翻译"** 标签页中，填入您的API服务地址（Base URL）、API 密钥（API Key）和模型名称（Model）。
   - 本模组兼容所有符合 OpenAI API 接口标准的模型服务。
3. **启用功能**:
   - 在对应的标签页中，打开您需要的功能开关，如"物品翻译"、"聊天翻译"等。
4. **保存配置**:
   - 点击左下角的 **"保存"** 按钮，所有设置将立即生效。
5. **开始翻译**:
   - **物品**: 将鼠标悬停在物品上即可看到翻译效果。
   - **聊天**: 点击聊天消息旁的 `[T]` 按钮即可开始翻译。

## ⌨️ 命令列表

主命令为 `/serverlocalizer`，可缩写为 `/sl`。

- `/sl config`: 打开模组的图形化配置界面。
- `/sl translate <messageId>`: 翻译指定ID的聊天消息（主要由内部点击调用）。

## 📂 配置文件

所有配置都保存在 `.minecraft/config/serverlocalizer/ModConfig.json` 文件中。您可以通过游戏内的配置界面进行修改，不建议手动编辑此文件。

翻译缓存则保存在 `.minecraft/config/serverlocalizer/language_packs/` 目录下的 `item.json` 文件中。

## 🔧 开发指南

### 环境配置
```bash
# 克隆项目
git clone https://github.com/CedarXuesong/ServerLocalizer.git

# 设置开发环境
./gradlew setupDecompWorkspace

# 生成IDE配置
./gradlew eclipse    # Eclipse
./gradlew idea       # IntelliJ IDEA
```

## 🤝 参与贡献

我们非常欢迎各种形式的贡献！

- **提交 Bug 报告**: 通过 [GitHub Issues](https://github.com/CedarXuesong/ServerLocalizer/issues) 提交您发现的任何问题。
- **改进翻译**: Fork 仓库并优化翻译缓存文件。
- **添加新功能**: 欢迎通过 Pull Request 贡献您的代码。

## 📞 联系方式

- **作者**: CedarXuesong
- **Bilibili**: [雪松CedarXuesong](https://space.bilibili.com/473773611)
- **问题反馈**: [GitHub Issues](https://github.com/CedarXuesong/ServerLocalizer/issues)

## 🙏 致谢

- [Minecraft Forge](https://files.minecraftforge.net/) - 强大的模组加载器。
- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) - 灵活的代码注入框架。
- 所有为本项目做出贡献的开发者和提供反馈的用户 ❤️。
