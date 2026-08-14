import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { App, Empty, Segmented, Spin, Tag } from 'antd';
import { Clock, FileText, Film, HardDrive, Image as ImageIcon, Link2, BookOpen } from 'lucide-react';
import {
  loadResourceList,
  type StudentResourceInfo,
} from '@/api/resource';
import { getGradeText } from '@/types/common';
import { useAuthStore } from '@/stores/auth';
import styles from './index.module.scss';

type ResourceTypeFilter = 'ALL' | 'VIDEO' | 'IMAGE' | 'DOCUMENT' | 'LINK';

const TYPE_META: Record<string, { label: string; color: string; icon: typeof FileText }> = {
  VIDEO: { label: '视频', color: '#1677ff', icon: Film },
  IMAGE: { label: '图片', color: '#52c41a', icon: ImageIcon },
  DOCUMENT: { label: '文档', color: '#fa8c16', icon: FileText },
  PPT: { label: '文档', color: '#fa8c16', icon: FileText },
  WORD: { label: '文档', color: '#fa8c16', icon: FileText },
  PDF: { label: '文档', color: '#fa8c16', icon: FileText },
  PICTURE_BOOK: { label: '绘本', color: '#eb2f96', icon: BookOpen },
  LINK: { label: '链接', color: '#722ed1', icon: Link2 },
};

function formatSize(size?: number): string {
  if (!size) {
    return '';
  }
  if (size >= 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
  }
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }
  return `${Math.max(1, Math.round(size / 1024))} KB`;
}

function formatDuration(seconds?: number): string {
  if (!seconds) {
    return '';
  }
  const minute = Math.floor(seconds / 60);
  const second = seconds % 60;
  return `${minute}:${String(second).padStart(2, '0')}`;
}

function normalizeType(type?: string): string {
  if (!type) {
    return 'DOCUMENT';
  }
  if (['VIDEO', 'IMAGE', 'DOCUMENT', 'PPT', 'WORD', 'PDF', 'PICTURE_BOOK', 'LINK'].includes(type)) {
    return type;
  }
  return 'DOCUMENT';
}

export default function CourseMaterial() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const userInfo = useAuthStore((state) => state.userInfo);
  const [loading, setLoading] = useState(true);
  const [resources, setResources] = useState<StudentResourceInfo[]>([]);
  const [filter, setFilter] = useState<ResourceTypeFilter>('ALL');

  useEffect(() => {
    let active = true;
    setLoading(true);
    loadResourceList({ pageNo: 1, pageSize: 100 })
      .then((result) => {
        if (active) {
          setResources(result.list || []);
        }
      })
      .catch(() => {
        // 请求层已统一提示
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  const visibleResources = useMemo(() => {
    if (filter === 'ALL') {
      return resources;
    }
    return resources.filter((item) => {
      const type = normalizeType(item.resourceType);
      if (filter === 'DOCUMENT') {
        return ['DOCUMENT', 'PPT', 'WORD', 'PDF'].includes(type);
      }
      return type === filter;
    });
  }, [filter, resources]);

  const handleOpen = (resource: StudentResourceInfo) => {
    if (normalizeType(resource.resourceType) === 'LINK') {
      const url = resource.description?.startsWith('http') ? resource.description : '';
      if (url) {
        window.open(url, '_blank', 'noopener,noreferrer');
      } else {
        message.info('该资料暂无可用链接');
      }
      return;
    }
    navigate(`/course-material/${resource.resourceId}`);
  };

  return (
    <div className={styles.materialPage}>
      <header className={styles.pageHeader}>
        <div>
          <h2>课程教材</h2>
          <p>当前{getGradeText(userInfo) || '学段'}可用的学习资料，点击卡片开始学习</p>
        </div>
        <Segmented<ResourceTypeFilter>
          value={filter}
          onChange={setFilter}
          options={[
            { label: '全部', value: 'ALL' },
            { label: '视频', value: 'VIDEO' },
            { label: '文档', value: 'DOCUMENT' },
            { label: '图片', value: 'IMAGE' },
          ]}
        />
      </header>

      <div className={styles.resourceGrid}>
        {loading ? (
          <div className={styles.loadingBox}>
            <Spin />
          </div>
        ) : visibleResources.length === 0 ? (
          <Empty description="当前学段暂无可用教材" className={styles.emptyBox} />
        ) : (
          visibleResources.map((resource) => {
            const type = normalizeType(resource.resourceType);
            const meta = TYPE_META[type] || TYPE_META.DOCUMENT;
            const Icon = meta.icon;
            return (
              <button
                key={resource.resourceId}
                className={styles.resourceCard}
                onClick={() => handleOpen(resource)}
              >
                <div className={styles.cardTop}>
                  <span className={styles.cardIcon} style={{ '--icon-color': meta.color } as React.CSSProperties}>
                    <Icon size={22} />
                  </span>
                  <Tag color={meta.color} className={styles.typeTag}>
                    {meta.label}
                  </Tag>
                </div>
                <div className={styles.cardTitle}>{resource.resourceName}</div>
                <div className={styles.cardDesc}>
                  {resource.description || '暂无简介'}
                </div>
                <div className={styles.cardFooter}>
                  {resource.duration ? (
                    <span>
                      <Clock size={13} />
                      {formatDuration(resource.duration)}
                    </span>
                  ) : null}
                  {resource.fileSize ? (
                    <span>
                      <HardDrive size={13} />
                      {formatSize(resource.fileSize)}
                    </span>
                  ) : null}
                </div>
              </button>
            );
          })
        )}
      </div>
    </div>
  );
}
