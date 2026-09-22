import { useCallback, useEffect, useMemo, useState } from 'react';
import { App, Button, Collapse, Drawer, Empty, Progress, Space, Spin, Tag, Tooltip } from 'antd';
import {
  ArrowLeft, CheckCircle2, Circle, Clock, Compass, Lock, Play, Rocket, Sparkles, Target,
} from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  loadLearningPathDetail,
  type LearningPathDetail,
  type LearningPathNode,
} from '@/api/learningPath';
import styles from './index.module.scss';

const NODE_STATUS: Record<number, { label: string; color: string; className: string; tip: string }> = {
  0: { label: '未解锁', color: 'default', className: 'nodeLocked', tip: '完成前置节点后解锁' },
  1: { label: '进行中', color: 'blue', className: 'nodeLearning', tip: '当前可以学习' },
  2: { label: '已掌握', color: 'green', className: 'nodeMastered', tip: '已掌握，可以复习巩固' },
};

function formatTime(value?: string | null): string {
  if (!value) {
    return '';
  }
  return value.replace('T', ' ').substring(0, 16);
}

function nodeKeyOf(node: LearningPathNode): string {
  return node.itemId;
}

/**
 * 学习路线详情：目标 → 「现在做这个」→ 按阶段展开的路线图 → 选学分支 → 节点学习指南抽屉。
 * 未解锁节点不打开抽屉，改为提示需先完成的具体前置节点。
 */
