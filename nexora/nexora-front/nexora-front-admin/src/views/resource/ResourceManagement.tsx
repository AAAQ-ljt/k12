import { useMemo, useState } from 'react';
import type { Key } from 'react';
import {
  App,
  Button,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tree,
  Upload,
  type TableProps,
  type UploadFile,
} from 'antd';
import {
  BookOpen,
  Download,
  Eye,
  FileText,
  Film,
  FolderOpen,
  FolderPlus,
  Image,
  Move,
  Presentation,
  RotateCcw,
  Search,
  Upload as UploadIcon,
} from 'lucide-react';
import StageTag from '@/components/StageTag';
import StatusTag from '@/components/StatusTag';
import {
  RESOURCE_STATUS_MAP,
  RESOURCE_TYPE_MAP,
  RESOURCE_TYPE_OPTIONS,
  STAGE_OPTIONS,
} from '@/types/common';
import styles from './index.module.scss';

interface DirNode {
  key: string;
  title: string;
  children?: DirNode[];
}

interface ResourceFile {
  id: string;
  name: string;
  type: string;
  dir: string;
  sizeText: string;
  stage: string;
  status: number;
  updateTime: string;
  uploader: string;
}

const MOCK_DIRS: DirNode[] = [
  {
    key: 'root',
    title: '全部资源',
    children: [
      {
        key: 'primary-low',
        title: '小学低年级',
        children: [
          { key: 'primary-low-picture', title: '绘本素材' },
          { key: 'primary-low-animation', title: '趣味动画' },
        ],
      },
      {
        key: 'primary-high',
        title: '小学高年级',
        children: [
          { key: 'primary-high-ppt', title: '课件PPT' },
          { key: 'primary-high-doc', title: '知识文档' },
        ],
      },
      {
        key: 'junior',
        title: '初中',
        children: [
          { key: 'junior-video', title: '视频课程' },
          { key: 'junior-exercise', title: '习题资源' },
        ],
      },
      {
        key: 'senior',
        title: '高中',
        children: [
          { key: 'senior-ppt', title: '神经网络课件' },
          { key: 'senior-video', title: '视频课程' },
          { key: 'senior-doc', title: '知识文档' },
        ],
      },
      {
        key: 'common',
        title: '通用资料',
        children: [
          { key: 'common-manual', title: '操作手册' },
          { key: 'common-template', title: '模板文件' },
        ],
      },
    ],
  },
];

const MOCK_FILES: ResourceFile[] = [
  {
    id: 'r001',
    name: '神经网络入门.pptx',
    type: 'PPT',
    dir: 'senior-ppt',
    sizeText: '18.6 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-12 14:30',
    uploader: 'admin',
  },
  {
    id: 'r002',
    name: '冒泡排序讲解.pptx',
    type: 'PPT',
    dir: 'senior-ppt',
    sizeText: '12.2 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-11 10:05',
    uploader: 'admin',
  },
  {
    id: 'r003',
    name: 'AI 伦理案例集.pdf',
    type: 'DOCUMENT',
    dir: 'senior-doc',
    sizeText: '3.8 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-10 16:42',
    uploader: 'admin',
  },
  {
    id: 'r004',
    name: '机器学习概念.pdf',
    type: 'DOCUMENT',
    dir: 'senior-doc',
    sizeText: '5.1 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-09 09:18',
    uploader: 'admin',
  },
  {
    id: 'r005',
    name: 'Python 基础练习.docx',
    type: 'WORD',
    dir: 'junior-exercise',
    sizeText: '1.4 MB',
    stage: 'JUNIOR',
    status: 1,
    updateTime: '2026-08-08 11:26',
    uploader: 'admin',
  },
  {
    id: 'r006',
    name: '计算机视觉导学.mp4',
    type: 'VIDEO',
    dir: 'senior-video',
    sizeText: '256 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-07 15:50',
    uploader: 'admin',
  },
  {
    id: 'r007',
    name: '动画：感知机.mp4',
    type: 'VIDEO',
    dir: 'senior-video',
    sizeText: '188 MB',
    stage: 'SENIOR',
    status: 0,
    updateTime: '2026-08-06 17:12',
    uploader: 'admin',
  },
  {
    id: 'r008',
    name: '小学 AI 启蒙绘本.pdf',
    type: 'PICTURE_BOOK',
    dir: 'primary-low-picture',
    sizeText: '22.3 MB',
    stage: 'PRIMARY_LOW',
    status: 1,
    updateTime: '2026-08-05 10:40',
    uploader: 'admin',
  },
  {
    id: 'r009',
    name: '人脸识别原理.docx',
    type: 'WORD',
    dir: 'senior-doc',
    sizeText: '2.6 MB',
    stage: 'SENIOR',
    status: 1,
    updateTime: '2026-08-04 13:08',
    uploader: 'admin',
  },
  {
    id: 'r010',
    name: '资源上传操作手册.md',
    type: 'DOCUMENT',
    dir: 'common-manual',
    sizeText: '86 KB',
    stage: '',
    status: 1,
    updateTime: '2026-08-03 09:30',
    uploader: 'admin',
  },
];

