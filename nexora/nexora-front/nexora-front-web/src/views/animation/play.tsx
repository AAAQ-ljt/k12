import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Empty, Skeleton } from 'antd';
import { ArrowLeft } from 'lucide-react';
import {
  getAnimationResource,
  parseAnimationScript,
  type AnimationResource,
  type AnimationScript,
} from '@/api/animation';
import SvgStepPlayer from '@/components/multimodal/SvgStepPlayer';
import styles from './play.module.scss';

/** 动画大屏播放页：全尺寸分步播放 */
export default function AnimationPlay() {
  const { resourceId = '' } = useParams();
  const navigate = useNavigate();
  const [resource, setResource] = useState<AnimationResource | null>(null);
  const [script, setScript] = useState<AnimationScript | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotFound(false);
    getAnimationResource(resourceId)
      .then((data) => {
        if (!active) {
          return;
        }
        const parsed = parseAnimationScript(data.extJson);
        if (!parsed) {
          setNotFound(true);
          return;
        }
        setResource(data);
        setScript(parsed);
      })
      .catch(() => {
        if (active) {
          setNotFound(true);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [resourceId]);

  if (loading) {
    return (
      <div className={styles.playPage}>
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  if (notFound || !script) {
    return (
      <div className={styles.playPage}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/animation')}>
          返回动画列表
        </Button>
        <Empty description="动画不存在或脚本无法解析" style={{ marginTop: 80 }} />
      </div>
    );
  }

  return (
    <div className={styles.playPage}>
      <header className={styles.playHeader}>
        <Button icon={<ArrowLeft size={16} />} onClick={() => navigate('/animation')}>
          返回动画列表
        </Button>
        <div className={styles.playTitle}>
          {resource?.resourceName || script.title}
          <span className={styles.playSub}>{script.steps.length} 步讲解 · AI 生成</span>
        </div>
      </header>
      <div className={styles.playStage}>
        <SvgStepPlayer script={script} />
      </div>
    </div>
  );
}