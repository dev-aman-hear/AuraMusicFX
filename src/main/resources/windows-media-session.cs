using System;
using System.IO;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Collections.Concurrent;
using Windows.Media;
using Windows.Media.Playback;

[assembly: AssemblyTitle("AuraMusicFX")]
[assembly: AssemblyProduct("AuraMusicFX")]
[assembly: AssemblyDescription("AuraMusicFX Media Host")]
[assembly: AssemblyCompany("Aman")]

public class Program {
    private static MediaPlayer _player;
    private static SystemMediaTransportControls _controls;
    private static SystemMediaTransportControlsDisplayUpdater _updater;
    private static ConcurrentQueue<string> _eventQueue = new ConcurrentQueue<string>();
    private static ConcurrentQueue<string> _commandQueue = new ConcurrentQueue<string>();
    private static string _silentAudioPath;

    public static void Main(string[] args) {
        try {
            Console.OutputEncoding = Encoding.UTF8;

            _player = new MediaPlayer();
            _player.CommandManager.IsEnabled = false;
            _player.Volume = 0;
            _player.IsLoopingEnabled = true;

            _controls = _player.SystemMediaTransportControls;
            _controls.IsEnabled = true;
            _controls.IsPlayEnabled = true;
            _controls.IsPauseEnabled = true;
            _controls.IsNextEnabled = true;
            _controls.IsPreviousEnabled = true;

            _updater = _controls.DisplayUpdater;
            _updater.Type = MediaPlaybackType.Music;

            _controls.ButtonPressed += OnButtonPressed;
            _controls.PlaybackPositionChangeRequested += OnPlaybackPositionChangeRequested;
            _player.MediaEnded += OnMediaEnded;

            _silentAudioPath = Path.Combine(Path.GetTempPath(), "AuraMusicFX-silent.wav");
            // Create a 10-minute silent WAV file to prevent frequent looping resets
            CreateSilentWav(_silentAudioPath, 8000, 4800000);

            var mediaSource = CreateMediaSourceFromPath(_silentAudioPath);
            if (mediaSource != null) {
                _player.Source = (Windows.Media.Playback.IMediaPlaybackSource)mediaSource;
            }

            Thread inputThread = new Thread(ReadStdin);
            inputThread.IsBackground = true;
            inputThread.Start();

            Console.WriteLine("READY");
            Console.Out.Flush();

            bool running = true;
            while (running) {
                string eventMsg;
                while (_eventQueue.TryDequeue(out eventMsg)) {
                    Console.WriteLine(eventMsg);
                    Console.Out.Flush();
                }

                string commandMsg;
                while (_commandQueue.TryDequeue(out commandMsg)) {
                    if (string.IsNullOrEmpty(commandMsg)) continue;
                    
                    string[] parts = commandMsg.Split('\t');
                    if (parts.Length == 0) continue;

                    switch (parts[0]) {
                        case "META":
                            if (parts.Length >= 4) {
                                _updater.MusicProperties.Title = DecodeField(parts[1]);
                                _updater.MusicProperties.Artist = DecodeField(parts[2]);
                                _updater.MusicProperties.AlbumTitle = DecodeField(parts[3]);
                            }
                             if (parts.Length >= 5) {
                                 string artPath = DecodeField(parts[4]);
                                 if (!string.IsNullOrEmpty(artPath) && File.Exists(artPath)) {
                                     object thumbnail = CreateThumbnailFromPath(artPath);
                                     if (thumbnail != null) {
                                         _updater.Thumbnail = (Windows.Storage.Streams.RandomAccessStreamReference)thumbnail;
                                     } else {
                                         _updater.Thumbnail = null;
                                     }
                                 } else {
                                     _updater.Thumbnail = null;
                                 }
                             } else {
                                 _updater.Thumbnail = null;
                             }
                            _updater.Update();
                            break;

                        case "STATUS":
                            if (parts.Length >= 2) {
                                string status = parts[1];
                                if (status == "PLAYING") {
                                    _player.Play();
                                    _controls.PlaybackStatus = MediaPlaybackStatus.Paused;
                                } else if (status == "PAUSED") {
                                    _player.Pause();
                                    _controls.PlaybackStatus = MediaPlaybackStatus.Playing;
                                } else {
                                    _player.Pause();
                                    _controls.PlaybackStatus = MediaPlaybackStatus.Stopped;
                                }
                            }
                            break;

                        case "TIMELINE":
                            if (parts.Length >= 3) {
                                try {
                                    double positionSec = double.Parse(parts[1]);
                                    double durationSec = double.Parse(parts[2]);

                                    var timeline = new SystemMediaTransportControlsTimelineProperties();
                                    timeline.StartTime = TimeSpan.FromSeconds(0);
                                    timeline.EndTime = TimeSpan.FromSeconds(durationSec);
                                    timeline.Position = TimeSpan.FromSeconds(positionSec);
                                    timeline.MinSeekTime = TimeSpan.FromSeconds(0);
                                    timeline.MaxSeekTime = TimeSpan.FromSeconds(durationSec);

                                    _controls.UpdateTimelineProperties(timeline);
                                } catch {
                                }
                            }
                            break;

                        case "QUIT":
                            running = false;
                            break;
                    }
                }

                Thread.Sleep(100);
            }
        } catch (Exception ex) {
            Console.Error.WriteLine("Error: " + ex.Message);
        } finally {
            Cleanup();
        }
    }

