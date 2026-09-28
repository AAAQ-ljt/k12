#!/usr/bin/env bash
# =============================================================================
# deploy_site.sh - Nexora 产品官网（Astro 纯静态）部署到内网虚拟机
# =============================================================================
# 在内网虚拟机（jsj-virtual-machine）上以 root 执行。标准跑法（开发机 Git Bash，
# 连接信息与工具见 F:/AIworker_program/school_frp_connnect/README.md）：
#
#   cd <仓库>/nexora/nexora-front/nexora-site && npm run build
#   tar czf /tmp/nexora-site-dist.tar.gz -C dist .
#   python "$TOOLS/nx_put.py" /tmp/nexora-site-dist.tar.gz /tmp/
#   python "$TOOLS/nx_put.py" deploy/deploy_site.sh /tmp/
#   NX_SUDO=1 python "$TOOLS/nx_ssh.py" "sudo -S -p '' bash /tmp/deploy_site.sh"
#
# 幂等：重复执行安全。frpc 重启安排在脚本退出后 1 秒，避免砍掉自己的 SSH 通道。
# 链路：公网 http://121.40.149.155/ (ECS:80, frp) -> 本机 nginx:8090 -> /opt/nexora/www/site
# =============================================================================
set -uo pipefail
log() { echo "=== [$(date +%T)] $* ==="; }

SITE_PKG=/tmp/nexora-site-dist.tar.gz
SITE_ROOT=/opt/nexora/www/site
SITE_PORT=8090
FRPC_CONF=/etc/frp/frpc.toml

# ---- 0) 前置检查 ----
[ -f "$SITE_PKG" ] || { echo "!! 缺少 $SITE_PKG，先用 nx_put.py 上传 dist 打包"; exit 1; }
if ss -tln | grep -q ":${SITE_PORT} "; then
  echo "!! 端口 ${SITE_PORT} 已被占用，先确认占用方再调整 SITE_PORT"; exit 1
fi

# ---- 1) 解压 dist（先解到 .tmp 校验，再换目录） ----
log "解压官网 dist"
rm -rf "${SITE_ROOT}.tmp"
mkdir -p "${SITE_ROOT}.tmp"
tar xzf "$SITE_PKG" -C "${SITE_ROOT}.tmp" || { echo "!! 解压失败"; exit 1; }
[ -f "${SITE_ROOT}.tmp/index.html" ] || { echo "!! 包内无 index.html"; exit 1; }
rm -rf "$SITE_ROOT"
mv "${SITE_ROOT}.tmp" "$SITE_ROOT"
chown -R nexora:nexora "$SITE_ROOT"
chmod -R a+rX "$SITE_ROOT"
echo "  $(du -sh "$SITE_ROOT" | cut -f1) -> $SITE_ROOT"

# ---- 2) nginx 站点（8090） ----
log "写 nginx 站点配置（nexora-site，端口 ${SITE_PORT}）"
cat > /etc/nginx/sites-available/nexora-site <<'EOF'
# 产品官网（Astro 纯静态，无后端）：frp 公网 80 -> 本机 8090
server {
    listen 8090;
    server_name _;

    root /opt/nexora/www/site;
    index index.html;

    gzip on;
    gzip_comp_level 5;
    gzip_min_length 1024;
    gzip_types text/css application/javascript application/json image/svg+xml text/plain;

    # Astro 带内容哈希的构建产物：长缓存
    location /_astro/ {
        add_header Cache-Control "public, max-age=31536000, immutable";
    }

    # public/js 特效引擎（文件名不带哈希）：短缓存，更新次日生效
    location /js/ {
        add_header Cache-Control "public, max-age=86400";
    }

    location / {
        try_files $uri $uri/ $uri/index.html =404;
    }
}
EOF
ln -sf /etc/nginx/sites-available/nexora-site /etc/nginx/sites-enabled/nexora-site
nginx -t || { echo "!! nginx -t 失败，未加载新配置"; exit 1; }
systemctl reload nginx
echo "  nginx: $(systemctl is-active nginx)"

# ---- 3) frp：公网 80 -> 本机 8090（幂等追加） ----
log "追加 frp 映射（公网 80 -> 本机 ${SITE_PORT}）"
if grep -Eq 'name *= *"site"' "$FRPC_CONF"; then
  echo "  frpc.toml 已含 site 代理，跳过追加"
else
  cp -n "$FRPC_CONF" "$FRPC_CONF.bak" || true
  cat >> "$FRPC_CONF" <<'EOF'

# ---------------------------------------------------------------------------
# 产品官网（Astro 纯静态，nginx 端口 8090）
# 公网入口： http://121.40.149.155/ （ECS 80 端口；2026-09-27 实测 REFUSED=安全组放行无监听）
# ---------------------------------------------------------------------------
[[proxies]]
name       = "site"
type       = "tcp"
localIP    = "127.0.0.1"
localPort  = 8090
remotePort = 80
EOF
  echo "  已追加（原配置备份在 ${FRPC_CONF}.bak）"
fi

# ---- 4) 重启 frpc（延迟 1 秒，脚本先退出保住 SSH 通道） ----
log "调度 frpc 重启（1 秒后执行）"
nohup bash -c "sleep 1; systemctl restart frpc" >/tmp/frpc-restart.log 2>&1 &
log "完成。约 5 秒后公网可访问 http://121.40.149.155/ ，验收： curl -sI http://121.40.149.155/"
echo "DEPLOY_SITE_DONE"
