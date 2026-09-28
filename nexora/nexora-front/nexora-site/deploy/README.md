# Nexora 官网部署（nexora-site）

> 纯静态站，无后端。链路：公网 `http://121.40.149.155/`（阿里云 ECS 80，frp）
> → 内网虚拟机 nginx `:8090` → `/opt/nexora/www/site`。
> 首次部署：2026-09-27。

## 日常更新（3 条命令）

开发机 Git Bash（连接信息与工具见 `F:/AIworker_program/school_frp_connnect/README.md`）：

```bash
cd <仓库>/nexora/nexora-front/nexora-site
npm run build
tar czf /tmp/nexora-site-dist.tar.gz -C dist .

cd F:/AIworker_program/school_frp_connnect
export NX_HOST=121.40.149.155 NX_PORT=30022 NX_USER=jsj NX_PWD='<密码>' MSYS_NO_PATHCONV=1

python 02-ssh-tools/nx_put.py "$(cygpath -m /tmp/nexora-site-dist.tar.gz)" /tmp/nexora-site-dist.tar.gz
NX_SUDO=1 python 02-ssh-tools/nx_ssh.py "sudo -S -p '' bash /tmp/deploy_site.sh"
```

> 注意：`MSYS_NO_PATHCONV=1` 会同时关掉**本地**路径的自动转换，所以本地包路径要用
> `cygpath -m` 转成 Windows 形式，只有远端路径保持 `/tmp/...`。

## deploy_site.sh 做什么（幂等，可重复执行）

1. 校验 `/tmp/nexora-site-dist.tar.gz` 存在、端口 8090 空闲；
2. 解压到 `/opt/nexora/www/site`（`nexora:nexora`，a+rX）；
3. 写 `/etc/nginx/sites-available/nexora-site`（listen 8090，`/_astro/` 一年 immutable、
   `/js/` 一天缓存、gzip），`nginx -t` 通过才 reload；
4. `/etc/frp/frpc.toml` 幂等追加 `site` 代理（公网 80 → 本机 8090；首次追加前备份为
   `frpc.toml.bak`）；
5. 延迟 1 秒重启 frpc（脚本先退出，避免砍掉自己的 SSH 通道）。

## 验收

```bash
curl -sI http://121.40.149.155/            # 200，text/html
curl -sI http://121.40.149.155/_astro/xxx.css   # 200 + immutable
curl -sI http://121.40.149.155:30080/      # 学生端回归 200
curl -sI http://121.40.149.155:30081/      # 管理端回归 200
journalctl -u frpc -n 10 --no-pager        # [site] start proxy success
```

## 服务器上的现状

| 项 | 值 |
|---|---|
| 站点根目录 | `/opt/nexora/www/site`（约 490 KB） |
| nginx 站点 | `/etc/nginx/sites-enabled/nexora-site`（listen 8090） |
| frp 代理 | `frpc.toml` 追加 `[[proxies]] name="site"`（公网 80 → 8090） |
| 内网端口 | 80=学生端 / 8080=管理端 / **8090=官网** |

## 绑定正式域名（后续可选）

当前公网入口是 ECS IP（HTTP）。若要像学生端/管理端一样走 Cloudflare 拿 HTTPS
（例如 `www.liyekai.dpdns.org`），在 DNS/CF 配好后把三处地址一起换成 `https://<域名>`
并重新构建部署：`astro.config.mjs` 的 `site`、`src/config/site.ts` 的 `origin`、
`public/robots.txt` 的 Sitemap 行。页面按钮与 canonical/og:url 会自动跟随。
