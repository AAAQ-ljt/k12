// Dot Cursor — 移植自 参考项目/k12bk/9.txt（Originkit DotCursor）
// 全站自定义光标：光点头 + 缎带拖尾；悬停链接/按钮放大成环。
// 仅精确指针设备启用（触屏自动跳过）；页签隐藏时暂停渲染。
"use strict";
(function () {
  if (typeof window === "undefined") return;
  if (!window.matchMedia || !window.matchMedia("(pointer: fine)").matches) return;

  const FOLLOW_TAU = 0.01;
  const SNAPPINESS = 10;
  const BORDER_WIDTH = 2;
  const HOVER_SCALE = 3.3;

  const P = {
    headColor: "#c9b8ff",
    trailColor: "#7c5cff",
    size: 20,
    trailLength: 10,
    trailThickness: 6,
  };

  function withAlpha(input, alpha) {
    const a = Math.max(0, Math.min(1, alpha));
    const s = String(input).trim();
    const hex = s.match(/^#([0-9a-f]{3,8})$/i);
    if (hex) {
      let h = hex[1];
      if (h.length === 3 || h.length === 4) h = h.split("").map((c) => c + c).join("");
      const n = parseInt(h.slice(0, 6), 16);
      if (!Number.isFinite(n)) return `rgba(0,0,0,${a})`;
      return `rgba(${(n >> 16) & 255},${(n >> 8) & 255},${n & 255},${a})`;
    }
    const rgb = s.match(/^rgba?\(([^)]+)\)/i);
    if (rgb) {
      const parts = rgb[1].split(",").map((v) => parseFloat(v));
      if (parts.length >= 3 && parts.slice(0, 3).every(Number.isFinite)) {
        return `rgba(${parts[0]},${parts[1]},${parts[2]},${a})`;
      }
    }
    return `rgba(0,0,0,${a})`;
  }

  const RING_SELECTOR = "a,button,[role=\"button\"],.ss__tab,.marquee__pill";

  const canvas = document.createElement("canvas");
  canvas.style.cssText =
    "position:fixed;inset:0;width:100vw;height:100vh;display:block;pointer-events:none;z-index:100000;opacity:0;";
  document.body.appendChild(canvas);
  const ctx = canvas.getContext("2d");
  if (!ctx) { canvas.remove(); return; }

  let w = 1;
  let h = 1;
  const resize = () => {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    w = Math.max(1, window.innerWidth);
    h = Math.max(1, window.innerHeight);
    canvas.width = Math.max(1, Math.floor(w * dpr));
    canvas.height = Math.max(1, Math.floor(h * dpr));
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  };
  resize();
  window.addEventListener("resize", resize);

  const previousCursor = document.documentElement.style.cursor;
  let cursorHidden = false;
  const hideNativeCursor = (hide) => {
    if (hide === cursorHidden) return;
    cursorHidden = hide;
    document.documentElement.style.cursor = hide ? "none" : previousCursor;
  };

  let ballX = 0;
  let ballY = 0;
  let targetX = 0;
  let targetY = 0;

  let radius = P.size / 2;
  let fillOpacity = 1;
  let strokeOpacity = 0;

  let points = [];

  let hitDirty = true;
  let overRing = false;

  let seeded = false;
  let inside = false;

  let hitX = 0;
  let hitY = 0;

  const seedAt = (x, y) => {
    seeded = true;
    targetX = x;
    targetY = y;
    ballX = x;
    ballY = y;
    canvas.style.opacity = "1";
    points = [];
  };

  const exitTo = (x, y) => {
    if (seeded && inside) {
      const gapX = x - ballX;
      const gapY = y - ballY;
      const gap = Math.hypot(gapX, gapY);
      if (gap > 1) {
        const steps = Math.min(32, Math.ceil(gap / 8));
        for (let i = 1; i <= steps; i++) {
          const t = i / steps;
          points.push({ x: ballX + gapX * t, y: ballY + gapY * t, age: 0 });
        }
      }
      ballX = x;
      ballY = y;
      targetX = x;
      targetY = y;
    }
    inside = false;
    hitDirty = false;
    hideNativeCursor(false);
  };

  const onMove = (e) => {
    if (e.pointerType && e.pointerType !== "mouse") return;
    targetX = e.clientX;
    targetY = e.clientY;
    hitX = e.clientX;
    hitY = e.clientY;
    if (!seeded) seedAt(e.clientX, e.clientY);
    inside = true;
    hitDirty = true;
    hideNativeCursor(true);
  };

  window.addEventListener("pointermove", onMove, { passive: true });
  document.documentElement.addEventListener("pointerleave", () => {
    if (inside) exitTo(targetX, targetY);
  });

  let raf = 0;
  let last = performance.now();
  let pageHidden = document.hidden;

  document.addEventListener("visibilitychange", () => {
    pageHidden = document.hidden;
    if (!pageHidden && !raf) {
      last = performance.now();
      raf = requestAnimationFrame(frame);
    }
  });

  const frame = (now) => {
    const dt = Math.min(0.05, Math.max(0.001, (now - last) / 1000));
    last = now;

    ctx.clearRect(0, 0, w, h);

    if (!seeded || pageHidden) {
      raf = pageHidden ? 0 : requestAnimationFrame(frame);
      return;
    }

    const followEase = 1 - Math.exp(-dt / FOLLOW_TAU);
    ballX += (targetX - ballX) * followEase;
    ballY += (targetY - ballY) * followEase;

    if (hitDirty) {
      const el = document.elementFromPoint(hitX, hitY);
      overRing = !!el?.closest(RING_SELECTOR);
      hitDirty = false;
    }

    const trailMs = P.trailLength * 40;

    if (!overRing) {
      if (inside) points.push({ x: ballX, y: ballY, age: 0 });
      for (const pt of points) pt.age += dt * 1000;
      points = points.filter((pt) => pt.age < trailMs);

      const n = points.length;
      if (n > 1) {
        const maxHalf = Math.max(0.5, (P.trailThickness / 20) * P.size / 2);

        const lx = [];
        const ly = [];
        const rx = [];
        const ry = [];

        let nx = 0;
        let ny = 0;
        for (let i = 0; i < n; i++) {
          const prev = points[Math.max(0, i - 1)];
          const next = points[Math.min(n - 1, i + 1)];
          const dx = next.x - prev.x;
          const dy = next.y - prev.y;
          const len = Math.hypot(dx, dy);
          if (len > 0.0001) {
            nx = -dy / len;
            ny = dx / len;
          }
          const t = Math.max(0, Math.min(1, 1 - points[i].age / trailMs));
          const half = maxHalf * t;
          lx.push(points[i].x + nx * half);
          ly.push(points[i].y + ny * half);
          rx.push(points[i].x - nx * half);
          ry.push(points[i].y - ny * half);
        }

        ctx.beginPath();
        ctx.moveTo(lx[0], ly[0]);
        for (let i = 1; i < n; i++) ctx.lineTo(lx[i], ly[i]);
        for (let i = n - 1; i >= 0; i--) ctx.lineTo(rx[i], ry[i]);
        ctx.closePath();
        ctx.fillStyle = withAlpha(P.trailColor, 0.38);
        ctx.fill();
      }
    } else {
      points = [];
    }

    const ease = 1 - Math.exp(-dt * (SNAPPINESS * 1.5));
    const targetRadius = overRing ? (P.size * HOVER_SCALE) / 2 : P.size / 2;
    radius += (targetRadius - radius) * ease;
    fillOpacity += ((overRing ? 0 : 1) - fillOpacity) * ease;
    strokeOpacity += ((overRing ? 1 : 0) - strokeOpacity) * ease;

    if (!inside) {
      raf = requestAnimationFrame(frame);
      return;
    }

    ctx.beginPath();
    ctx.arc(ballX, ballY, Math.max(0.5, radius), 0, Math.PI * 2);
    if (strokeOpacity > 0.01) {
      ctx.strokeStyle = withAlpha(P.headColor, strokeOpacity);
      ctx.lineWidth = BORDER_WIDTH;
      ctx.stroke();
    }
    if (fillOpacity > 0.01) {
      ctx.fillStyle = withAlpha(P.headColor, fillOpacity);
      ctx.fill();
    }

    raf = requestAnimationFrame(frame);
  };
  raf = requestAnimationFrame(frame);
})();
