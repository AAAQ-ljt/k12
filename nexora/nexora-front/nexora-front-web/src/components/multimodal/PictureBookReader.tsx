import { useCallback, useEffect, useRef, useState } from 'react';
import { ChevronLeft, ChevronRight, Loader2, Pause, Volume2 } from 'lucide-react';
import { Select, Switch } from 'antd';
import {
  PICTURE_BOOK_VOICES,
  pictureBookAudioUrl,
  pictureBookImageUrl,
  type PictureBookAudioTask,
  type PictureBookScript,
} from '@/api/pictureBook';
import styles from './PictureBookReader.module.scss';

/** 音色选择器选项：默认音色（学段默认）+ 9 个预置音色 */
const VOICE_OPTIONS = [
  { label: '默认音色', value: '' },
  ...PICTURE_BOOK_VOICES.map((voice) => ({ label: voice, value: voice })),
];

/**
 * 绘本翻页阅读器：图文页 + 左右翻页 + 页数指示 + 语音朗读。
 * 朗读：每页一条旁白音频（后端 MiMo TTS 合成落盘），支持单页播放 / 连播自动翻页 /
 * 音色切换（已有同音色音频直接播，否则按所选音色异步生成后播放）。
 * 页图缺失时显示文字占位，可选接入手动「补画本页插图」。
 */
