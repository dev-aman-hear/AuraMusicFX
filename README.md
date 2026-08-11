# Aura Music v3.1.0

A modern glassmorphism music player built with JavaFX. Version 3.1.0 introduces full System Media Transport Controls (SMTC) integration via the `jmtc` library, enhanced cross-platform media session handling, optimized decoders, and refined lyrics & artwork management.

## Features

* 🎵 **Broad Format Support**: Playback for MP3, WAV, M4A, FLAC, ALAC, and M4P formats.
* ⚡ **Optimized Core**: Reduced application size with highly optimized memory (RAM) and CPU footprint thanks to custom integrated decoders.
* 🪟 **Native System Media Transport Controls (SMTC)**: Native integration via `jmtc` supporting Windows 10 & 11 Media Transport Controls (Volume Flyout), keyboard media keys, system notifications, and Linux DBus session control.
* 🎨 **Dynamic Album Artwork**: Seamless metadata and high-res cover art extraction.
* 📝 **Synchronized Lyrics**: Built-in support for `.lrc` lyrics files with custom rendering.
* 📂 **Smart Resume**: Automatic folder and playback position restoration on launch.
* 🔀 **Playback Controls**: Shuffle mode, repeat modes, and robust queue management.
* ⌨ **Keyboard Shortcuts**: Full keyboard accessibility for media playback.
* ✨ **Stunning Aesthetics**: Modern transparent, glassmorphism UI with smooth micro-animations.

## Screenshots

<img width="420" height="720" alt="1" src="https://github.com/user-attachments/assets/b84207a6-5f08-4247-8d29-aea16be97efb" />
<img width="420" height="720" alt="3" src="https://github.com/user-attachments/assets/85cea05b-d96f-41db-bcc1-af4867f97500" />
<img width="420" height="720" alt="4" src="https://github.com/user-attachments/assets/fa333a80-2038-447d-a8f8-54c4df69cf42" />
<img width="325" height="538" alt="2" src="https://github.com/user-attachments/assets/3c1e3cb0-47d2-48b0-8dd5-af99bf29ad41" />

## Build

```bash
.\mvnw.cmd clean package

mkdir jpackage-input
cp target\AuraMusicFX-3.1.0.jar jpackage-input\

jpackage --type app-image --name AuraMusicFX --input jpackage-input --main-jar AuraMusicFX-3.1.0.jar --main-class dev.aman.auramusicfx.Launcher --icon src\main\resources\icons\AuraMusicFX_Ultimate.ico --dest dist

jpackage --type msi --name AuraMusicFX --app-image dist\AuraMusicFX --app-version 3.1.0 --vendor "AuraMusic" --win-menu --win-shortcut --dest installer

jpackage --type exe --name AuraMusicFX --app-image dist\AuraMusicFX --app-version 3.1.0 --vendor "AuraMusic" --win-menu --win-shortcut --dest installer
```

## Run Locally

```bash
java -jar target/AuraMusicFX-3.1.0.jar
```
*Note: Ensure your Java environment supports JavaFX 21 or use the Maven `javafx:run` command to test changes locally.*

## Installer

Windows MSI and EXE installers are available in the `installer` directory or the GitHub Releases page.

## Author

Aman
