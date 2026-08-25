import { useCallback, useEffect, useRef } from 'react';
import Artplayer from 'artplayer';
import Hls from 'hls.js';
import BaseDialog from '@/components/BaseDialog';
import {
  getStudentVideoPlaylistUrl,
  getVideoPlaylistUrl,
  type ResourceInfo,
} from '@/api/resource';
import styles from './VideoPreviewModal.module.scss';

interface VideoPlayerProps {
  open: boolean;
  onReady: (player: Artplayer | null) => void;
  url: string;
}

function VideoPlayer({ open, onReady, url }: VideoPlayerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const playerRef = useRef<Artplayer | null>(null);

  useEffect(() => {
    if (!open || !containerRef.current) {
      if (playerRef.current) {
        playerRef.current.destroy();
        playerRef.current = null;
      }
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
    onReady(player);
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
        // 父级关闭弹窗时已销毁，重复销毁直接忽略
      }
      playerRef.current = null;
      onReady(null);
    };
  }, [onReady, open, url]);

  return (
    <div className={styles.playerWrap}>
      <div ref={containerRef} className={styles.player} />
    </div>
  );
}

interface VideoPreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  userId?: string;
  onClose: () => void;
}

export default function VideoPreviewModal({
  open,
  resource,
  onClose,
  userId,
}: VideoPreviewModalProps) {
  const playerRef = useRef<Artplayer | null>(null);
  const previewUrl = resource
    ? userId
      ? getStudentVideoPlaylistUrl(resource.resourceId, userId)
      : getVideoPlaylistUrl(resource.resourceId)
    : '';
  const handlePlayerReady = useCallback((player: Artplayer | null) => {
    playerRef.current = player;
  }, []);

  useEffect(() => {
    if (!open && playerRef.current) {
      try {
        playerRef.current.destroy();
      } catch {
        // 播放器已销毁时忽略
      }
      playerRef.current = null;
    }
  }, [open]);

  return (
    <BaseDialog
      className={styles.videoPreviewModal}
      open={open}
      title={resource?.resourceName || '视频预览'}
      width="80vw"
      top={40}
      showCancel={false}
      footer={null}
      contentPadding={0}
      bodyStyle={{ padding: 0, maxHeight: 'none', overflow: 'hidden' }}
      onCancel={onClose}
    >
      {resource && (
        <VideoPlayer
          key={resource.resourceId}
          open={open}
          onReady={handlePlayerReady}
          url={previewUrl}
        />
      )}
    </BaseDialog>
  );
}
