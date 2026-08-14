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
        m3u8(video, videoUrl, art) {
          if (Hls.isSupported()) {
            const hls = new Hls();
            hls.loadSource(videoUrl);
            hls.attachMedia(video);
            art.on('destroy', () => hls.destroy());
          } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = videoUrl;
          }
        },
      },
    });
    playerRef.current = player;
    return () => {
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
