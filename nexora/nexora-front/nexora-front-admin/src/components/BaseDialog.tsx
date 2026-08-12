import { Modal } from 'antd';
import { Button, Space } from 'antd';
import type { ReactNode } from 'react';

interface BaseDialogProps {
  /** 是否显示 */
  open: boolean;
  /** 标题 */
  title?: ReactNode;
  /** 宽度（百分比或具体值，默认 520） */
  width?: number | string;
  /** 距顶部距离（默认 30） */
  top?: number;
  /** 是否显示取消按钮（link 样式，默认 true） */
  showCancel?: boolean;
  /** 取消按钮文案 */
  cancelText?: string;
  /** 确认按钮文案 */
  okText?: string;
  /** 是否展示右上角关闭按钮（默认 true） */
  showClose?: boolean;
  /** 内容区域 padding */
  contentPadding?: number | string;
  /** 确认按钮 loading */
  loading?: boolean;
  /** 取消回调 */
  onCancel: () => void;
  /** 确认回调 */
  onOk?: () => void | Promise<void>;
  /** 自定义底部按钮区（传入后 okText/loading 不生效） */
  footer?: ReactNode;
  /** 内容（插槽） */
  children: ReactNode;
}

/**
 * 通用弹窗组件（参考 smart-campus BaseDialog 规范）：
 * - 自定义标题 / 宽度 / 距顶部距离
 * - 底部按钮区：确认 + link 样式取消（可配置）
 * - 内容区超过可视高度时内部滚动
 * - 右上角关闭按钮可配置
 */
export default function BaseDialog({
  open,
  title,
  width = 520,
  top = 30,
  showCancel = true,
  cancelText = '取消',
  okText = '确定',
  showClose = true,
  contentPadding = 24,
  loading = false,
  onCancel,
  onOk,
  footer,
  children,
}: BaseDialogProps) {
  return (
    <Modal
      open={open}
      title={title}
      width={width}
      centered={false}
      style={{ top }}
      closable={showClose}
      onCancel={onCancel}
      footer={
        footer ?? (
          <Space>
            {showCancel && (
              <Button type="link" onClick={onCancel}>
                {cancelText}
              </Button>
            )}
            <Button type="primary" loading={loading} onClick={onOk}>
              {okText}
            </Button>
          </Space>
        )
      }
      styles={{
        body: {
          padding: contentPadding,
          maxHeight: `calc(100vh - ${typeof top === 'number' ? top : 30}px - 120px)`,
          overflowY: 'auto',
        },
      }}
    >
      {children}
    </Modal>
  );
}