export default function LearningPathDetailPage() {
  const { pathId = '' } = useParams();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<LearningPathDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeNode, setActiveNode] = useState<LearningPathNode | null>(null);
  const [activeStageKeys, setActiveStageKeys] = useState<string[]>([]);

  const load = useCallback(async () => {
    if (!pathId) {
      return;
    }
    setLoading(true);
    try {
      const data = await loadLearningPathDetail(pathId);
      setDetail(data);
      const stages = data.stages ?? [];
      const currentNode = [...stages.flatMap((stage) => stage.nodes ?? [])]
        .find((node) => node.itemId === data.currentNodeId);
      const currentStage = currentNode
        ? stages.find((stage) => (stage.nodes ?? []).some((node) => node.itemId === currentNode.itemId))
        : undefined;
      setActiveStageKeys(currentStage ? [currentStage.name] : stages.length > 0 ? [stages[0].name] : []);
    } catch {
      // 错误已统一提示
    } finally {
      setLoading(false);
    }
  }, [pathId]);

  useEffect(() => {
    void load();
  }, [load]);

  const stages = detail?.stages ?? [];
  const allNodes = useMemo(() => stages.flatMap((stage) => stage.nodes ?? []), [stages]);
  const currentNode = useMemo(
    () => allNodes.find((node) => node.itemId === detail?.currentNodeId)
      ?? allNodes.find((node) => node.status === 1)
      ?? null,
    [allNodes, detail?.currentNodeId],
  );
  const currentStageName = useMemo(() => {
    if (!currentNode) {
      return '';
    }
    return stages.find((stage) => (stage.nodes ?? []).some((node) => node.itemId === currentNode.itemId))?.name ?? '';
  }, [currentNode, stages]);

  /** 未解锁节点：不打开抽屉，提示需要先完成的前置节点 */
  const handleNodeClick = (node: LearningPathNode) => {
    if (node.status === 0) {
      const prerequisite = node.prerequisiteName || '上一个节点';
      message.info(`请先完成前置节点「${prerequisite}」，本节点会自动解锁`);
      return;
    }
    setActiveNode(node);
  };

  const askAi = (node: LearningPathNode, mode: 'explain' | 'quiz') => {
    const question = mode === 'explain'
      ? `请给我讲讲「${node.knowledgePointName}」，并结合我的学习路径告诉我该重点掌握什么。`
      : `针对《${node.knowledgePointName}》出 3 道题考考我。`;
    setActiveNode(null);
    navigate('/ai-tutor', { state: { presetQuestion: question } });
  };

  const renderNode = (node: LearningPathNode, index: number) => {
    const meta = NODE_STATUS[node.status] ?? NODE_STATUS[0];
    const locked = node.status === 0;
    const statusIcon = node.status === 2
      ? <CheckCircle2 size={15} className={styles.iconMastered} />
      : node.status === 1
        ? <Circle size={15} className={styles.iconLearning} />
        : <Lock size={15} className={styles.iconLocked} />;
    return (
      <button
        key={nodeKeyOf(node)}
        type="button"
        className={`${styles.node} ${styles[meta.className]}`}
        onClick={() => handleNodeClick(node)}
      >
        <span className={styles.nodeIndex}>{index + 1}</span>
        <span className={styles.nodeBody}>
          <span className={styles.nodeTitleRow}>
            {statusIcon}
            <span className={styles.nodeTitle}>{node.knowledgePointName}</span>
            {node.must === false ? <Tag>选学</Tag> : null}
            {node.itemType === 1 ? <Tag color="purple">复习</Tag> : null}
            {node.due ? <Tag color="red">该复习了</Tag> : null}
          </span>
          {node.task ? <span className={styles.nodeTask}>要做：{node.task}</span> : null}
          <span className={styles.nodeMeta}>
            <Tag color={meta.color}>{meta.label}</Tag>
            {node.way ? <span>{node.way}</span> : null}
            {node.minutes ? (
              <span>
                <Clock size={12} /> {node.minutes} 分钟
              </span>
            ) : null}
            <span>掌握度 {node.masteryScore || 0}%</span>
            {locked ? (
              <span className={styles.nodeLockHint}>
                需先完成「{node.prerequisiteName || '上一个节点'}」
              </span>
            ) : null}
          </span>
        </span>
        <span className={styles.nodeBar}>
          <Progress
            percent={node.masteryScore || 0}
            showInfo={false}
            size="small"
            strokeColor={node.status === 2 ? '#52c41a' : '#1677ff'}
          />
        </span>
      </button>
    );
  };

  if (loading && !detail) {
    return (
      <div className={styles.page}>
        <div className={styles.loadingBlock}>
          <Spin /> <span>正在加载学习路线…</span>
        </div>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className={styles.page}>
        <Empty description="学习路线不存在或已被删除">
          <Button onClick={() => navigate('/learning-path')}>返回路线库</Button>
        </Empty>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.detailHeader}>
        <Button type="text" icon={<ArrowLeft size={16} />} onClick={() => navigate('/learning-path')}>
          返回路线库
        </Button>
        <div className={styles.detailTitleRow}>
          <Compass size={20} />
          <span className={styles.detailTitle}>{detail.title}</span>
          <Tag color={detail.status === 1 ? 'green' : 'blue'}>
            {detail.status === 1 ? '已完成' : '进行中'}
          </Tag>
        </div>
        <div className={styles.detailProgress}>
          <Progress
            percent={detail.progress || 0}
            size="small"
            style={{ maxWidth: 260 }}
          />
          <span className={styles.pathSummaryText}>
            已完成 {detail.finishedItems || 0}/{detail.totalItems || 0} 个节点
          </span>
        </div>
      </div>

      {detail.goal || detail.outcome || detail.cadence || detail.startHint ? (
        <div className={styles.goalCard}>
          <div className={styles.goalTitle}>
            <Target size={16} />
            <span>这条路线要带你到哪里</span>
          </div>
          <div className={styles.goalGrid}>
            {detail.goal ? (
              <div className={styles.goalItem}>
                <span className={styles.goalLabel}>总目标</span>
                <span className={styles.goalValue}>{detail.goal}</span>
              </div>
            ) : null}
            {detail.outcome ? (
              <div className={styles.goalItem}>
                <span className={styles.goalLabel}>产出物</span>
                <span className={styles.goalValue}>{detail.outcome}</span>
              </div>
            ) : null}
            {detail.cadence ? (
              <div className={styles.goalItem}>
                <span className={styles.goalLabel}>建议节奏</span>
                <span className={styles.goalValue}>{detail.cadence}</span>
              </div>
            ) : null}
            {detail.startHint ? (
              <div className={styles.goalItem}>
                <span className={styles.goalLabel}>起点建议</span>
                <span className={styles.goalValue}>{detail.startHint}</span>
              </div>
            ) : null}
          </div>
        </div>
      ) : null}

      {currentNode ? (
        <div className={styles.focusCard}>
          <div className={styles.focusHeader}>
            <Rocket size={17} />
            <span>现在做这个</span>
            {currentStageName ? <Tag color="blue">{currentStageName}</Tag> : null}
          </div>
          <div className={styles.focusBody}>
            <div className={styles.focusName}>{currentNode.knowledgePointName}</div>
            {currentNode.task ? <div className={styles.focusTask}>要做什么：{currentNode.task}</div> : null}
            <div className={styles.focusMeta}>
              {currentNode.way ? <span>{currentNode.way}</span> : null}
              {currentNode.minutes ? <span>约 {currentNode.minutes} 分钟</span> : null}
              <span>掌握度 {currentNode.masteryScore || 0}%</span>
            </div>
          </div>
          <Space wrap>
            <Button type="primary" icon={<Play size={14} />} onClick={() => askAi(currentNode, 'explain')}>
              让 AI 讲这个知识点
            </Button>
            <Button icon={<Sparkles size={14} />} onClick={() => askAi(currentNode, 'quiz')}>
              练一练
            </Button>
            <Button onClick={() => navigate('/course-material')}>去课程教材找材料</Button>
          </Space>
          <div className={styles.focusNote}>
            练一练用对话内答题卡自测，本次自测不记入掌握度；掌握度由课时通关测验、节点快测和主观题批阅写入
          </div>
        </div>
      ) : null}

      {!currentNode && detail.status === 1 ? (
        <div className={styles.finishedCard}>
          <CheckCircle2 size={18} className={styles.iconMastered} />
          <div>
            <div className={styles.finishedTitle}>这条路线已全部完成</div>
            <div className={styles.finishedDesc}>
              主线节点都已掌握。可以回到路线库查看其它路线，或继续学习选学分支巩固兴趣方向。
            </div>
          </div>
          <Button className={styles.finishedAction} onClick={() => navigate('/learning-path')}>
            返回路线库
          </Button>
        </div>
      ) : null}

      <Collapse
        activeKey={activeStageKeys}
        onChange={(keys) => setActiveStageKeys(Array.isArray(keys) ? keys : [keys])}
        items={stages.map((stage) => {
          const nodes = stage.nodes ?? [];
          const mastered = nodes.filter((node) => node.status === 2).length;
          return {
            key: stage.name,
            label: (
              <span className={styles.stageHeader}>
                <span className={styles.stageName}>{stage.name}</span>
                {stage.goal ? <span className={styles.stageGoal}>{stage.goal}</span> : null}
                <Tag color={stage.finished ? 'green' : 'default'}>
                  {mastered}/{nodes.length} 已掌握
                </Tag>
              </span>
            ),
            children: (
              <div className={styles.stageBody}>
                {stage.checkpoint ? (
                  <div className={styles.stageCheckpoint}>阶段验收：{stage.checkpoint}</div>
                ) : null}
                <div className={styles.mainLine}>
                  {nodes.map((node, index) => renderNode(node, index))}
                </div>
              </div>
            ),
          };
        })}
      />

      {detail.branches && detail.branches.length > 0 ? (
        <div className={styles.branchesBlock}>
          <div className={styles.sectionTitle}>
            <Target size={16} />
            <span>选学分支（按兴趣挑 1 条即可）</span>
          </div>
          <div className={styles.branches}>
            {detail.branches.map((branch) => (
              <div key={branch.branchName} className={styles.branchCard}>
                <div className={styles.branchTitle}>兴趣分支 · {branch.branchName}</div>
                <div className={styles.branchNodes}>
                  {(branch.nodes ?? []).map((node, index) => renderNode(node, index))}
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      <Drawer
        title={activeNode ? activeNode.knowledgePointName : '节点'}
        placement="right"
        width={480}
        open={!!activeNode}
        onClose={() => setActiveNode(null)}
      >
        {activeNode ? (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Space size={8} wrap>
              <Tag color={(NODE_STATUS[activeNode.status] ?? NODE_STATUS[0]).color}>
                {(NODE_STATUS[activeNode.status] ?? NODE_STATUS[0]).label}
              </Tag>
              {activeNode.must === false ? <Tag>选学</Tag> : <Tag color="blue">必学</Tag>}
              {activeNode.itemType === 1 ? <Tag color="purple">复习节点</Tag> : null}
              {activeNode.due ? <Tag color="red">该复习了</Tag> : null}
            </Space>
            {activeNode.task ? (
              <div className={styles.drawerBlock}>
                <div className={styles.drawerLabel}>要做什么</div>
                <div className={styles.drawerText}>{activeNode.task}</div>
              </div>
            ) : null}
            {activeNode.way ? (
              <div className={styles.drawerBlock}>
                <div className={styles.drawerLabel}>怎么学</div>
                <div className={styles.drawerText}>{activeNode.way}</div>
              </div>
            ) : null}
            <div className={styles.drawerBlock}>
              <div className={styles.drawerLabel}>怎么算学会</div>
              <div className={styles.drawerText}>
                掌握度 ≥ 80% 记为「已掌握」；当前掌握度 {activeNode.masteryScore || 0}%，练习 {activeNode.practiceCount || 0} 次
                {activeNode.nextReviewTime ? `，下次复习 ${formatTime(activeNode.nextReviewTime)}` : ''}
                {activeNode.finishTime ? `，掌握于 ${formatTime(activeNode.finishTime)}` : ''}
              </div>
            </div>
            {activeNode.learningTip ? (
              <div className={styles.drawerBlock}>
                <div className={styles.drawerLabel}>学习建议</div>
                <div className={styles.drawerText}>{activeNode.learningTip}</div>
              </div>
            ) : null}
            <div className={styles.drawerBlock}>
              <div className={styles.drawerLabel}>下一步</div>
              <Space direction="vertical" size={8} style={{ width: '100%' }}>
                <Button type="primary" block onClick={() => askAi(activeNode, 'explain')}>
                  让 AI 讲这个知识点
                </Button>
                <Tooltip title="用对话内答题卡自测（本次自测不记入掌握度）">
                  <Button block icon={<Sparkles size={14} />} onClick={() => askAi(activeNode, 'quiz')}>
                    练一练（自测，不记掌握度）
                  </Button>
                </Tooltip>
              </Space>
            </div>
          </Space>
        ) : null}
      </Drawer>
    </div>
  );
}
