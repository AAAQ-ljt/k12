let ws = null;
//最大重试次数
let maxRetries = 5;
//重试间隔
let retryInterval = 2000;
//是否正在重连
let isConnecting = false;
//已重试次数
let retryCount = 0;
//心跳间隔
let HEARTBEAT_INTERVAL = 5000;
//心跳轮训
let heartbeatTimer = null;
//wsURL地址
let wsUrl = '';
//是否需要重连
let needReconnect = true;
//重连timer
let reconnectTimer = null;

// 连接WebSocket
const connectWs = () => {
    if (isConnecting || !needReconnect) {
        return
    };
    isConnecting = true;
    console.log(`尝试连接... (重试次数: ${retryCount}/${maxRetries}), ws: ${wsUrl}`);
    try {
        ws = new WebSocket(wsUrl);
        ws.onopen = () => {
            isConnecting = false;
            retryCount = 0;
            console.log("ws连接成功");
            // 启动心跳
            startHeartbeat();
        };

        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            self.postMessage({
                type: 'message',
                data: data
            });
        };

        ws.onerror = (error) => {
            handleReconnect();
        };

        ws.onclose = (event) => {
            isConnecting = false;
            // 清理心跳
            clearHeartbeat();
            if (event.code !== 1000) {
                handleReconnect();
            }
        };
    } catch (error) {
        console.error("ws连接失败", error)
        handleReconnect();
    }
};

// 启动心跳
const startHeartbeat = () => {
    clearHeartbeat();
    heartbeatTimer = setInterval(() => {
        if (ws?.readyState === WebSocket.OPEN) {
            ws.send('ping');
        }
    }, HEARTBEAT_INTERVAL);
};

// 清理心跳
const clearHeartbeat = () => {
    if (heartbeatTimer) {
        clearInterval(heartbeatTimer);
        heartbeatTimer = null;
    }
};

// 重连处理
const handleReconnect = () => {
    if (!needReconnect) {
        return;
    }

    if (isConnecting) {
        return;
    }

    if (retryCount >= maxRetries) {
        console.log("已达到最大重试次数，停止重试");
        retryCount = 0;
        return;
    }

    retryCount += 1;
    isConnecting = false;

    // 指数退避策略
    const delay = retryInterval * Math.pow(1.5, retryCount - 1);
    // 通知主线程重连信息
    self.postMessage({
        type: 'reconnecting',
        retryCount: retryCount,
        delay: delay,
        message: `等待 ${delay / 1000} 秒后重试...`
    });
    // 清除之前的重连定时器
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
    }
    reconnectTimer = setTimeout(() => {
        connectWs();
    }, delay);
};


// 关闭连接
const closeWs = () => {
    needReconnect = false;
    isConnecting = false;
    clearHeartbeat();
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
        ws.close();
        ws = null;
    }
    console.log("ws连接已关闭");
};

// 监听主线程消息
self.onmessage = function (e) {
    const { type, data } = e.data;
    switch (type) {
        case 'init':
            wsUrl = data.wsUrl;
            connectWs();
            break;
        case 'close':
            closeWs();
            break;
    }
};