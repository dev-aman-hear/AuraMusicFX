$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$player = [Windows.Media.Playback.MediaPlayer, Windows.Media.Playback, ContentType = WindowsRuntime]::new()
$player.CommandManager.IsEnabled = $false
$player.Volume = 0
$player.IsLoopingEnabled = $true

$silentAudioPath = Join-Path $env:TEMP "AuraMusicFX-silent.wav"
$sampleRate = 8000
$sampleCount = 2000
$stream = [System.IO.File]::Open($silentAudioPath, [System.IO.FileMode]::Create)
$binaryWriter = [System.IO.BinaryWriter]::new($stream)
try {
    $binaryWriter.Write([Text.Encoding]::ASCII.GetBytes("RIFF"))
    $binaryWriter.Write([int](36 + $sampleCount))
    $binaryWriter.Write([Text.Encoding]::ASCII.GetBytes("WAVEfmt "))
    $binaryWriter.Write([int]16)
    $binaryWriter.Write([int16]1)
    $binaryWriter.Write([int16]1)
    $binaryWriter.Write([int]$sampleRate)
    $binaryWriter.Write([int]$sampleRate)
    $binaryWriter.Write([int16]1)
    $binaryWriter.Write([int16]8)
    $binaryWriter.Write([Text.Encoding]::ASCII.GetBytes("data"))
    $binaryWriter.Write([int]$sampleCount)
    $binaryWriter.Write([byte[]](New-Object byte[] $sampleCount | ForEach-Object { 128 }))
}
finally {
    $binaryWriter.Dispose()
    $stream.Dispose()
}

$silentAudioUri = [Uri]::new($silentAudioPath)
$player.Source = [Windows.Media.Core.MediaSource, Windows.Media.Core, ContentType = WindowsRuntime]::CreateFromUri(
    $silentAudioUri
)

$controls = $player.SystemMediaTransportControls
$controls.IsEnabled = $true
$controls.IsPlayEnabled = $true
$controls.IsPauseEnabled = $true
$controls.IsNextEnabled = $true
$controls.IsPreviousEnabled = $true

$updater = $controls.DisplayUpdater
$updater.Type = [Windows.Media.MediaPlaybackType, Windows.Media, ContentType = WindowsRuntime]::Music

$buttonHandler = [Windows.Foundation.TypedEventHandler[
    Windows.Media.SystemMediaTransportControls,
    Windows.Media.SystemMediaTransportControlsButtonPressedEventArgs
]] {
    param($sender, $eventArgs)
    [Console]::Out.WriteLine("BUTTON`t" + $eventArgs.Button.ToString().ToUpperInvariant())
    [Console]::Out.Flush()
}
$buttonToken = $controls.add_ButtonPressed($buttonHandler)

function Decode-Field([string] $value) {
    if ([string]::IsNullOrEmpty($value)) {
        return ""
    }
    return [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($value))
}

[Console]::Out.WriteLine("READY")
[Console]::Out.Flush()

try {
    while (($line = [Console]::In.ReadLine()) -ne $null) {
        $parts = $line.Split("`t")
        switch ($parts[0]) {
            "META" {
                if ($parts.Length -ge 4) {
                    $updater.MusicProperties.Title = Decode-Field $parts[1]
                    $updater.MusicProperties.Artist = Decode-Field $parts[2]
                    $updater.MusicProperties.AlbumTitle = Decode-Field $parts[3]
                    $updater.Update()
                }
            }
            "STATUS" {
                if ($parts.Length -ge 2) {
                    $controls.PlaybackStatus = switch ($parts[1]) {
                        "PLAYING" {
                            $player.Play()
                            [Windows.Media.MediaPlaybackStatus, Windows.Media, ContentType = WindowsRuntime]::Playing
                        }
                        "PAUSED"  {
                            $player.Pause()
                            [Windows.Media.MediaPlaybackStatus, Windows.Media, ContentType = WindowsRuntime]::Paused
                        }
                        default   {
                            $player.Pause()
                            [Windows.Media.MediaPlaybackStatus, Windows.Media, ContentType = WindowsRuntime]::Stopped
                        }
                    }
                }
            }
            "QUIT" { break }
        }
        if ($parts[0] -eq "QUIT") {
            break
        }
    }
}
finally {
    $controls.remove_ButtonPressed($buttonToken)
    $controls.IsEnabled = $false
    $player.Dispose()
    Remove-Item $silentAudioPath -Force -ErrorAction SilentlyContinue
}
