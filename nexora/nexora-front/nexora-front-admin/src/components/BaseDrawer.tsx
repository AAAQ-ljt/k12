import { Button, Drawer, Space } from 'antd';
import type { FormInstance } from 'antd';
import type { CSSProperties, ReactNode } from 'react';

export interface BaseDrawerProps {
  /** 自定义抽屉类名（用于覆盖 antd Drawer 样式） */
  className?: string;
  /** 是否显示 */
  open: boolean;
  /** 标题 */
  title?: ReactNode;
  /** 宽度（默认 520，与 BaseDialog 保持一致） */
  width?: number | string;
  /** 抽屉方向（默认 right） */
  placement?: 'left' | 'right' | 'top' | 'bottom';
  /** 是否显示取消按钮（link 样式，默认 true） */
  showCancel?: boolean;
  /** 取消按钮文案 */
  cancelText?: string;
  /** 确认按钮文案 */
  okText?: string;
  /** 是否展示右上角关闭按钮（默认 true） */
  showClose?: boolean;
  /** 点击遮罩是否关闭（默认 true） */
  maskClosable?: boolean;
  /** 内容区域 padding */
  contentPadding?: number | string;
  /** 内容区域额外样式（可覆盖默认 maxHeight/padding） */
  bodyStyle?: CSSProperties;
  /** 确认按钮 loading */
  loading?: boolean;
  /** 关闭回调 */
  onClose: () => void;
  /** 确认回调 */
  onOk?: () => void | Promise<void>;
  /** 自定义底部按钮区（传入后 okText/loading 不生效） */
  footer?: ReactNode;
  /** 表单实例：关闭时自动 resetFields */
  form?: FormInstance;
  /** 打开/关闭动画结束回调 */
  afterOpenChange?: (open: boolean) => void;
  /** 内容（插槽） */
  children: ReactNode;
}

/**
 * 通用抽屉组件，与 BaseDialog 保持同一套设计约定：
 * - 受控模式：open + onClose
 * - 默认从右侧滑出，宽度 520
 * - 底部按钮区：确认 + link 样式取消（可配置）
 * - 内容区超过可视高度时内部滚动
 * - 关闭时自动重置传入的表单
 */
export default function BaseDrawer({
  className,
  open,
  title,
  width = 520,
  placement = 'right',
  showCancel = true,
  cancelText = '取消',
  okText = '确定',
  showClose = true,
  maskClosable = true,
  contentPadding = 24,
  bodyStyle,
  loading = false,
  onClose,
  onOk,
  footer,
  form,
  afterOpenChange,
  children,
}: BaseDrawerProps) {
  const handleClose = () => {
    form?.resetFields();
    onClose();
  };

  return (
    <Drawer
      className={className}
      open={open}
      title={title}
      width={width}
      placement={placement}
      closable={showClose}
      maskClosable={maskClosable}
      onClose={handleClose}
      afterOpenChange={afterOpenChange}
      footer={
        footer === undefined ? (
          <Space>
            {showCancel && (
              <Button type="link" onClick={handleClose}>
                {cancelText}
              </Button>
            )}
            <Button type="primary" loading={loading} onClick={onOk}>
              {okText}
            </Button>
          </Space>
        ) : (
          footer
        )
      }
      styles={{
        body: {
          padding: contentPadding,
          overflowY: 'auto',
          ...bodyStyle,
        },
        footer: {
          padding: '12px 24px',
          borderTop: '1px solid #f0f0f0',
        },
      }}
    >
      {children}
    </Drawer>
  );
}
