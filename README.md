<div align="center">
  <img src="doc/favicon.ico" alt="Logo" width="128" height="128">
</div>

# 🌐 ServerLocalizer

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9-brightgreen.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-11.15.1.2318-blue.svg)](https://files.minecraftforge.net)
[![Release](https://img.shields.io/github/v/release/CedarXuesong/ServerLocalizer?include_prereleases&color=orange)](https://github.com/CedarXuesong/ServerLocalizer/releases)
[![Sponsor](https://img.shields.io/badge/Sponsor-on%20Afdian-pink.svg)](https://afdian.com/a/Cedaring)
[![中文文档 (Chinese Readme)](https://img.shields.io/badge/文档-中文-blue.svg)](./doc/README_zh-CN.md)

> 🎮 A powerful Minecraft server content localization AI translation mod, allowing you to easily enjoy international servers!

## 📖 Introduction

ServerLocalizer is a Forge mod for Minecraft 1.8.9 that provides high-quality, customizable game content translation by intercepting server packets and calling AI large language model APIs. It helps you overcome language barriers and enjoy a seamless gaming experience on international servers.

## ✨ Features

### Core Translation Functions
- **Item Translation**: Automatically translates item names and lores.
- **Chat Translation**: Instantly translates any chat message by clicking the `[T]` button next to it.
- **Style Preservation**: Fully retains color codes and text formatting in both items and chat.

### Intelligence & Efficiency
- **Stream Response**: When enabled, translation results are displayed word by word, providing a smoother interactive experience without waiting.
- **Smart Caching**: Automatically caches translation results to avoid redundant requests and save resources.
- **Concurrent Processing**: Uses a thread pool to handle multiple translation requests, ensuring smooth game performance.

### User Experience
- **Visual Configuration GUI**: Provides a modern GUI for users to easily adjust all settings.

## 📥 Installation Guide

1. **Prerequisites**:
   - Minecraft 1.8.9
   - Forge `11.15.1.2318` or a later version

2. **Installation Steps**:
   1. Download the latest `ServerLocalizer-x.x.jar` from the [Releases](https://github.com/CedarXuesong/ServerLocalizer/releases) page.
   2. Place the downloaded `.jar` file into your `.minecraft/mods` folder.
   3. Launch the game to start using the mod.

## 🎮 How to Use

1. **Open Configuration**:
   - From the main menu or pause menu, click **"Mods"** -> find **"ServerLocalizer"** -> click the **"Config"** button.
   - Alternatively, use the in-game command `/sl config`.
2. **Configure API**:
   - In the **"Item Translation"** and **"Chat Translation"** tabs, enter your API service address (Base URL), API Key, and Model name.
   - This mod is compatible with any model service that adheres to the OpenAI API standards.
3. **Enable Features**:
   - In the respective tabs, enable the features you need, such as "Item Translation" or "Chat Translation".
4. **Save Configuration**:
   - Click the **"Save"** button at the bottom left. All settings will take effect immediately.
5. **Start Translating**:
   - **Items**: Hover over an item to see the translation.
   - **Chat**: Click the `[T]` button next to a chat message to start the translation.

## ⌨️ Command List

The main command is `/serverlocalizer`, which can be shortened to `/sl`.

- `/sl config`: Opens the mod's graphical configuration interface.
- `/sl translate <messageId>`: Translates a chat message with a specific ID (mainly used internally by clicks).

## 📂 Configuration Files

All configurations are stored in the `.minecraft/config/serverlocalizer/ModConfig.json` file. It is recommended to modify settings through the in-game GUI rather than editing this file manually.

## 🔧 Development Guide

### Setup Environment
```bash
# Clone the project
git clone https://github.com/CedarXuesong/ServerLocalizer.git

# Setup the development workspace
./gradlew setupDecompWorkspace

# Generate IDE configurations
./gradlew eclipse    # For Eclipse
./gradlew idea       # For IntelliJ IDEA
```

## 🤝 Contributing

We welcome all forms of contributions!

- **Submit Bug Reports**: Report any issues you find via [GitHub Issues](https://github.com/CedarXuesong/ServerLocalizer/issues).
- **Improve Translations**: Fork the repository and optimize the translation cache files.
- **Add New Features**: Feel free to contribute your code via Pull Requests.

## 📞 Contact

- **Author**: CedarXuesong
- **Bilibili**: [雪松CedarXuesong](https://space.bilibili.com/473773611)
- **Feedback & Issues**: [GitHub Issues](https://github.com/CedarXuesong/ServerLocalizer/issues)

## 🙏 Acknowledgements

- [Minecraft Forge](https://files.minecraftforge.net/) - The powerful mod loader.
- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) - The flexible code injection framework.
- All developers and users who have contributed to this project and provided feedback ❤️. 