/**
 * WebSocket 工具（单例）
 *
 * 通过 Vite 代理 /ws → ws://localhost:6062，前端统一连 `ws://localhost:3000/ws?token=xxx`。
 * 内置心跳（30s ping）、断线重连（最多 3 次，间隔 5s）、消息类型分发。
 */

type MessageHandler = (data: any) => void;
type CloseHandler = (event: CloseEvent) => void;

const HEARTBEAT_INTERVAL = 30_000;
const MAX_RECONNECT = 3;
const RECONNECT_DELAY = 5_000;

class WebSocketManager {
  private ws: WebSocket | null = null;
  private token: string | null = null;
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private reconnectCount = 0;
  private manualClose = false;

  private messageHandlers = new Map<string, Set<MessageHandler>>();
  private closeHandlers = new Set<CloseHandler>();

  /** 建立 WebSocket 连接 */
  connect(token: string): void {
    this.token = token;
    this.manualClose = false;
    this.reconnectCount = 0;
    this.doConnect();
  }

  private doConnect(): void {
    if (!this.token) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const url = `${protocol}//${host}/ws?token=${encodeURIComponent(this.token)}`;

    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      this.reconnectCount = 0;
      this.startHeartbeat();
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

  /** 发送消息 */
  send(message: any): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const payload = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(payload);
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

  private tryReconnect(): void {
    if (this.reconnectCount >= MAX_RECONNECT) return;
    this.reconnectCount++;
    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      this.doConnect();
    }, RECONNECT_DELAY);
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
