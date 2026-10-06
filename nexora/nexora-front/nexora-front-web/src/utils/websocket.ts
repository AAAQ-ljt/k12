/**
 * WebSocket 工具（单例）
 *
 * 通过 Vite 代理 /ws → ws://localhost:6062，前端统一连 `ws://ws://host/ws?token=xxx`。
 * 内置心跳、断线重连（无限重试 + 退避）、消息类型分发。
 *
 * 2026-10-07 修复「必须刷新页面才出内容 / 回答整段一起出现」：
 * 1. 心跳 30s → 20s：服务端读超时是 60s（IdleStateHandler(60,0,0)），而标签页切到后台时
 *    浏览器会把定时器节流到分钟级，30s 的心跳很容易迟到 → 服务端判"没有发送心跳"直接断开；
 * 2. 重连由「最多 3 次（15s 内）」改为**无限重试 + 退避**（5/10/20/30s 封顶）：
 *    原来 15 秒内没连上就永久放弃，页面却毫无感知，只能靠刷新重建连接；
 * 3. 新增连接状态回调（onOpen / onClose），页面可据此提示「连接中断，正在重连…」并在
 *    重连成功后补齐断线期间丢失的内容。
 */

type MessageHandler = (data: any) => void;
type CloseHandler = (event: CloseEvent) => void;
type OpenHandler = () => void;

const HEARTBEAT_INTERVAL = 20_000;
/** 重连退避序列（毫秒）：最后一次为封顶值，此后按封顶值无限重试 */
const RECONNECT_DELAYS = [5_000, 10_000, 20_000, 30_000];

class WebSocketManager {
  private ws: WebSocket | null = null;
  private token: string | null = null;
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private reconnectCount = 0;
  private manualClose = false;
  /** 是否已成功建立过连接（用于区分首次连接与断线重连，重连成功才通知页面补数据） */
  private everOpened = false;

  private messageHandlers = new Map<string, Set<MessageHandler>>();
  private closeHandlers = new Set<CloseHandler>();
  private openHandlers = new Set<OpenHandler>();

  /** 建立 WebSocket 连接（幂等：已连接且 token 相同则复用，不重复建连） */
  connect(token: string): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN && this.token === token) {
      return;
    }
    this.disconnect();
    this.token = token;
    this.manualClose = false;
    this.reconnectCount = 0;
    this.everOpened = false;
    this.doConnect();
  }

  private doConnect(): void {
    if (!this.token) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const url = `${protocol}//${host}/ws?token=${encodeURIComponent(this.token)}`;

    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      const reconnected = this.everOpened;
      this.everOpened = true;
      this.reconnectCount = 0;
      this.startHeartbeat();
      if (reconnected) {
        // 断线重连成功：通知页面（补拉历史，避免用户看到半截回答还以为要刷新）
        this.openHandlers.forEach((handler) => handler());
      }
    };

    this.ws.onmessage = (event: MessageEvent) => {
      this.handleMessage(event.data);
    };

    this.ws.onclose = (event: CloseEvent) => {
      this.stopHeartbeat();
      this.closeHandlers.forEach((handler) => handler(event));
      if (!this.manualClose) {
        this.tryReconnect();
      }
    };

    this.ws.onerror = () => {
      // onclose 会在 error 后触发，重连逻辑交给 onclose
    };
  }

  /** 主动关闭连接 */
  disconnect(): void {
    this.manualClose = true;
    this.stopHeartbeat();
    this.clearReconnectTimer();
    const socket = this.ws;
    this.ws = null;
    if (!socket) {
      return;
    }
    if (socket.readyState === WebSocket.CONNECTING) {
      socket.onopen = () => socket.close();
      socket.onerror = null;
      socket.onclose = null;
      socket.onmessage = null;
    } else {
      socket.close();
    }
  }

  /** 发送消息；连接不可用时立即触发一次重连（心跳失败不能静默跳过） */
  send(message: any): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const payload = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(payload);
      return;
    }
    if (!this.manualClose && this.token) {
      this.tryReconnect();
    }
  }

  /** 注册消息回调（按 type 分发，type 为 '*' 表示接收全部） */
  onMessage(type: string, handler: MessageHandler): void;
  onMessage(handler: MessageHandler): void;
  onMessage(typeOrHandler: string | MessageHandler, handler?: MessageHandler): void {
    if (typeof typeOrHandler === 'function') {
      this.onMessage('*', typeOrHandler);
      return;
    }
    const type = typeOrHandler;
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, new Set());
    }
    this.messageHandlers.get(type)!.add(handler!);
  }

  /** 移除消息回调 */
  offMessage(type: string, handler: MessageHandler): void {
    this.messageHandlers.get(type)?.delete(handler);
  }

  /** 注册连接关闭回调 */
  onClose(handler: CloseHandler): void {
    this.closeHandlers.add(handler);
  }

  /** 注销连接关闭回调 */
  offClose(handler: CloseHandler): void {
    this.closeHandlers.delete(handler);
  }

  /** 注册「断线后重连成功」回调（首次连接不触发；页面据此补拉历史） */
  onOpen(handler: OpenHandler): void {
    this.openHandlers.add(handler);
  }

  /** 注销重连成功回调 */
  offOpen(handler: OpenHandler): void {
    this.openHandlers.delete(handler);
  }

  /** 当前连接是否已建立 */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  // ====== 内部方法 ======

  private handleMessage(raw: string): void {
    let data: any;
    try {
      data = JSON.parse(raw);
    } catch {
      data = raw;
    }
    const type = data?.type ?? '*';
    // 按 type 精确分发
    this.messageHandlers.get(type)?.forEach((handler) => handler(data));
    // 通配回调
    this.messageHandlers.get('*')?.forEach((handler) => handler(data));
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'ping' });
    }, HEARTBEAT_INTERVAL);
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 无限重连 + 退避（5/10/20/30s，30s 封顶）。
   *
   * 原实现「最多 3 次、每次 5s」——15 秒内连不上就永久放弃，而页面没有任何提示，
   * 表现为「AI 不回话，必须刷新页面」，这是 2026-10-07 那次反馈的直接原因。
   * 服务端重启（部署）、网络抖动、后台标签页心跳超时都会触发断连，必须重试到底。
   */
  private tryReconnect(): void {
    if (this.manualClose || !this.token) {
      return;
    }
    if (this.reconnectTimer) {
      return;
    }
    const delay = RECONNECT_DELAYS[Math.min(this.reconnectCount, RECONNECT_DELAYS.length - 1)];
    this.reconnectCount++;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.doConnect();
    }, delay);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }
}

export const websocket = new WebSocketManager();
export default websocket;
