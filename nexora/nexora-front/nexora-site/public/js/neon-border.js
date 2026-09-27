// Neon Border — 移植自 参考项目/k12bk/8.txt（Originkit NeonBorder）
// 两团光带沿圆角矩形边框巡游（3 层辉光 + 2 层锐边），conic-gradient 由 --arc 驱动。
// 用法：宿主元素加 data-neon 属性；颜色/速度可在下方 OPTS 调整。
"use strict";
(function () {
  const OPTS = {
    color: "#a06bff",
    thickness: 2,
    borderSize: 30,
    glow: 100,
    movement: "continuous",
    speed: 16,
  };

  const EDGE_COPIES = 2;
  const GLOW_LAYERS = [
    { blur: 8, opacity: 0.5, reach: 0.3 },
    { blur: 15, opacity: 0.3, reach: 0.6 },
    { blur: 57, opacity: 0.18, reach: 1 },
  ];
  const MAX_GLOW_BLUR = Math.max(...GLOW_LAYERS.map((l) => l.blur));
  const MAX_GLOW_REACH = 36;

  const SLOWEST_CYCLE = 30;
  const FASTEST_CYCLE = 4;
  const GLIDE_EASE = [0.65, 0, 0.35, 1];
  const glideEase = makeEaseFn(GLIDE_EASE);

  function makeEaseFn(pts) {
    const [x1, y1, x2, y2] = pts;
    const bez = (a, b, t) => {
      const u = 1 - t;
      return 3 * u * u * t * a + 3 * u * t * t * b + t * t * t;
    };
    return (t) => {
      const x = Math.max(0, Math.min(1, t));
      let s = x;
      for (let i = 0; i < 8; i++) {
        const cx = bez(x1, x2, s) - x;
        const u = 1 - s;
        const dx = 3 * u * u * x1 + 6 * u * s * (x2 - x1) + 3 * s * s * (1 - x2);
        if (Math.abs(dx) < 1e-6) break;
        s -= cx / dx;
        s = Math.max(0, Math.min(1, s));
      }
      return bez(y1, y2, s);
    };
  }

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
    return `rgba(0,0,0,${a})`;
  }

  function perimeterPoint(u, w, h) {
    const d = (((u % 1) + 1) % 1) * 2 * (w + h);
    if (d < w) return [d, 0];
    if (d < w + h) return [w, d - w];
    if (d < w * 2 + h) return [w - (d - w - h), h];
    return [0, h - (d - w * 2 - h)];
  }

  function cornerLap(k, w, h) {
    const p = 2 * (w + h);
    const at = [0, w / p, (w + h) / p, (w * 2 + h) / p];
    return Math.floor(k / 4) + at[((k % 4) + 4) % 4];
  }

  function perimeterAngle(u, w, h) {
    const [x, y] = perimeterPoint(u, w, h);
    return (Math.atan2(x - w / 2, h / 2 - y) * 180) / Math.PI;
  }

  const ARC_SAMPLES = 24;
  const MIN_ARC = 0.015;

  function buildArc(lap, lengthPct, w, h, color) {
    const fw = w > 0 ? w : 100;
    const fh = h > 0 ? h : 100;
    const len = Math.max(0, Math.min(100, lengthPct));
    const span = Math.max(MIN_ARC, (len / 100) * 0.5);
    const solidT = len / 100;

    const stops = [];
    let base = 0;
    let prev = 0;
    let acc = 0;

    for (let i = 0; i <= ARC_SAMPLES; i++) {
      const f = i / ARC_SAMPLES;
      const angle = perimeterAngle(lap + (f - 0.5) * span, fw, fh);
      if (i === 0) {
        base = angle;
      } else {
        let d = angle - prev;
        while (d > 180) d -= 360;
        while (d < -180) d += 360;
        acc += d;
      }
      prev = angle;

      const t = Math.abs(f - 0.5) * 2;
      const k = solidT >= 1 ? 1 : t <= solidT ? 1 : 1 - (t - solidT) / (1 - solidT);
      stops.push(`${withAlpha(color, k * k * (3 - 2 * k))} ${acc.toFixed(2)}deg`);
    }

    const end = acc.toFixed(2);
    stops.push(`${withAlpha(color, 0)} ${end}deg`);
    stops.push(`${withAlpha(color, 0)} 360deg`);

    return `conic-gradient(from ${base.toFixed(2)}deg at 50% 50%, ${stops.join(", ")})`;
  }

  const BAND_MASK_CSS =
    "-webkit-mask-image:linear-gradient(#fff 0 0), linear-gradient(#fff 0 0);" +
    "-webkit-mask-clip:content-box, border-box;-webkit-mask-composite:xor;" +
    "mask-image:linear-gradient(#fff 0 0), linear-gradient(#fff 0 0);" +
    "mask-clip:content-box, border-box;mask-composite:exclude;";

  function initNeon(host) {
    const groups = [];
    const groupArcs = ["", ""];

    function buildGroup(startLap) {
      const group = document.createElement("div");
      group.style.cssText =
        "position:absolute;inset:0;overflow:visible;pointer-events:none;z-index:-1;";

      const amount = Math.max(0, Math.min(100, OPTS.glow)) / 100;
      const glowOuter = 10 + MAX_GLOW_REACH + MAX_GLOW_BLUR * 2;

      const band = (r, offset, radius) => {
        const div = document.createElement("div");
        div.style.cssText =
          `position:absolute;inset:${offset - r}px;box-sizing:border-box;padding:${r}px;` +
          `border-radius:${radius > 0 ? radius + r : 0}px;background:var(--arc);` +
          BAND_MASK_CSS;
        return div;
      };

      if (amount > 0) {
        for (const l of GLOW_LAYERS) {
          const r = OPTS.thickness + amount * MAX_GLOW_REACH * l.reach;
          const layer = document.createElement("div");
          layer.style.cssText =
            `position:absolute;inset:-${glowOuter}px;box-sizing:border-box;padding:${glowOuter}px;` +
            `border-radius:${glowOuter}px;opacity:${l.opacity};mix-blend-mode:plus-lighter;` +
            `filter:blur(${l.blur.toFixed(1)}px);` +
            BAND_MASK_CSS;
          layer.appendChild(band(r, glowOuter, 0));
          group.appendChild(layer);
        }
      }
      for (let i = 0; i < EDGE_COPIES; i++) {
        const edge = document.createElement("div");
        edge.style.cssText = "position:absolute;inset:0;mix-blend-mode:plus-lighter;";
        edge.appendChild(band(OPTS.thickness, 0, 0));
        group.appendChild(edge);
      }
      return group;
    }

    const sizeRef = { w: 0, h: 0 };
    let radius = 24;

    const readSize = () => {
      const r = host.getBoundingClientRect();
      if (r.width === sizeRef.w && r.height === sizeRef.h) return;
      sizeRef.w = r.width;
      sizeRef.h = r.height;
      const br = parseFloat(getComputedStyle(host).borderTopLeftRadius);
      radius = Math.max(0, Math.min(100, Number.isFinite(br) ? br : 24));
      for (const g of groups) {
        g.style.borderRadius = `${radius}px`;
      }
    };

    const ro = new ResizeObserver(() => {
      readSize();
      for (const g of groups) {
        g.style.setProperty("--arc", buildArc(0, OPTS.borderSize, sizeRef.w, sizeRef.h, OPTS.color));
      }
    });
    ro.observe(host);
    readSize();

    for (const start of [0, 0.5]) {
      const g = buildGroup(start);
      g.style.setProperty("--arc", buildArc(start, OPTS.borderSize, sizeRef.w, sizeRef.h, OPTS.color));
      host.appendChild(g);
      groups.push(g);
    }
    if (!sizeRef.w) readSize();

    let raf = 0;
    let last = performance.now();
    let stepT = 0;
    let corner = 0;
    let inView = true;
    let pageHidden = document.hidden;

    const wake = () => {
      if (inView && !pageHidden && !raf) {
        last = performance.now();
        raf = requestAnimationFrame(frame);
      }
    };
    new IntersectionObserver((es) => { inView = es[0].isIntersecting; wake(); }).observe(host);
    document.addEventListener("visibilitychange", () => { pageHidden = document.hidden; wake(); });

    const frame = (now) => {
      const dt = Math.min(0.05, Math.max(0, (now - last) / 1000));
      last = now;
      const s = Math.max(0, Math.min(20, OPTS.speed));

      const beat = (SLOWEST_CYCLE + ((FASTEST_CYCLE - SLOWEST_CYCLE) * (s - 1)) / 19) / 4;
      stepT += dt / beat;
      while (stepT >= 1) { stepT -= 1; corner += 1; }
      const eased = glideEase(Math.min(1, stepT));

      const fw = sizeRef.w > 0 ? sizeRef.w : 100;
      const fh = sizeRef.h > 0 ? sizeRef.h : 100;
      const from = cornerLap(corner, fw, fh);
      const to = cornerLap(corner + 1, fw, fh);
      const cur = from + (to - from) * eased;

      if (groups[0]) groups[0].style.setProperty("--arc", buildArc(cur, OPTS.borderSize, fw, fh, OPTS.color));
      if (groups[1]) groups[1].style.setProperty("--arc", buildArc(cur + 0.5, OPTS.borderSize, fw, fh, OPTS.color));

      if (inView && !pageHidden) {
        raf = requestAnimationFrame(frame);
      } else {
        raf = 0;
      }
    };
    raf = requestAnimationFrame(frame);
  }

  for (const host of document.querySelectorAll("[data-neon]")) {
    initNeon(host);
  }
  window.NexoraFx = Object.assign(window.NexoraFx || {}, { initNeon });
})();
