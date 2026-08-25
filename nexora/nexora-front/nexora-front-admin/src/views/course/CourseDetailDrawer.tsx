import { useEffect, useMemo, useState } from 'react';
import {
  App,
  Button,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Popconfirm,
  Space,
  Spin,
  Table,
  Tag,
  type TableProps,
} from 'antd';
import { BookOpen, FolderOpen, Link2, Plus, Search, Trash2 } from 'lucide-react';
import BaseDrawer from '@/components/BaseDrawer';
import {
  addChapter,
  addLesson,
  bindLessonResources,
  delChapter,
  delLesson,
  getDetail,
  unbindLessonResource,
  updateChapter,
  updateLesson,
  type CourseChapter,
  type CourseChapterLesson,
  type CourseDetail,
  type CourseInfo,
} from '@/api/course';
import { loadDataList as loadResourceList, type ResourceInfo } from '@/api/resource';
import { getStageOption } from '@/types/common';
import styles from './course-detail.module.scss';

interface CourseDetailDrawerProps {
  open: boolean;
  course?: CourseInfo;
  onClose: () => void;
  onChanged: () => void;
}

interface FormModalState<T> {
  open: boolean;
  mode: 'create' | 'edit';
  record?: T;
}

const RESOURCE_TYPE_MAP: Record<string, { text: string; color: string }> = {
  VIDEO: { text: '视频', color: 'blue' },
  DOCUMENT: { text: '文档', color: 'green' },
  IMAGE: { text: '图片', color: 'purple' },
  PPT: { text: 'PPT', color: 'orange' },
  WORD: { text: 'Word', color: 'cyan' },
  PICTURE_BOOK: { text: '绘本', color: 'magenta' },
  LINK: { text: '链接', color: 'geekblue' },
};

