"use strict";

// ===== 配置（相当于原来组件的 props 合并结果） =====
// 如需换参数，直接改这里即可。
const PRESET = {
  background: "#070812",
  color: "#FFFFFF",
  colors: ["#7c5cff", "#f093fb"],
  colorSpread: 8,
  columns: 72,
  rows: 28,
  thickness: 3,
  speed: 38,
  spin: 15,
  distance: 34,
  camera: { perspective: 19, cameraHeight: 20 },
  hole: { depth: 300, layers: 2, falloff: 7 },
  wave: { wave: 300, ripple: 366 },
  interaction: { clickPulse: 160, hoverSpeed: 160, transition: { duration: 0.6 } },
};

const MAX_DPR = 2;
const MAX_COLORS = 8;
const SHIFT0 = 3;
const NEAR = 1;
const FAR = 1000;
const SPEED_REFERENCE = 50;
const MAX_DT = 0.05;
const LOOK_AT = [0, 0, 1];

function clamp(v, lo, hi) { return v < lo ? lo : v > hi ? hi : v; }

// ===== 颜色解析（修复 #5：支持命名颜色、百分比） =====
const NAMED = {
  black: [0, 0, 0], white: [1, 1, 1], red: [1, 0, 0], lime: [0, 1, 0],
  green: [0, 0.5, 0], blue: [0, 0, 1], yellow: [1, 1, 0], cyan: [0, 1, 1],
  aqua: [0, 1, 1], magenta: [1, 0, 1], fuchsia: [1, 0, 1], gray: [0.5, 0.5, 0.5],
  grey: [0.5, 0.5, 0.5], silver: [0.75, 0.75, 0.75], maroon: [0.5, 0, 0],
  olive: [0.5, 0.5, 0], navy: [0, 0, 0.5], teal: [0, 0.5, 0.5],
  purple: [0.5, 0, 0.5], orange: [1, 0.647, 0], pink: [1, 0.753, 0.796],
};

function parseHslComponent(v) {
  const t = String(v == null ? "" : v).trim();
  if (!t) return NaN;
  const num = parseFloat(t);
  if (t.endsWith("%")) return num / 100;
  return num > 1 ? num / 100 : num;
}

function parseColor(input, fallback) {
  if (input == null || input === "") return fallback;
  const s = String(input).trim().toLowerCase();
  if (!s) return fallback;

  if (s[0] === "#") {
    const hex = s.slice(1);
    const short = hex.length === 3 || hex.length === 4;
    const long = hex.length === 6 || hex.length === 8;
    if (!short && !long) return fallback;
    const grab = (i) => short
      ? parseInt(hex[i] + hex[i], 16)
      : parseInt(hex.slice(i * 2, i * 2 + 2), 16);
    const r = grab(0), g = grab(1), b = grab(2);
    if ([r, g, b].some(Number.isNaN)) return fallback;
    return [r / 255, g / 255, b / 255];
  }

  if (NAMED[s]) return NAMED[s].slice();

  const rgb = s.match(/rgba?\(([^)]+)\)/);
  if (rgb) {
    const p = rgb[1].split(/[,\s/]+/).filter(Boolean).map((v) => {
      const t = v.trim();
      // rgb() 里的百分比是相对 255 的，例如 rgb(100%, 0%, 0%)
      return t.endsWith("%") ? (parseFloat(t) / 100) * 255 : parseFloat(t);
    });
    if (p.length >= 3 && p.slice(0, 3).every((v) => !Number.isNaN(v))) {
      return [p[0] / 255, p[1] / 255, p[2] / 255];
    }
  }

  const hsl = s.match(/hsla?\(([^)]+)\)/);
  if (hsl) {
    const parts = hsl[1].split(/[,\s/]+/).filter(Boolean);
    const h = parseFloat(parts[0]);
    const sat = parseHslComponent(parts[1]);
    const li = parseHslComponent(parts[2]);
    if (![h, sat, li].some(Number.isNaN)) {
      const hh = ((h % 360) + 360) % 360 / 360;
      const s2 = clamp(sat, 0, 1);
      const l2 = clamp(li, 0, 1);
      const c = (1 - Math.abs(2 * l2 - 1)) * s2;
      const x = c * (1 - Math.abs(((hh * 6) % 2) - 1));
      const m = l2 - c / 2;
      const seg = Math.floor(hh * 6) % 6;
      const t = seg === 0 ? [c, x, 0]
        : seg === 1 ? [x, c, 0]
        : seg === 2 ? [0, c, x]
        : seg === 3 ? [0, x, c]
        : seg === 4 ? [x, 0, c]
        : [c, 0, x];
      return [t[0] + m, t[1] + m, t[2] + m];
    }
  }

  return fallback;
}

