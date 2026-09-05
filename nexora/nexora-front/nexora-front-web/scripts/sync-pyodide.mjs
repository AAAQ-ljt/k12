// 将 node_modules/pyodide（运行时产物：pyodide.asm.mjs / python_stdlib.zip / pyodide-lock.json 等）
// 拷贝到 public/pyodide，作为浏览器端 Python 运行环境的本地 indexURL（离线可用，CDN 仅兜底）。
import { cpSync, existsSync, mkdirSync, rmSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = join(dirname(fileURLToPath(import.meta.url)), '..');
const src = join(projectRoot, 'node_modules', 'pyodide');
const dest = join(projectRoot, 'public', 'pyodide');

if (!existsSync(src)) {
  console.warn('[sync-pyodide] 未找到 node_modules/pyodide，请先执行 npm install');
  process.exit(1);
}

rmSync(dest, { recursive: true, force: true });
mkdirSync(dest, { recursive: true });
cpSync(src, dest, { recursive: true });
console.log(`[sync-pyodide] 已拷贝 pyodide → public/pyodide`);