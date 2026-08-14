import { useEffect, useRef } from 'react';
import Artplayer from 'artplayer';
import Hls from 'hls.js';
import BaseDialog from '@/components/BaseDialog';
import { getVideoPlaylistUrl, type ResourceInfo } from '@/api/resource';
import styles from './VideoPreviewModal.module.scss';

interface VideoPlayerProps {
  url: string;
}

function VideoPlayer({ url }: VideoPlayerProps) {
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
      player.destroy();
      playerRef.current = null;
    };
  }, [url]);

  return (
    <div className={styles.playerWrap}>
      <div ref={containerRef} className={styles.player} />
    </div>
  );
}

interface VideoPreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  onClose: () => void;
}

export default function VideoPreviewModal({ open, resource, onClose }: VideoPreviewModalProps) {
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
        <VideoPlayer key={resource.resourceId} url={getVideoPlaylistUrl(resource.resourceId)} />
      )}
    </BaseDialog>
  );
}