function formatBytes(bytes: number): string {
  if (!bytes) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
}

function collectDirKeys(node: DirNode, result: string[] = []): string[] {
  result.push(node.key);
  node.children?.forEach((child) => collectDirKeys(child, result));
  return result;
}

function findDirTitle(nodes: DirNode[], key: string): string {
  for (const node of nodes) {
    if (node.key === key) return node.title;
    if (node.children) {
      const child = findDirTitle(node.children, key);
      if (child) return child;
    }
  }
  return '全部资源';
}

function typeIcon(type: string) {
  const size = 16;
  switch (type) {
    case 'VIDEO':
      return <Film size={size} />;
    case 'PPT':
      return <Presentation size={size} />;
    case 'WORD':
      return <FileText size={size} />;
    case 'IMAGE':
      return <Image size={size} />;
    case 'PICTURE_BOOK':
      return <BookOpen size={size} />;
    default:
      return <FileText size={size} />;
  }
}

export default function ResourceManagement() {
  const { message } = App.useApp();
  const [dirs, setDirs] = useState<DirNode[]>(MOCK_DIRS);
  const [files, setFiles] = useState<ResourceFile[]>(MOCK_FILES);
  const [selectedDir, setSelectedDir] = useState('root');
  const [nameKeyword, setNameKeyword] = useState('');
  const [typeKeyword, setTypeKeyword] = useState<string | undefined>(undefined);
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [dirModalOpen, setDirModalOpen] = useState(false);
  const [moveModalOpen, setMoveModalOpen] = useState(false);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [dirForm] = Form.useForm();
  const [moveForm] = Form.useForm();
  const [uploadForm] = Form.useForm();
  const [uploadFileList, setUploadFileList] = useState<UploadFile[]>([]);

  const allDirKeys = useMemo(() => collectDirKeys({ key: 'root', title: '全部资源', children: dirs[0]?.children }), [dirs]);
  const scopedDirKeys = useMemo(() => {
    const findNode = (nodes: DirNode[], key: string): DirNode | null => {
      for (const node of nodes) {
        if (node.key === key) return node;
        if (node.children) {
          const found = findNode(node.children, key);
          if (found) return found;
        }
      }
      return null;
    };
    const node = findNode(dirs, selectedDir);
    return node ? collectDirKeys(node) : [selectedDir];
  }, [dirs, selectedDir]);

  const filteredFiles = useMemo(() => {
    return files.filter((file) => {
      const inDir = selectedDir === 'root' || scopedDirKeys.includes(file.dir);
      const nameMatched = !nameKeyword || file.name.toLowerCase().includes(nameKeyword.toLowerCase());
      const typeMatched = !typeKeyword || file.type === typeKeyword;
      return inDir && nameMatched && typeMatched;
    });
  }, [files, nameKeyword, scopedDirKeys, selectedDir, typeKeyword]);

  const dirOptions = useMemo(() => {
    const flat: { label: string; value: string }[] = [];
    const walk = (nodes: DirNode[], prefix = '') => {
      nodes.forEach((node) => {
        const label = prefix ? `${prefix} / ${node.title}` : node.title;
        flat.push({ label, value: node.key });
        if (node.children) walk(node.children, label);
      });
    };
    walk(dirs);
    return flat;
  }, [dirs]);

  const currentDirTitle = findDirTitle(dirs, selectedDir);

  const handleReset = () => {
    setNameKeyword('');
    setTypeKeyword(undefined);
    setSelectedDir('root');
  };

  const openCreateDir = () => {
    dirForm.resetFields();
    dirForm.setFieldsValue({ parentDir: selectedDir });
    setDirModalOpen(true);
  };

  const handleCreateDir = async () => {
    try {
      const values = await dirForm.validateFields();
      const key = `dir-${Date.now()}`;
      const addNode = (nodes: DirNode[], parentKey: string): DirNode[] => {
        return nodes.map((node) => {
          if (node.key === parentKey) {
            return {
              ...node,
              children: [...(node.children ?? []), { key, title: values.dirName }],
            };
          }
          return node.children ? { ...node, children: addNode(node.children, parentKey) } : node;
        });
      };
      setDirs((prev) => addNode(prev, values.parentDir));
      setDirModalOpen(false);
      message.success(`目录「${values.dirName}」已创建`);
    } catch {
      // 表单校验失败时不关闭弹窗
    }
  };

  const openMove = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先在文件列表中选择要转移的文件');
      return;
    }
    moveForm.resetFields();
    moveForm.setFieldsValue({ targetDir: selectedDir });
    setMoveModalOpen(true);
  };

  const handleMove = async () => {
    try {
      const values = await moveForm.validateFields();
      setFiles((prev) => prev.map((file) => (
        selectedRowKeys.includes(file.id) ? { ...file, dir: values.targetDir } : file
      )));
      setMoveModalOpen(false);
      setSelectedRowKeys([]);
      message.success(`已转移 ${selectedRowKeys.length} 个文件`);
    } catch {
      // 表单校验失败时不关闭弹窗
    }
  };

  const openUpload = () => {
    uploadForm.resetFields();
    uploadForm.setFieldsValue({ dir: selectedDir });
    setUploadFileList([]);
    setUploadModalOpen(true);
  };

  const handleUpload = async () => {
    try {
      const values = await uploadForm.validateFields();
      const file = uploadFileList[0]?.originFileObj as File | undefined;
      const newFile: ResourceFile = {
        id: `r-${Date.now()}`,
        name: values.fileName || file?.name || '未命名资源',
        type: values.fileType,
        dir: values.dir,
        sizeText: file ? formatBytes(file.size) : '0 KB',
        stage: values.stage ?? '',
        status: 1,
        updateTime: '2026-08-13 20:00',
        uploader: 'admin',
      };
      setFiles((prev) => [newFile, ...prev]);
      setUploadModalOpen(false);
      setUploadFileList([]);
      message.success(`资源「${newFile.name}」已上传`);
    } catch {
      // 表单校验失败时不关闭弹窗
    }
  };

  const columns: TableProps<ResourceFile>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
      render: (_, record) => (
        <span className={styles.typeCell}>
          {typeIcon(record.type)}
          <span>{record.name}</span>
        </span>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 110,
      render: (_, record) => (
        <StatusTag status={record.type} statusMap={RESOURCE_TYPE_MAP} />
      ),
    },
    {
      title: '大小',
      dataIndex: 'sizeText',
      key: 'sizeText',
      width: 110,
    },
    {
      title: '所属目录',
      dataIndex: 'dir',
      key: 'dir',
      width: 170,
      ellipsis: true,
      render: (_, record) => findDirTitle(dirs, record.dir),
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (_, record) => <StageTag stage={record.stage ?? ''} />,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (_, record) => (
        <StatusTag status={String(record.status)} statusMap={RESOURCE_STATUS_MAP} />
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 130,
      render: () => (
        <Space size="small">
          <Button type="link" size="small" icon={<Eye size={13} />} onClick={() => message.info('静态演示：预览')}>
            预览
          </Button>
          <Button type="link" size="small" icon={<Download size={13} />} onClick={() => message.info('静态演示：下载')}>
            下载
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <div className={styles.toolbar}>
        <div className={styles.toolbarLeft}>
          <Input
            value={nameKeyword}
            onChange={(e) => setNameKeyword(e.target.value)}
            placeholder="资源名称"
            allowClear
            prefix={<Search size={14} />}
            className={styles.nameInput}
          />
          <Select
            value={typeKeyword}
            onChange={setTypeKeyword}
            placeholder="文件类型"
            allowClear
            options={RESOURCE_TYPE_OPTIONS}
            className={styles.typeSelect}
          />
          <Button type="primary" icon={<Search size={14} />} onClick={() => message.success('筛选已生效')}>
            查询
          </Button>
          <Button icon={<RotateCcw size={14} />} onClick={handleReset}>
            重置
          </Button>
        </div>
        <div className={styles.toolbarRight}>
          <Button icon={<FolderPlus size={14} />} onClick={openCreateDir}>
            新建目录
          </Button>
          <Button icon={<Move size={14} />} onClick={openMove}>
            转移文件
          </Button>
          <Button type="primary" icon={<UploadIcon size={14} />} onClick={openUpload}>
            上传资源
          </Button>
        </div>
      </div>

      <div className={styles.body}>
        <aside className={styles.treePanel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>目录结构</span>
            <span className={styles.panelMeta}>{allDirKeys.length - 1} 个目录</span>
          </div>
          <div className={styles.treeContent}>
            <Tree.DirectoryTree
              treeData={dirs}
              selectedKeys={[selectedDir]}
              onSelect={(keys) => {
                if (keys.length > 0) setSelectedDir(String(keys[0]));
              }}
              defaultExpandAll
              showIcon
            />
          </div>
        </aside>

        <section className={styles.filePanel}>
          <div className={styles.fileHeader}>
            <div className={styles.fileHeaderLeft}>
              <span className={styles.panelTitle}>文件列表</span>
              <span className={styles.panelMeta}>{filteredFiles.length} 个文件</span>
            </div>
            <div className={styles.fileHeaderRight}>
              <FolderOpen size={14} />
              <span>当前目录：{currentDirTitle}</span>
            </div>
          </div>
          <div className={styles.tableWrap}>
            <Table<ResourceFile>
              columns={columns}
              dataSource={filteredFiles}
              rowKey="id"
              pagination={false}
              rowSelection={{
                selectedRowKeys,
                onChange: (keys) => setSelectedRowKeys(keys),
              }}
              scroll={{ y: 'calc(100vh - 320px)' }}
            />
          </div>
        </section>
      </div>

      <Modal
        title="新建目录"
        open={dirModalOpen}
        onCancel={() => setDirModalOpen(false)}
        onOk={() => void handleCreateDir()}
      >
        <Form form={dirForm} layout="vertical">
          <Form.Item label="目录名称" name="dirName" rules={[{ required: true, message: '请输入目录名称' }]}>
            <Input placeholder="例如：机器学习课件" maxLength={50} />
          </Form.Item>
          <Form.Item label="上级目录" name="parentDir" rules={[{ required: true, message: '请选择上级目录' }]}>
            <Select options={dirOptions} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="转移文件"
        open={moveModalOpen}
        onCancel={() => setMoveModalOpen(false)}
        onOk={() => void handleMove()}
      >
        <p className={styles.modalTip}>已选择 {selectedRowKeys.length} 个文件</p>
        <Form form={moveForm} layout="vertical">
          <Form.Item label="目标目录" name="targetDir" rules={[{ required: true, message: '请选择目标目录' }]}>
            <Select options={dirOptions} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="上传资源"
        open={uploadModalOpen}
        onCancel={() => setUploadModalOpen(false)}
        onOk={() => void handleUpload()}
        width={560}
      >
        <Form form={uploadForm} layout="vertical">
          <Form.Item
            label="选择文件"
            required
            extra="静态演示阶段仅记录文件信息，不会真正上传"
          >
            <Upload.Dragger
              multiple={false}
              fileList={uploadFileList}
              beforeUpload={() => false}
              onChange={({ fileList }) => setUploadFileList(fileList)}
            >
              <p className={styles.uploadHint}>
                <UploadIcon size={28} />
              </p>
              <p className={styles.uploadText}>点击或拖拽文件到此处</p>
            </Upload.Dragger>
          </Form.Item>
          <Form.Item label="资源名称" name="fileName">
            <Input placeholder="留空则使用文件名" maxLength={100} />
          </Form.Item>
          <Form.Item label="文件类型" name="fileType" rules={[{ required: true, message: '请选择文件类型' }]}>
            <Select options={RESOURCE_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item label="所属目录" name="dir" rules={[{ required: true, message: '请选择所属目录' }]}>
            <Select options={dirOptions} />
          </Form.Item>
          <Form.Item label="学段" name="stage">
            <Select options={STAGE_OPTIONS} allowClear placeholder="通用资料可不选" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