export default function CourseDetailDrawer({
  open,
  course,
  onClose,
  onChanged,
}: CourseDetailDrawerProps) {
  const { message } = App.useApp();
  const [detail, setDetail] = useState<CourseDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedChapterId, setSelectedChapterId] = useState<string>();
  const [selectedLessonId, setSelectedLessonId] = useState<string>();
  const [chapterModal, setChapterModal] = useState<FormModalState<CourseChapter>>({
    open: false,
    mode: 'create',
  });
  const [lessonModal, setLessonModal] = useState<FormModalState<CourseChapterLesson>>({
    open: false,
    mode: 'create',
  });
  const [pickerOpen, setPickerOpen] = useState(false);
  const [resourceRows, setResourceRows] = useState<ResourceInfo[]>([]);
  const [resourceTotal, setResourceTotal] = useState(0);
  const [resourcePage, setResourcePage] = useState(1);
  const [resourceLoading, setResourceLoading] = useState(false);
  const [resourceKeyword, setResourceKeyword] = useState('');
  const [selectedResourceIds, setSelectedResourceIds] = useState<string[]>([]);
  const [chapterForm] = Form.useForm();
  const [lessonForm] = Form.useForm();

  useEffect(() => {
    if (chapterModal.open) {
      chapterForm.setFieldsValue(chapterModal.record || { chapterName: '' });
    }
  }, [chapterModal.open, chapterModal.record, chapterForm]);

  useEffect(() => {
    if (lessonModal.open) {
      lessonForm.setFieldsValue(
        lessonModal.record || { lessonName: '', summary: '', videoDuration: undefined },
      );
    }
  }, [lessonModal.open, lessonModal.record, lessonForm]);

  const loadDetail = async (courseId: string) => {
    setLoading(true);
    try {
      const data = await getDetail(courseId);
      setDetail(data);
      const firstChapter = data.chapters[0];
      setSelectedChapterId(firstChapter?.chapter.chapterId);
      setSelectedLessonId(firstChapter?.lessons[0]?.lesson.lessonId);
    } catch {
      // 请求层已统一提示
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && course?.courseId) {
      setDetail(null);
      setSelectedChapterId(undefined);
      setSelectedLessonId(undefined);
      loadDetail(course.courseId);
    }
  }, [open, course?.courseId]);

  const selectedChapter = useMemo(
    () => detail?.chapters.find((item) => item.chapter.chapterId === selectedChapterId),
    [detail, selectedChapterId],
  );
  const selectedLesson = useMemo(
    () => selectedChapter?.lessons.find((item) => item.lesson.lessonId === selectedLessonId),
    [selectedChapter, selectedLessonId],
  );

  const loadResources = async (page = resourcePage, keyword = resourceKeyword) => {
    setResourceLoading(true);
    try {
      const result = await loadResourceList({
        pageNo: page,
        pageSize: 10,
        status: 1,
        stage: course?.stage || undefined,
        stageIncludeNull: course?.stage ? true : undefined,
        resourceNameFuzzy: keyword || undefined,
      });
      setResourceRows(result.list);
      setResourceTotal(result.totalCount);
      setResourcePage(page);
    } catch {
      // 请求层已统一提示
    } finally {
      setResourceLoading(false);
    }
  };

  const handleChapterSubmit = async (values: Record<string, any>) => {
    if (chapterModal.mode === 'create') {
      await addChapter({ courseId: course?.courseId, chapterName: values.chapterName });
      message.success('新增章节成功');
    } else {
      await updateChapter({
        chapterId: chapterModal.record?.chapterId,
        chapterName: values.chapterName,
      });
      message.success('修改章节成功');
    }
    setChapterModal({ open: false, mode: 'create' });
    chapterForm.resetFields();
    if (course?.courseId) {
      await loadDetail(course.courseId);
    }
    onChanged();
  };

  const handleLessonSubmit = async (values: Record<string, any>) => {
    if (lessonModal.mode === 'create') {
      await addLesson({
        chapterId: selectedChapterId,
        courseId: course?.courseId,
        lessonName: values.lessonName,
        summary: values.summary,
        videoDuration: values.videoDuration,
      });
      message.success('新增课时成功');
    } else {
      await updateLesson({
        lessonId: lessonModal.record?.lessonId,
        lessonName: values.lessonName,
        summary: values.summary,
        videoDuration: values.videoDuration,
      });
      message.success('修改课时成功');
    }
    setLessonModal({ open: false, mode: 'create' });
    lessonForm.resetFields();
    if (course?.courseId) {
      await loadDetail(course.courseId);
    }
    onChanged();
  };

  const handleBindResources = async () => {
    if (!selectedLessonId || selectedResourceIds.length === 0) {
      return;
    }
    try {
      await bindLessonResources(selectedLessonId, selectedResourceIds);
      message.success('资源绑定成功');
      setPickerOpen(false);
      setSelectedResourceIds([]);
      if (course?.courseId) {
        await loadDetail(course.courseId);
      }
      onChanged();
    } catch {
      // 请求层已统一提示
    }
  };

  const resourceColumns: TableProps<ResourceInfo>['columns'] = [
    {
      title: '资源名称',
      dataIndex: 'resourceName',
      key: 'resourceName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'resourceType',
      key: 'resourceType',
      width: 100,
      render: (type: string) => {
        const meta = RESOURCE_TYPE_MAP[type] || { text: type, color: 'default' };
        return <Tag color={meta.color}>{meta.text}</Tag>;
      },
    },
    {
      title: '学段',
      dataIndex: 'stage',
      key: 'stage',
      width: 110,
      render: (stage?: string) => {
        const option = getStageOption(stage);
        return option ? <Tag color={option.color}>{option.label}</Tag> : <Tag>未标记</Tag>;
      },
    },
  ];

  return (
    <BaseDrawer
      open={open}
      title={course ? `${course.courseName} - 章节管理` : '章节管理'}
      width="80%"
      footer={null}
      onClose={onClose}
    >
      {loading || !detail ? (
        <div className={styles.loadingBox}>
          <Spin />
        </div>
      ) : (
        <div className={styles.drawerLayout}>
          <section className={styles.panel}>
            <div className={styles.panelHeader}>
              <span>
                <FolderOpen size={15} />
                章节
              </span>
              <Button
                type="primary"
                size="small"
                icon={<Plus size={13} />}
                onClick={() => setChapterModal({ open: true, mode: 'create' })}
              >
                新增章节
              </Button>
            </div>
            <List
              className={styles.listPanel}
              dataSource={detail.chapters}
              locale={{ emptyText: <Empty description="暂无章节" /> }}
              renderItem={(item) => (
                <List.Item
                  className={selectedChapterId === item.chapter.chapterId ? styles.itemActive : ''}
                  onClick={() => {
                    setSelectedChapterId(item.chapter.chapterId);
                    setSelectedLessonId(item.lessons[0]?.lesson.lessonId);
                  }}
                  actions={[
                    <Button
                      key="edit"
                      type="link"
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        setChapterModal({ open: true, mode: 'edit', record: item.chapter });
                      }}
                    >
                      编辑
                    </Button>,
                    <Popconfirm
                      key="del"
                      title="删除章节会同时删除其课时和资源绑定，确认？"
                      onConfirm={async () => {
                        await delChapter(item.chapter.chapterId);
                        message.success('章节已删除');
                        if (course?.courseId) {
                          await loadDetail(course.courseId);
                        }
                        onChanged();
                      }}
                    >
                      <Button type="link" size="small" danger onClick={(e) => e.stopPropagation()}>
                        删除
                      </Button>
                    </Popconfirm>,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Space size={6}>
                        <BookOpen size={14} />
                        {item.chapter.chapterName}
                      </Space>
                    }
                    description={`${item.lessons.length} 个课时`}
                  />
                </List.Item>
              )}
            />
          </section>

          <section className={styles.panel}>
            <div className={styles.panelHeader}>
              <span>
                <BookOpen size={15} />
                课时
              </span>
              <Button
                type="primary"
                size="small"
                icon={<Plus size={13} />}
                disabled={!selectedChapterId}
                onClick={() => setLessonModal({ open: true, mode: 'create' })}
              >
                新增课时
              </Button>
            </div>
            {!selectedChapter ? (
              <Empty description="请先选择章节" className={styles.emptyBox} />
            ) : (
              <List
                className={styles.listPanel}
                dataSource={selectedChapter.lessons}
                locale={{ emptyText: <Empty description="暂无课时" /> }}
                renderItem={(item) => (
                  <List.Item
                    className={selectedLessonId === item.lesson.lessonId ? styles.itemActive : ''}
                    onClick={() => setSelectedLessonId(item.lesson.lessonId)}
                    actions={[
                      <Button
                        key="edit"
                        type="link"
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          setLessonModal({ open: true, mode: 'edit', record: item.lesson });
                        }}
                      >
                        编辑
                      </Button>,
                      <Popconfirm
                        key="del"
                        title="删除课时会同时删除资源绑定，确认？"
                        onConfirm={async () => {
                          await delLesson(item.lesson.lessonId);
                          message.success('课时已删除');
                          if (course?.courseId) {
                            await loadDetail(course.courseId);
                          }
                          onChanged();
                        }}
                      >
                        <Button type="link" size="small" danger onClick={(e) => e.stopPropagation()}>
                          删除
                        </Button>
                      </Popconfirm>,
                    ]}
                  >
                    <List.Item.Meta
                      title={item.lesson.lessonName}
                      description={item.lesson.summary || '暂无摘要'}
                    />
                  </List.Item>
                )}
              />
            )}
          </section>

          <section className={styles.panel}>
            <div className={styles.panelHeader}>
              <span>
                <Link2 size={15} />
                课时资源
              </span>
              <Button
                size="small"
                icon={<Plus size={13} />}
                disabled={!selectedLessonId}
                onClick={() => {
                  setSelectedResourceIds(selectedLesson?.resources.map((item) => item.resourceId) ?? []);
                  setPickerOpen(true);
                  loadResources(1, '');
                }}
              >
                绑定资源
              </Button>
            </div>
            {!selectedLesson ? (
              <Empty description="请先选择课时" className={styles.emptyBox} />
            ) : selectedLesson.resources.length === 0 ? (
              <Empty description="暂无资源" className={styles.emptyBox} />
            ) : (
              <List
                className={styles.listPanel}
                dataSource={selectedLesson.resources}
                renderItem={(item) => {
                  const meta = RESOURCE_TYPE_MAP[item.resourceType || ''] || {
                    text: item.resourceType || '资源',
                    color: 'default',
                  };
                  return (
                    <List.Item
                      actions={[
                        <Popconfirm
                          key="unbind"
                          title="确认解绑该资源？"
                          onConfirm={async () => {
                            await unbindLessonResource(item.id);
                            message.success('资源已解绑');
                            if (course?.courseId) {
                              await loadDetail(course.courseId);
                            }
                            onChanged();
                          }}
                        >
                          <Button type="link" size="small" danger icon={<Trash2 size={13} />}>
                            解绑
                          </Button>
                        </Popconfirm>,
                      ]}
                    >
                      <List.Item.Meta
                        title={item.resourceName}
                        description={<Tag color={meta.color}>{meta.text}</Tag>}
                      />
                    </List.Item>
                  );
                }}
              />
            )}
          </section>
        </div>
      )}

      <Modal
        title={chapterModal.mode === 'create' ? '新增章节' : '编辑章节'}
        open={chapterModal.open}
        onCancel={() => {
          chapterForm.resetFields();
          setChapterModal({ open: false, mode: 'create' });
        }}
        onOk={() => chapterForm.submit()}
      >
        <Form
          form={chapterForm}
          layout="vertical"
          initialValues={chapterModal.record || { chapterName: '' }}
          onFinish={handleChapterSubmit}
        >
          <Form.Item
            name="chapterName"
            label="章节名称"
            rules={[{ required: true, message: '请输入章节名称' }]}
          >
            <Input placeholder="请输入章节名称" maxLength={100} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={lessonModal.mode === 'create' ? '新增课时' : '编辑课时'}
        open={lessonModal.open}
        onCancel={() => {
          lessonForm.resetFields();
          setLessonModal({ open: false, mode: 'create' });
        }}
        onOk={() => lessonForm.submit()}
      >
        <Form
          form={lessonForm}
          layout="vertical"
          initialValues={lessonModal.record || { lessonName: '', summary: '', videoDuration: undefined }}
          onFinish={handleLessonSubmit}
        >
          <Form.Item
            name="lessonName"
            label="课时名称"
            rules={[{ required: true, message: '请输入课时名称' }]}
          >
            <Input placeholder="请输入课时名称" maxLength={100} />
          </Form.Item>
          <Form.Item name="summary" label="课时摘要">
            <Input.TextArea rows={3} placeholder="请输入课时摘要" maxLength={500} />
          </Form.Item>
          <Form.Item name="videoDuration" label="视频时长（秒）">
            <Input type="number" placeholder="可选" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={course?.stage ? `选择资源（${getStageOption(course.stage)?.label ?? course.stage}）` : '选择资源'}
        open={pickerOpen}
        width={720}
        onCancel={() => setPickerOpen(false)}
        onOk={handleBindResources}
        okText="确认绑定"
        cancelText="取消"
      >
        <Space.Compact style={{ width: '100%', marginBottom: 12 }}>
          <Input
            value={resourceKeyword}
            onChange={(e) => setResourceKeyword(e.target.value)}
            placeholder="搜索资源名称"
            onPressEnter={() => loadResources(1, resourceKeyword)}
            prefix={<Search size={14} />}
          />
          <Button onClick={() => loadResources(1, resourceKeyword)}>查询</Button>
        </Space.Compact>
        <Table<ResourceInfo>
          rowKey="resourceId"
          size="small"
          columns={resourceColumns}
          dataSource={resourceRows}
          loading={resourceLoading}
          pagination={{
            current: resourcePage,
            pageSize: 10,
            total: resourceTotal,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page) => loadResources(page, resourceKeyword),
          }}
          rowSelection={{
            selectedRowKeys: selectedResourceIds,
            onChange: (keys) => setSelectedResourceIds(keys as string[]),
          }}
        />
      </Modal>
    </BaseDrawer>
  );
}
