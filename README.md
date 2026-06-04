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

<img width="420" height="720" alt="1" src="https://github.com/user-attachments/assets/b84207a6-5f08-4247-8d29-aea16be97efb" />
<img width="420" height="720" alt="3" src="https://github.com/user-attachments/assets/85cea05b-d96f-41db-bcc1-af4867f97500" />
<img width="420" height="720" alt="4" src="https://github.com/user-attachments/assets/fa333a80-2038-447d-a8f8-54c4df69cf42" />
<img width="325" height="538" alt="2" src="https://github.com/user-attachments/assets/3c1e3cb0-47d2-48b0-8dd5-af99bf29ad41" />


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