    private static void ReadStdin() {
        string line;
        while ((line = Console.ReadLine()) != null) {
            _commandQueue.Enqueue(line);
        }
    }

    private static void OnButtonPressed(SystemMediaTransportControls sender, SystemMediaTransportControlsButtonPressedEventArgs args) {
        _eventQueue.Enqueue("BUTTON\t" + args.Button.ToString().ToUpperInvariant());
    }

    private static void OnPlaybackPositionChangeRequested(SystemMediaTransportControls sender, PlaybackPositionChangeRequestedEventArgs args) {
        _eventQueue.Enqueue("BUTTON\tSEEK\t" + args.RequestedPlaybackPosition.TotalSeconds);
    }

    private static void OnMediaEnded(MediaPlayer sender, object args) {
        sender.PlaybackSession.Position = TimeSpan.Zero;
        sender.Play();
    }

    private static string DecodeField(string value) {
        if (string.IsNullOrEmpty(value)) return "";
        try {
            return Encoding.UTF8.GetString(Convert.FromBase64String(value));
        } catch {
            return "";
        }
    }

    private static void CreateSilentWav(string path, int sampleRate, int sampleCount) {
        try {
            using (var stream = File.Open(path, FileMode.Create))
            using (var writer = new BinaryWriter(stream)) {
                writer.Write(Encoding.ASCII.GetBytes("RIFF"));
                writer.Write(36 + sampleCount);
                writer.Write(Encoding.ASCII.GetBytes("WAVEfmt "));
                writer.Write(16);
                writer.Write((short)1);
                writer.Write((short)1);
                writer.Write(sampleRate);
                writer.Write(sampleRate);
                writer.Write((short)1);
                writer.Write((short)8);
                writer.Write(Encoding.ASCII.GetBytes("data"));
                writer.Write(sampleCount);
                
                byte[] data = new byte[sampleCount];
                for (int i = 0; i < sampleCount; i++) {
                    data[i] = 128;
                }
                writer.Write(data);
            }
        } catch {}
    }

    private static object CreateThumbnailFromPath(string path) {
        try {
            if (!File.Exists(path)) return null;
            var fileStream = File.OpenRead(path);

            Type extType = Type.GetType("System.IO.WindowsRuntimeStreamExtensions, System.Runtime.WindowsRuntime, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089");
            if (extType == null) {
                extType = Type.GetType("System.IO.WindowsRuntimeStreamExtensions");
            }
            if (extType == null) return null;

            var asRandomAccessStreamMethod = extType.GetMethod("AsRandomAccessStream", new Type[] { typeof(Stream) });
            if (asRandomAccessStreamMethod == null) return null;

            object winrtStream = asRandomAccessStreamMethod.Invoke(null, new object[] { fileStream });
            if (winrtStream == null) return null;

            Type streamRefType = Type.GetType("Windows.Storage.Streams.RandomAccessStreamReference, Windows.Storage, ContentType=WindowsRuntime");
            if (streamRefType == null) return null;

            Type iRandomAccessStreamType = Type.GetType("Windows.Storage.Streams.IRandomAccessStream, Windows.Storage, ContentType=WindowsRuntime");
            if (iRandomAccessStreamType == null) return null;

            var createFromStreamMethod = streamRefType.GetMethod("CreateFromStream", new Type[] { iRandomAccessStreamType });
            if (createFromStreamMethod == null) return null;

            return createFromStreamMethod.Invoke(null, new object[] { winrtStream });
        } catch (Exception ex) {
            Console.WriteLine("THUMBNAIL_ERROR\t" + ex.Message + "\t" + ex.StackTrace);
        }
        return null;
    }

    private static object CreateMediaSourceFromPath(string path) {
        try {
            Type mediaSourceType = Type.GetType("Windows.Media.Core.MediaSource, Windows.Media, ContentType=WindowsRuntime");
            if (mediaSourceType == null) return null;
            
            var method = mediaSourceType.GetMethod("CreateFromUri", new Type[] { typeof(Uri) });
            if (method == null) return null;

            Uri uri = new Uri(path);
            return method.Invoke(null, new object[] { uri });
        } catch (Exception ex) {
            Console.WriteLine("MEDIASOURCE_ERROR\t" + ex.Message + "\t" + ex.StackTrace);
        }
        return null;
    }

    private static void Cleanup() {
        try {
            if (_controls != null) {
                _controls.ButtonPressed -= OnButtonPressed;
                _controls.PlaybackPositionChangeRequested -= OnPlaybackPositionChangeRequested;
                _controls.IsEnabled = false;
            }
            if (_player != null) {
                _player.MediaEnded -= OnMediaEnded;
                _player.Dispose();
            }
            if (File.Exists(_silentAudioPath)) {
                File.Delete(_silentAudioPath);
            }
        } catch {}
    }
}