// ===== 矩阵 / 几何工具（与原逻辑一致） =====
function perspective(fovDeg, aspect) {
  const f = 1 / Math.tan((fovDeg * Math.PI) / 360);
  const nf = 1 / (NEAR - FAR);
  const m = new Float32Array(16);
  m[0] = f / aspect;
  m[5] = f;
  m[10] = (FAR + NEAR) * nf;
  m[11] = -1;
  m[14] = 2 * FAR * NEAR * nf;
  return m;
}

function lookAt(eye, center, up) {
  let zx = eye[0] - center[0];
  let zy = eye[1] - center[1];
  let zz = eye[2] - center[2];
  let len = Math.hypot(zx, zy, zz) || 1;
  zx /= len; zy /= len; zz /= len;

  let xx = up[1] * zz - up[2] * zy;
  let xy = up[2] * zx - up[0] * zz;
  let xz = up[0] * zy - up[1] * zx;
  len = Math.hypot(xx, xy, xz) || 1;
  xx /= len; xy /= len; xz /= len;

  const yx = zy * xz - zz * xy;
  const yy = zz * xx - zx * xz;
  const yz = zx * xy - zy * xx;

  const m = new Float32Array(16);
  m[0] = xx; m[1] = yx; m[2] = zx;
  m[4] = xy; m[5] = yy; m[6] = zy;
  m[8] = xz; m[9] = yz; m[10] = zz;
  m[12] = -(xx * eye[0] + xy * eye[1] + xz * eye[2]);
  m[13] = -(yx * eye[0] + yy * eye[1] + yz * eye[2]);
  m[14] = -(zx * eye[0] + zy * eye[1] + zz * eye[2]);
  m[15] = 1;
  return m;
}

function multiply(a, b) {
  const o = new Float32Array(16);
  for (let c = 0; c < 4; c++) {
    for (let r = 0; r < 4; r++) {
      o[c * 4 + r] =
        a[r] * b[c * 4] +
        a[4 + r] * b[c * 4 + 1] +
        a[8 + r] * b[c * 4 + 2] +
        a[12 + r] * b[c * 4 + 3];
    }
  }
  return o;
}

function rotationY(rad) {
  const c = Math.cos(rad);
  const s = Math.sin(rad);
  const m = new Float32Array(16);
  m[0] = c; m[2] = -s;
  m[5] = 1;
  m[8] = s; m[10] = c;
  m[15] = 1;
  return m;
}

function unitBox() {
  const position = [];
  const uv = [];
  const index = [];
  const faces = [
    { n: [1, 0, 0], u: [0, 0, -1], v: [0, 1, 0] },
    { n: [-1, 0, 0], u: [0, 0, 1], v: [0, 1, 0] },
    { n: [0, 1, 0], u: [1, 0, 0], v: [0, 0, 1] },
    { n: [0, -1, 0], u: [1, 0, 0], v: [0, 0, -1] },
    { n: [0, 0, 1], u: [1, 0, 0], v: [0, 1, 0] },
    { n: [0, 0, -1], u: [-1, 0, 0], v: [0, 1, 0] },
  ];
  for (const f of faces) {
    const base = position.length / 3;
    for (const [su, sv] of [[-1, -1], [1, -1], [1, 1], [-1, 1]]) {
      position.push(
        (f.n[0] + su * f.u[0] + sv * f.v[0]) * 0.5,
        (f.n[1] + su * f.u[1] + sv * f.v[1]) * 0.5,
        (f.n[2] + su * f.u[2] + sv * f.v[2]) * 0.5
      );
      uv.push((su + 1) * 0.5, (sv + 1) * 0.5);
    }
    index.push(base, base + 1, base + 2, base, base + 2, base + 3);
  }
  return {
    position: new Float32Array(position),
    uv: new Float32Array(uv),
    index: new Uint16Array(index),
  };
}

function instanceData(rows, columns, layers) {
  const out = new Float32Array(rows * columns * layers * 3);
  let n = 0;
  for (let i = 0; i < rows; i++) {
    for (let j = 0; j < layers; j++) {
      for (let k = 0; k < columns; k++) {
        out[n++] = i;
        out[n++] = k;
        out[n++] = j;
      }
    }
  }
  return out;
}

