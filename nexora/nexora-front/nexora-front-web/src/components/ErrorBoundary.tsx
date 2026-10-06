import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Button } from 'antd';
import { AlertTriangle, RotateCcw } from 'lucide-react';

interface ErrorBoundaryProps {
  children: ReactNode;
  /** 出错时的替代内容标题（不传则用默认文案） */
  title?: string;
  /** 兜底区域高度，默认自适应 */
  minHeight?: number;
}

interface ErrorBoundaryState {
  error: Error | null;
}

/**
 * 局部错误边界：把「单个卡片/组件出错」限制在该区域，不拖垮整页。
 *
 * 背景（2026-10-07）：对话里点击历史卡片时整页被 React Router 的默认错误页接管
 * （NotFoundError: Failed to execute 'removeChild'…）。这类错误来自**第三方组件
 * 改动了 React 管理的 DOM**（本页第三方有 jit-viewer 内含的 Vue 子应用、SvgStepPlayer 注入的
 * SVG 等），React 卸载时 `removeChild` 找不到节点即抛出，且发生在提交阶段——只能在错误边界里兜住。
 * 兜住之后：其余消息仍可正常阅读，点「重试」即可重新挂载这一块。
 */
export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { error: null };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // 保留控制台细节，便于定位第三方触发点
    console.error('[Nexora] 卡片渲染失败：', error, info.componentStack);
  }

  private reset = () => this.setState({ error: null });

  render() {
    const { error } = this.state;
    if (!error) {
      return this.props.children;
    }
    return (
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: 8,
          padding: '10px 12px',
          borderRadius: 10,
          border: '1px solid var(--warm-orange-400)',
          background: 'var(--warm-orange-100)',
          color: 'var(--warm-brown-800)',
          minHeight: this.props.minHeight,
        }}
      >
        <AlertTriangle size={15} style={{ flex: 'none', marginTop: 2 }} />
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{ fontSize: 13, fontWeight: 600 }}>
            {this.props.title || '这一块内容暂时显示不出来'}
          </div>
          <div style={{ fontSize: 12, marginTop: 2, color: 'var(--text-secondary)' }}>
            其余内容不受影响，可点「重试」重新加载这一块。
          </div>
          <Button size="small" type="link" icon={<RotateCcw size={13} />} style={{ paddingLeft: 0 }} onClick={this.reset}>
            重试
          </Button>
        </div>
      </div>
    );
  }
}