export default function PictureBookReader({
  resourceId,
  script,
  fixingPage,
  onFixPage,
  audioTask,
  onGenerateAudio,
}: {
  resourceId: string;
  script: PictureBookScript;
  /** 正在补画的页码（0 开始），用于按钮 loading 态 */
  fixingPage?: number | null;
  /** 点击补画回调（父组件负责任务提交与轮询刷新） */
  onFixPage?: (page: number) => void;
  /** 进行中的旁白任务（null=无任务），由父组件提交与轮询 */
  audioTask?: PictureBookAudioTask | null;
  /** 点击生成旁白回调：page=null 整本补录，数字=单页（重）录制；voice 为空=学段默认 */
  onGenerateAudio?: (page: number | null, voice: string) => void;
}) {
  const pages = script.pages || [];
  const [current, setCurrent] = useState(0);
  /** 所选音色（''=默认音色，即学段默认） */
  const [selectedVoice, setSelectedVoice] = useState(script.voice ?? '');
  /** 重录完成后的音频版本号，用于击穿浏览器缓存 */
  const [audioVersion, setAudioVersion] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [autoPlayNext, setAutoPlayNext] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  /** 连播翻页标记：页面切换 effect 里据此自动播放新页 */
  const playNextRef = useRef(false);
  /** 旁白任务结束（audioTask 由父组件在完成后清空）：重录的音频 URL 加版本参数防缓存 */
  const prevTaskRef = useRef<PictureBookAudioTask | null>(null);

  const stopAudio = useCallback(() => {
    audioRef.current?.pause();
  }, []);

  // 音色切换后当前页音频可能不再匹配，直接停止播放
  useEffect(() => {
    audioRef.current?.pause();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedVoice]);

  // 翻页：停止当前音频；连播模式下自动播放新页音频
  useEffect(() => {
    const el = audioRef.current;
    if (el) {
      el.pause();
      el.currentTime = 0;
    }
    setPlaying(false);
    if (playNextRef.current) {
      playNextRef.current = false;
      const newPage = pages[current];
      const newPlayable = !!newPage?.audioFile && (!selectedVoice || newPage.audioVoice === selectedVoice);
      if (newPlayable) {
        // src 已随渲染更新，等一拍再播，避免与 src 设置竞争
        window.setTimeout(() => {
          audioRef.current?.play().catch(() => setPlaying(false));
        }, 50);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current]);

  useEffect(() => {
    if (prevTaskRef.current && !audioTask) {
      setAudioVersion((prev) => prev + 1);
      stopAudio();
    }
    prevTaskRef.current = audioTask ?? null;
  }, [audioTask, stopAudio]);

  if (pages.length === 0) {
    return <div className={styles.empty}>绘本数据异常</div>;
  }

  // 兜底：pages 内可能存在 null/残缺元素（历史异常产物），渲染安全降级
  const page = pages[current] ?? null;
  const pageText = page?.text ?? '';
  const hasImage = !!page?.imageFile;
  const fixing = fixingPage === current;

  const hasAudio = !!page?.audioFile;
  /** 当前页音频是否可直接播放（未选音色=任何音色均可；选了音色须一致） */
  const pagePlayable = hasAudio && (!selectedVoice || page?.audioVoice === selectedVoice);
  /** 是否存在缺音频或音色与所选不符的页（显示「生成全书朗读」） */
  const bookNeedsAudio = pages.some(
    (item) => !item.audioFile || (selectedVoice && item.audioVoice !== selectedVoice),
  );
  const audioTaskRunning = !!audioTask;
  const voiceLabel = selectedVoice || '默认音色';

  const handlePlayPause = () => {
    const el = audioRef.current;
    if (!el || !pagePlayable) {
      return;
    }
    if (playing) {
      el.pause();
    } else {
      void el.play().catch(() => setPlaying(false));
    }
  };

  /** 播完：连播开启则翻到下一页（由翻页 effect 接力播放），否则停在原地 */
  const handleEnded = () => {
    setPlaying(false);
    if (autoPlayNext && current < pages.length - 1) {
      playNextRef.current = true;
      setCurrent((prev) => Math.min(pages.length - 1, prev + 1));
    }
  };

  return (
    <div className={styles.reader}>
      <div className={styles.stage}>
        {hasImage ? (
          <img
            key={current}
            className={styles.pageImage}
            src={pictureBookImageUrl(resourceId, current)}
            alt={`第 ${current + 1} 页`}
          />
        ) : (
          <div className={styles.pagePlaceholder}>
          （本页暂无插图）
          {script.imageError ? <div className={styles.placeholderError}>{script.imageError}</div> : null}
          {onFixPage ? (
            <button
              type="button"
              className={styles.fixButton}
              disabled={fixing}
              onClick={() => onFixPage(current)}
            >
              {fixing ? '补画中，请稍候...' : '补画本页插图'}
            </button>
          ) : null}
        </div>
        )}
        <div className={styles.pageText}>{pageText || '（本页内容缺失）'}</div>
      </div>

      {onGenerateAudio ? (
        <div className={styles.narrationBar}>
          <Select
            size="middle"
            value={selectedVoice}
            onChange={setSelectedVoice}
            options={VOICE_OPTIONS}
            className={styles.voiceSelect}
            aria-label="选择朗读音色"
          />
          {pagePlayable ? (
            <button type="button" className={styles.playButton} onClick={handlePlayPause}>
              {playing ? <Pause size={18} /> : <Volume2 size={18} />}
              <span>{playing ? '停止朗读' : '朗读本页'}</span>
            </button>
          ) : (
            <button
              type="button"
              className={styles.playButton}
              disabled={audioTaskRunning}
              onClick={() => onGenerateAudio(current, selectedVoice)}
            >
              <Volume2 size={18} />
              <span>
                {audioTask?.page === current
                  ? '录制中...'
                  : hasAudio
                    ? `用「${voiceLabel}」重录本页`
                    : `用「${voiceLabel}」朗读本页`}
              </span>
            </button>
          )}
          <label className={styles.autoPlayRow}>
            <Switch size="small" checked={autoPlayNext} onChange={setAutoPlayNext} />
            <span>连播</span>
          </label>
          {bookNeedsAudio && !audioTaskRunning ? (
            <button
              type="button"
              className={styles.wholeBookButton}
              onClick={() => onGenerateAudio(null, selectedVoice)}
            >
              用「{voiceLabel}」生成全书朗读
            </button>
          ) : null}
        </div>
      ) : null}

      {audioTaskRunning ? (
        <div className={styles.narrationProgress}>
          <Loader2 size={14} className={styles.spinner} />
          {audioTask.message || '正在录制旁白...'}
          {audioTask.total > 0 ? `（${audioTask.current}/${audioTask.total} 页）` : ''}
        </div>
      ) : null}
      {hasAudio && !pagePlayable && !audioTaskRunning ? (
        <div className={styles.narrationHint}>
          本页旁白为「{page?.audioVoice}」音色，切换音色后可重新录制。
        </div>
      ) : null}
      {script.audioError && !audioTaskRunning ? (
        <div className={styles.narrationHint}>{script.audioError}</div>
      ) : null}

      {/* 隐藏音频元素：src 随页码/音色版本更新 */}
      {pagePlayable ? (
        <audio
          ref={audioRef}
          src={pictureBookAudioUrl(resourceId, current, audioVersion)}
          preload="none"
          onPlay={() => setPlaying(true)}
          onPause={() => setPlaying(false)}
          onEnded={handleEnded}
        />
      ) : null}

      <div className={styles.footer}>
        <button
          type="button"
          className={styles.navButton}
          disabled={current === 0}
          onClick={() => setCurrent((prev) => Math.max(0, prev - 1))}
        >
          <ChevronLeft size={18} />
          <span>上一页</span>
        </button>
        <span className={styles.pageIndicator}>{current + 1} / {pages.length}</span>
        <button
          type="button"
          className={styles.navButton}
          disabled={current >= pages.length - 1}
          onClick={() => setCurrent((prev) => Math.min(pages.length - 1, prev + 1))}
        >
          <span>下一页</span>
          <ChevronRight size={18} />
        </button>
      </div>
    </div>
  );
}