// ===== Shader（与原版完全一致） =====
const VERT = `#version 300 es
precision highp float;

in vec3 aPosition;
in vec2 aUv;
in vec3 aRcl;

uniform mat4 uProjection;
uniform mat4 uModelView;
uniform float uArc;
uniform float uShift;
uniform float uPhase;
uniform float uSeam;
uniform float uDepth;
uniform float uFalloff;
uniform float uWave;
uniform float uRipple;
uniform vec3 uColors[8];
uniform int uColorCount;
uniform float uColorSpread;

out vec2 vUv;
out vec3 vColor;

void main() {
    float radius = uShift;
    float zShift = 0.0;
    int row = int(aRcl.x + 0.5);
    for (int i = 0; i < row; i++) {
        radius += radius * uArc;
        zShift += radius * uArc;
    }

    vec4 p = vec4(aPosition, 1.0);

    if (p.z > 0.0) radius += radius * uArc;

    p.xz *= radius * uArc;

    p.z += zShift + uShift - uSeam;

    float c = aRcl.y * uRipple;
    float wave = sin(c / 5.3) * 1.1 + sin(c / 1.3) * 1.5 + cos(c / 1.7) * 2.5;
    wave *= uWave;

    float t = uFalloff - aRcl.x + abs(wave) + uPhase;
    t += aRcl.z * abs(sin(aRcl.y));
    t = max(t, 0.0);
    p.y -= t * t * t * uDepth + aRcl.z;

    float a = aRcl.y * uArc;
    float sn = sin(a);
    float cs = cos(a);
    p.xz = p.xz * mat2(cs, -sn, sn, cs);

    float span = uColorSpread > 0.0
        ? mod(aRcl.x, uColorSpread) / uColorSpread * float(uColorCount)
        : 0.0;
    float idxF = floor(span);
    float fracT = span - idxF;
    int i0 = int(mod(idxF, float(uColorCount)));
    int i1 = int(mod(idxF + 1.0, float(uColorCount)));
    vec3 c0 = uColors[0];
    vec3 c1 = uColors[0];
    for (int k = 0; k < 8; k++) {
        if (k == i0) c0 = uColors[k];
        if (k == i1) c1 = uColors[k];
    }
    vColor = mix(c0, c1, fracT);

    vUv = aUv;
    gl_Position = uProjection * uModelView * p;
}
`;

const FRAG = `#version 300 es
precision highp float;

in vec2 vUv;
in vec3 vColor;

uniform vec3 uBackground;
uniform float uThickness;

out vec4 fragColor;

void main() {
    float d = min(min(vUv.x, 1.0 - vUv.x), min(vUv.y, 1.0 - vUv.y));
    float aa = max(fwidth(d), 1e-5);
    float frame = 1.0 - smoothstep(uThickness - aa, uThickness + aa, d);

    fragColor = vec4(mix(uBackground, vColor, frame), 1.0);
}
`;

