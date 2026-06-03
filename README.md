# Aura Music

A modern glassmorphism music player built with JavaFX.

## Features

* 🎵 MP3, WAV and M4A playback
* 🎨 Dynamic album artwork
* 📝 Synchronized lyrics (.lrc support)
* 📂 Automatic folder restore
* ⏯ Playback position restore
* 🔀 Shuffle mode
* 🔁 Repeat modes
* 📋 Queue management
* ⌨ Keyboard shortcuts
* 🪟 Modern transparent UI

## Screenshots

(screenshots here later)

## Build

```bash
.\mvnw.cmd clean package

jpackage --type app-image --name AuraMusicFX --input target --main-jar AuraMusicFX-1.0.0.jar --main-class dev.aman.auramusicfx.Launcher --icon src\main\resources\icons\AuraMusicFX_Ultimate.ico --dest dist

jpackage --type msi --name AuraMusicFX --app-image dist\AuraMusicFX --app-version 2.0 --vendor "Aman" --win-menu --win-shortcut --dest installer
```

## Run

```bash
java -jar target/AuraMusicFX-1.0.0.jar
```

## Installer

Windows MSI installer available in Releases.

## Author

Aman
