// Radial Reveal — 主按钮悬停时从指针位置圆形扩散反色（简化自 参考项目/k12bk/7.txt）
"use strict";
(function () {
  if (!window.matchMedia || !window.matchMedia("(pointer: fine)").matches) return;

  const EASE = "cubic-bezier(0.22, 0.61, 0.36, 1)";

  const pct = (btn, e) => {
    const r = btn.getBoundingClientRect();
    return {
      x: Math.round(((e.clientX - r.left) / r.width) * 100),
      y: Math.round(((e.clientY - r.top) / r.height) * 100),
    };
  };

  for (const btn of document.querySelectorAll(".btn--primary")) {
    btn.classList.add("btn--radial");
    const overlay = document.createElement("span");
    overlay.className = "btn__reveal";
    overlay.innerHTML = btn.innerHTML;
    btn.appendChild(overlay);

    btn.addEventListener("pointerenter", (e) => {
      const { x, y } = pct(btn, e);
      overlay.animate(
          [
            { clipPath: `circle(0% at ${x}% ${y}%)` },
            { clipPath: `circle(140% at ${x}% ${y}%)` },
          ],
          { duration: 420, easing: EASE, fill: "forwards" },
      );
    });
    btn.addEventListener("pointerleave", (e) => {
      const { x, y } = pct(btn, e);
      overlay.animate(
          [
            { clipPath: `circle(140% at ${x}% ${y}%)` },
            { clipPath: `circle(0% at ${x}% ${y}%)` },
          ],
          { duration: 360, easing: EASE, fill: "forwards" },
      );
    });
  }
})();
