import { useEffect, useRef } from 'react';
import Artplayer from 'artplayer';
import Hls from 'hls.js';
import styles from './VideoPlayer.module.scss';

interface VideoPlayerProps {
  url: string;
}

export default function VideoPlayer({ url }: VideoPlayerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const playerRef = useRef<Artplayer | null>(null);

  useEffect(() => {
    if (!containerRef.current) {
      return undefined;
    }

    let activeHls: Hls | null = null;
    // ArtPlayer 的 m3u8 初始化是异步的，卸载后仍可能回调，需阻止补建 HLS
    let disposed = false;

    const destroyHls = () => {
      if (!activeHls) {
        return;
      }
      const hls = activeHls;
      activeHls = null;
      try {
        hls.detachMedia();
      } catch {
        // 已分离时忽略
      }
      try {
        hls.destroy();
      } catch {
        // 已销毁时忽略
      }
    };

    const player = new Artplayer({
      container: containerRef.current,
      url,
      autoSize: true,
      autoplay: true,
      theme: '#1677ff',
      volume: 0.8,
      isLive: false,
      muted: false,
      pip: true,
      fullscreen: true,
      fullscreenWeb: true,
      playsInline: true,
      setting: true,
      customType: {
        m3u8(video, videoUrl) {
          if (disposed) {
            return;
          }
          if (Hls.isSupported()) {
            destroyHls();
            const hls = new Hls();
            activeHls = hls;
            hls.loadSource(videoUrl);
            hls.attachMedia(video);
          } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = videoUrl;
          }
        },
      },
    });
    playerRef.current = player;
    player.on('destroy', () => {
      disposed = true;
      destroyHls();
    });

    const handleVisibilityChange = () => {
      if (document.hidden && playerRef.current) {
        playerRef.current.pause();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      disposed = true;
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      try {
        player.pause();
      } catch {
        // 已销毁时忽略
      }
      destroyHls();
      try {
        player.destroy();
      } catch {
        // 播放器已销毁时忽略
      }
      playerRef.current = null;
    };
  }, [url]);

  return (
    <div className={styles.playerWrap}>
      <div ref={containerRef} className={styles.player} />
    </div>
  );
}
