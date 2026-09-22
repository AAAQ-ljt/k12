import { Drawer, Empty } from 'antd';
import WikiListPanel from '@/components/knowledge/WikiListPanel';
import styles from './KnowledgeDrawer.module.scss';

interface Props {
  open: boolean;
  onClose: () => void;
  /** 变化时刷新列表（AI 改动知识页后由对话推送触发） */
  reloadKey: number;
}

/**
 * AI 助教「我的知识页」侧边抽屉：集中查看 AI 生成与整理的知识页，
 * 支持阅览、编辑、确认入库、删除（与资源中心「知识页」目录共用同一套组件与接口）。
 */
export default function KnowledgeDrawer({ open, onClose, reloadKey }: Props) {
  return (
    <Drawer
      title="我的知识页"
      placement="right"
      width={880}
      open={open}
      onClose={onClose}
      styles={{ body: { paddingTop: 12 } }}
    >
      <div className={styles.hint}>
        这里汇总 AI 为你生成与整理的知识页（草稿需确认入库后才能被 AI 助教检索到）。
        也可以直接在对话里说：「把《XX》总结一下」「把这两篇整理成一篇」「把《XX》入库」。
      </div>
      <WikiListPanel
        reloadKey={reloadKey}
        withToolbar
        emptyText={
          <Empty description="还没有知识页：可在资源中心「原始资料」生成，或直接对 AI 助教说「帮我整理一篇知识页」" />
        }
      />
    </Drawer>
  );
}
