import Message from "../Message"
import { useMessageStore } from "@/stores/messageStore"

let worker = null;
let messageStore = null;

// 检查是否显示连接提示
const wsCheck = () => {
    return import.meta.env.VITE_WS_CHECK === "true";
}

// 初始化WebSocket
const initWs = async () => {
    // 获取消息存储
    messageStore = useMessageStore();

    // 创建Worker
    worker = new Worker(new URL('./websocket.worker.js', import.meta.url), {
        type: 'module'
    });
    // 初始化Worker连接
    const token = localStorage.getItem("token");
    const baseUrl = import.meta.env.VITE_WS;
    const wsUrl = baseUrl + token;

    worker.postMessage({
        type: 'init',
        data: { wsUrl }
    });

    // 监听Worker消息
    worker.onmessage = (event) => {
        const { type, data, message, retryCount, delay } = event.data;
        console.log("收到workder消息", type);
        switch (type) {
            case 'message':
                // 使用原有的消息处理方法
                if (messageStore && messageStore.onMessage) {
                    messageStore.onMessage(data);
                }
                break;
            case 'reconnecting':
                console.log(`第${retryCount}次重连，等待${delay / 1000}秒`);
                if (wsCheck()) {
                    Message.warning(`连接断开第${retryCount}次重连中...`);
                }
                break;
        }
    };

    worker.onerror = (error) => {
        console.error('Web Worker错误:', error);
    };
}

// 关闭连接
const closeWebSocket = () => {
    if (worker) {
        worker.postMessage({
            type: 'close'
        });
        worker.terminate();
        worker = null;
    }
}

// 导出其他方法
export {
    initWs,
    closeWebSocket,
}