// ===== 初始化与渲染循环 =====
function init(root) {
  const canvas = document.createElement("canvas");
  root.appendChild(canvas);

  const gl = canvas.getContext("webgl2", {
    antialias: true,
    // 修复 #3：画面整体不透明，直接声明 alpha:false，去掉误导性的透明设置
    alpha: false,
  });
  if (!gl) {
    /* WebGL2 不可用：静默降级，保留底层极光背景 */
    return () => {};
  }

  const compile = (type, src) => {
    const sh = gl.createShader(type);
    gl.shaderSource(sh, src);
    gl.compileShader(sh);
    if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) {
      console.error("InfiniteHole shader:", gl.getShaderInfoLog(sh));
      return null;
    }
    return sh;
  };

  const vs = compile(gl.VERTEX_SHADER, VERT);
  const fs = compile(gl.FRAGMENT_SHADER, FRAG);
  if (!vs || !fs) return () => {};

  const program = gl.createProgram();
  gl.attachShader(program, vs);
  gl.attachShader(program, fs);
  gl.linkProgram(program);
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    console.error("InfiniteHole link:", gl.getProgramInfoLog(program));
    return () => {};
  }
  gl.useProgram(program);

  const U = {
    projection: gl.getUniformLocation(program, "uProjection"),
    modelView: gl.getUniformLocation(program, "uModelView"),
    arc: gl.getUniformLocation(program, "uArc"),
    shift: gl.getUniformLocation(program, "uShift"),
    phase: gl.getUniformLocation(program, "uPhase"),
    seam: gl.getUniformLocation(program, "uSeam"),
    depth: gl.getUniformLocation(program, "uDepth"),
    falloff: gl.getUniformLocation(program, "uFalloff"),
    wave: gl.getUniformLocation(program, "uWave"),
    ripple: gl.getUniformLocation(program, "uRipple"),
    colors: gl.getUniformLocation(program, "uColors[0]"),
    colorCount: gl.getUniformLocation(program, "uColorCount"),
    colorSpread: gl.getUniformLocation(program, "uColorSpread"),
    background: gl.getUniformLocation(program, "uBackground"),
    thickness: gl.getUniformLocation(program, "uThickness"),
  };

  const box = unitBox();
  const vao = gl.createVertexArray();
  gl.bindVertexArray(vao);

  const posBuf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, posBuf);
  gl.bufferData(gl.ARRAY_BUFFER, box.position, gl.STATIC_DRAW);
  const aPosition = gl.getAttribLocation(program, "aPosition");
  gl.enableVertexAttribArray(aPosition);
  gl.vertexAttribPointer(aPosition, 3, gl.FLOAT, false, 0, 0);

  const uvBuf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, uvBuf);
  gl.bufferData(gl.ARRAY_BUFFER, box.uv, gl.STATIC_DRAW);
  const aUv = gl.getAttribLocation(program, "aUv");
  gl.enableVertexAttribArray(aUv);
  gl.vertexAttribPointer(aUv, 2, gl.FLOAT, false, 0, 0);

  const rclBuf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, rclBuf);
  const aRcl = gl.getAttribLocation(program, "aRcl");
  gl.enableVertexAttribArray(aRcl);
  gl.vertexAttribPointer(aRcl, 3, gl.FLOAT, false, 0, 0);
  gl.vertexAttribDivisor(aRcl, 1);

  const idxBuf = gl.createBuffer();
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, idxBuf);
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, box.index, gl.STATIC_DRAW);

  // 几何只构建一次（静态配置，不需要每次重建）
  // 移动端降密度（Nexora 增补）：小屏减少实例数，保证流畅
  const small = window.innerWidth < 760;
  const cols = Math.max(3, Math.round(small ? 48 : PRESET.columns));
  const rws = Math.max(1, Math.round(small ? 20 : PRESET.rows));
  const lyrs = Math.max(1, Math.round(PRESET.hole.layers));
  gl.bindBuffer(gl.ARRAY_BUFFER, rclBuf);
  gl.bufferData(gl.ARRAY_BUFFER, instanceData(rws, cols, lyrs), gl.STATIC_DRAW);
  const instances = rws * cols * lyrs;

  // 颜色只解析一次
  const stops = [PRESET.color, ...(Array.isArray(PRESET.colors) ? PRESET.colors : [])].slice(0, MAX_COLORS);
  const colorCount = Math.max(1, stops.length);
  const colorsFlat = new Float32Array(MAX_COLORS * 3);
  for (let i = 0; i < MAX_COLORS; i++) {
    const c = parseColor(stops[i % colorCount], [1, 1, 1]);
    colorsFlat[i * 3] = c[0];
    colorsFlat[i * 3 + 1] = c[1];
    colorsFlat[i * 3 + 2] = c[2];
  }
  const bg = parseColor(PRESET.background, [0, 0, 0]);

  gl.enable(gl.DEPTH_TEST);
  gl.clearColor(bg[0], bg[1], bg[2], 1);

  let cssW = 0;
  let cssH = 0;
  const resize = () => {
    const w = canvas.clientWidth || 1;
    const h = canvas.clientHeight || 1;
    const dpr = Math.min(window.devicePixelRatio || 1, MAX_DPR);
    const bw = Math.max(1, Math.round(w * dpr));
    const bh = Math.max(1, Math.round(h * dpr));
    if (canvas.width !== bw || canvas.height !== bh) {
      canvas.width = bw;
      canvas.height = bh;
    }
    cssW = w;
    cssH = h;
    gl.viewport(0, 0, bw, bh);
  };
  resize();

  const ro = new ResizeObserver(resize);
  ro.observe(canvas);

  let hoverTarget = 0;
  let hoverAmt = 0;
  let pulse = 0;
  let cycles = 0;
  let angle = 0;

  root.addEventListener("pointerenter", () => { hoverTarget = 1; });
  root.addEventListener("pointerleave", () => { hoverTarget = 0; });
  root.addEventListener("pointerdown", () => { pulse = 1; });

  let raf = 0;
  let last = performance.now();
  // 性能守护：离开视口或页签隐藏时暂停渲染（Nexora 增补）
  let inView = true;
  let pageHidden = document.hidden;

  const tick = (now) => {
    const dt = clamp((now - last) / 1000, 0, MAX_DT);
    last = now;

    const transDur =
      typeof PRESET.interaction.transition?.duration === "number" &&
      PRESET.interaction.transition.duration > 0
        ? PRESET.interaction.transition.duration
        : 0.4;
    hoverAmt += (hoverTarget - hoverAmt) * (1 - Math.exp(-dt / (transDur * 0.5)));
    pulse *= Math.exp(-dt / (transDur * 0.6));
    if (pulse < 0.001) pulse = 0;

    const hoverSpeedFactor = 1 + hoverAmt * (clamp(PRESET.interaction.hoverSpeed, 0, 400) / 100 - 1);
    const clickPulseFactor = 1 + pulse * (clamp(PRESET.interaction.clickPulse, 0, 400) / 100 - 1);

    cycles = (cycles + dt * ((PRESET.speed * hoverSpeedFactor) / SPEED_REFERENCE)) % 1;
    angle = (angle + dt * ((PRESET.spin * Math.PI) / 180)) % (Math.PI * 2);

    const arc = (2 * Math.PI) / cols;
    const step = (SHIFT0 * arc) / (1 + arc); // 修复 #6：step 只在这一处计算
    const shift = SHIFT0 - cycles * step;

    const eye = [0, PRESET.camera.cameraHeight, PRESET.distance - hoverAmt * (PRESET.distance * 0.15)];
    const view = lookAt(eye, LOOK_AT, [0, 1, 0]);
    const modelView = multiply(view, rotationY(angle));
    const proj = perspective(
      clamp(PRESET.camera.perspective, 5, 175),
      Math.max(cssW, 1) / Math.max(cssH, 1)
    );

    gl.useProgram(program);
    gl.bindVertexArray(vao);
    gl.uniformMatrix4fv(U.projection, false, proj);
    gl.uniformMatrix4fv(U.modelView, false, modelView);
    gl.uniform1f(U.arc, arc);
    gl.uniform1f(U.shift, shift);
    gl.uniform1f(U.phase, cycles);
    gl.uniform1f(U.seam, (SHIFT0 - shift) * arc);
    gl.uniform1f(U.depth, (Math.max(0, PRESET.hole.depth) / 100) * clickPulseFactor);
    gl.uniform1f(U.falloff, PRESET.hole.falloff);
    gl.uniform1f(U.wave, (Math.max(0, PRESET.wave.wave) / 100) * clickPulseFactor);
    gl.uniform1f(U.ripple, Math.max(0, PRESET.wave.ripple) / 100);
    gl.uniform3fv(U.colors, colorsFlat);
    gl.uniform1i(U.colorCount, colorCount);
    gl.uniform1f(U.colorSpread, Math.max(0.0001, PRESET.colorSpread));
    gl.uniform3f(U.background, bg[0], bg[1], bg[2]);
    gl.uniform1f(U.thickness, clamp((clamp(PRESET.thickness, 0, 49) / 100) * (1 + pulse * 0.4), 0, 0.5));

    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.drawElementsInstanced(gl.TRIANGLES, box.index.length, gl.UNSIGNED_SHORT, 0, instances);

    if (inView && !pageHidden) raf = requestAnimationFrame(tick); else raf = 0;
  };

  const wake = () => { if (inView && !pageHidden && !raf) { last = performance.now(); raf = requestAnimationFrame(tick); } };
  new IntersectionObserver((es) => { inView = es[0].isIntersecting; wake(); }).observe(root);
  document.addEventListener("visibilitychange", () => { pageHidden = document.hidden; wake(); });

  raf = requestAnimationFrame(tick);

  // 修复 #2：返回清理函数，释放 GL 资源（静态页面下只会在页面卸载时触发）
  return () => {
    cancelAnimationFrame(raf);
    ro.disconnect();
    gl.deleteBuffer(posBuf);
    gl.deleteBuffer(uvBuf);
    gl.deleteBuffer(rclBuf);
    gl.deleteBuffer(idxBuf);
    gl.deleteVertexArray(vao);
    gl.deleteProgram(program);
    gl.deleteShader(vs);
    gl.deleteShader(fs);
  };
}

const holeHost = document.querySelector("[data-hole]");
if (holeHost) {
  const dispose = init(holeHost);
  window.addEventListener("pagehide", dispose, { once: true });
}
