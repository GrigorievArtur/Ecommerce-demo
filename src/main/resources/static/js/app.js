// Progressive enhancement only — every page, form and flow works without JavaScript.
(function () {
    "use strict";

    function on(el, type, handler) {
        if (el) { el.addEventListener(type, handler); }
    }

    /* ---- Mobile navigation ------------------------------------------- */
    var toggle = document.querySelector(".nav-toggle");
    var nav = document.getElementById("primary-nav");
    function closeNav() {
        if (!nav || !toggle) { return; }
        nav.classList.remove("open");
        toggle.setAttribute("aria-expanded", "false");
    }
    on(toggle, "click", function () {
        var open = nav.classList.toggle("open");
        toggle.setAttribute("aria-expanded", open ? "true" : "false");
    });
    on(document, "keydown", function (event) {
        if (event.key === "Escape") { closeNav(); }
    });
    on(document, "click", function (event) {
        if (!nav || !toggle || !nav.classList.contains("open")) { return; }
        if (!nav.contains(event.target) && !toggle.contains(event.target)) { closeNav(); }
    });

    /* ---- Theme toggle (light / dark / system) ------------------------ */
    var themeButton = document.querySelector("[data-theme-toggle]");
    function systemPrefersDark() {
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
    }
    function currentTheme() {
        return document.documentElement.getAttribute("data-theme")
            || (systemPrefersDark() ? "dark" : "light");
    }
    function applyTheme(theme) {
        document.documentElement.setAttribute("data-theme", theme);
        try { localStorage.setItem("theme", theme); } catch (e) { /* private mode */ }
        if (themeButton) {
            themeButton.setAttribute("aria-pressed", theme === "dark" ? "true" : "false");
        }
    }
    if (themeButton) {
        themeButton.setAttribute("aria-pressed", currentTheme() === "dark" ? "true" : "false");
        on(themeButton, "click", function () {
            applyTheme(currentTheme() === "dark" ? "light" : "dark");
        });
    }

    /* ---- Flash messages: dismiss + gentle auto-hide ------------------ */
    document.querySelectorAll("[data-dismiss='alert']").forEach(function (button) {
        on(button, "click", function () {
            var alert = button.closest(".alert");
            if (alert) { alert.remove(); }
        });
    });
    document.querySelectorAll(".alert").forEach(function (el) {
        if (el.getAttribute("role") === "alert") { return; }
        setTimeout(function () {
            if (!el.isConnected) { return; }
            el.style.transition = "opacity .4s ease";
            el.style.opacity = "0";
            setTimeout(function () { el.remove(); }, 400);
        }, 6000);
    });

    /* ---- Payment method: show card fields only for card ------------- */
    var methodInputs = document.querySelectorAll('input[name="method"]');
    var cardFields = document.getElementById("card-fields");
    var redirectNote = document.getElementById("redirect-note");
    function syncPaymentFields() {
        if (!methodInputs.length) { return; }
        var method = document.querySelector('input[name="method"]:checked');
        var isCard = method && method.value === "CARD";
        if (cardFields) { cardFields.hidden = !isCard; }
        if (redirectNote) { redirectNote.hidden = isCard; }
    }
    methodInputs.forEach(function (input) { on(input, "change", syncPaymentFields); });
    syncPaymentFields();

    /* ---- Saved address vs new address at checkout ------------------- */
    var addressRadios = document.querySelectorAll("input[name='shippingPresetId']");
    var newAddressFields = document.getElementById("new-address-fields");
    var saveAddressRow = document.getElementById("save-address-row");
    function usingSavedAddress() {
        var checked = document.querySelector("input[name='shippingPresetId']:checked");
        return checked && checked.value !== "";
    }
    function syncAddressFields() {
        if (!addressRadios.length) { return; }
        var saved = usingSavedAddress();
        if (newAddressFields) { newAddressFields.hidden = saved; }
        if (saveAddressRow) { saveAddressRow.hidden = saved; }
    }
    addressRadios.forEach(function (input) { on(input, "change", syncAddressFields); });
    syncAddressFields();

    /* ---- Product image gallery --------------------------------------- */
    var reduceMotion = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)");
    function reduced() { return Boolean(reduceMotion && reduceMotion.matches); }

    var gallery = document.querySelector("[data-gallery]");
    if (gallery) {
        var mainImage = gallery.querySelector("[data-gallery-main]");
        gallery.querySelectorAll("[data-gallery-thumb]").forEach(function (thumb) {
            on(thumb, "click", function () {
                var url = thumb.getAttribute("data-gallery-thumb");
                if (mainImage && url) {
                    mainImage.src = url;
                    if (window.Motion && !reduced()) {
                        window.Motion.animate(mainImage, { opacity: [0.35, 1] }, {
                            duration: 0.35,
                            ease: [0.22, 1, 0.36, 1]
                        });
                    }
                }
                gallery.querySelectorAll("[data-gallery-thumb]").forEach(function (other) {
                    other.classList.toggle("active", other === thumb);
                    other.setAttribute("aria-pressed", other === thumb ? "true" : "false");
                });
            });
        });
    }

    /* ---- Quantity steppers ------------------------------------------- */
    document.querySelectorAll("[data-stepper]").forEach(function (stepper) {
        var input = stepper.querySelector("input");
        if (!input) { return; }
        stepper.querySelectorAll("[data-step]").forEach(function (button) {
            on(button, "click", function () {
                var step = parseInt(button.getAttribute("data-step"), 10) || 0;
                var value = parseInt(input.value, 10) || 1;
                var min = parseInt(input.getAttribute("min"), 10) || 1;
                var max = parseInt(input.getAttribute("max"), 10) || Number.MAX_SAFE_INTEGER;
                input.value = Math.min(Math.max(value + step, min), max);
                input.dispatchEvent(new Event("change", { bubbles: true }));
            });
        });
    });

    /* ---- Filters: submit on change when JS is available ------------- */
    document.querySelectorAll("[data-auto-submit]").forEach(function (form) {
        form.querySelectorAll("select").forEach(function (select) {
            on(select, "change", function () {
                if (form.requestSubmit) { form.requestSubmit(); } else { form.submit(); }
            });
        });
    });

    /* ---- Prevent double submits (loading state) ---------------------- */
    document.querySelectorAll("form[data-loading]").forEach(function (form) {
        on(form, "submit", function () {
            var button = form.querySelector("button[type='submit']");
            if (button && !button.disabled) {
                button.classList.add("is-loading");
                button.setAttribute("aria-busy", "true");
                setTimeout(function () {
                    button.classList.remove("is-loading");
                    button.removeAttribute("aria-busy");
                }, 8000);
            }
        });
    });

    /* ---- Back to top -------------------------------------------------- */
    var backToTop = document.querySelector("[data-back-to-top]");
    if (backToTop) {
        var updateBackToTop = function () {
            backToTop.hidden = window.scrollY < 600;
        };
        on(window, "scroll", updateBackToTop, { passive: true });
        on(backToTop, "click", function () {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
        updateBackToTop();
    }
    /* ==================================================================
       Motion layer — progressive enhancement, transform/opacity only.
       Motion (motion.dev, MIT) is vendored locally; if it is missing or the
       visitor prefers reduced motion every effect is skipped and the CSS
       fallback is used instead.
       ================================================================== */

    function clamp01(value) { return value < 0 ? 0 : value > 1 ? 1 : value; }
    function smooth(value) { return value * value * (3 - 2 * value); }
    function easeInOut(value) {
        return value < 0.5
            ? 4 * value * value * value
            : 1 - Math.pow(-2 * value + 2, 3) / 2;
    }

    /* ---- Landing showcase: two orbits that land in the product grid --- */
    function initShowcase(root) {
        var M = window.Motion;
        var stage = root.querySelector("[data-showcase-stage]");
        var grid = root.querySelector("[data-showcase-grid]");
        var copy = root.querySelector("[data-showcase-copy]");
        var head = root.querySelector("[data-showcase-head]");
        var hint = root.querySelector("[data-showcase-hint]");
        var bar = root.querySelector("[data-showcase-bar]");
        var core = root.querySelector("[data-showcase-core]");
        var guides = Array.prototype.slice.call(root.querySelectorAll("[data-ring-guide]"));
        var ghosts = Array.prototype.slice.call(root.querySelectorAll("[data-ghost]"));
        var cards = grid ? Array.prototype.slice.call(grid.children) : [];
        var desktop = window.matchMedia("(min-width: 900px)");

        if (!M || !stage || !copy || !head || cards.length === 0 || reduced() || !desktop.matches) {
            root.classList.add("is-static");
            return;
        }

        root.classList.add("is-3d");

        var geo = null;
        var cardData = [];
        var progress = 0;
        var spin = 0;
        var spinAnimation = null;

        function measure() {
            var stageRect = stage.getBoundingClientRect();
            var width = stageRect.width;
            var height = stageRect.height;

            cards.forEach(function (card) {
                card.style.transform = "";
                card.style.opacity = "";
                card.style.zIndex = "";
            });
            void stage.offsetHeight; /* flush once, then measure without transforms */

            cardData = cards.map(function (card) {
                var rect = card.getBoundingClientRect();
                return {
                    el: card,
                    nx: rect.left - stageRect.left + rect.width / 2,
                    ny: rect.top - stageRect.top + rect.height / 2
                };
            });

            var cx = width * 0.71;
            var cy = height * 0.43;
            var outerX = Math.min(width * 0.25, 295);
            var outerY = Math.min(height * 0.30, 242);

            geo = {
                cx: cx,
                cy: cy,
                outerX: outerX,
                outerY: outerY,
                innerX: outerX * 0.78,
                innerY: outerY * 0.72
            };

            if (guides[0]) {
                guides[0].style.left = cx + "px";
                guides[0].style.top = cy + "px";
                guides[0].style.width = geo.outerX * 2 + "px";
                guides[0].style.height = geo.outerY * 2 + "px";
                guides[0].style.transform = "translate(-50%,-50%) rotate(-11deg)";
            }
            if (guides[1]) {
                guides[1].style.left = cx + "px";
                guides[1].style.top = cy + "px";
                guides[1].style.width = geo.innerX * 2 + "px";
                guides[1].style.height = geo.innerY * 2 + "px";
                guides[1].style.transform = "translate(-50%,-50%) rotate(9deg)";
            }
            ghosts.forEach(function (ghost) {
                /* Base position is the orbit centre; the render pass only adds offsets. */
                ghost.style.left = cx + "px";
                ghost.style.top = cy + "px";
            });
            if (core) {
                core.style.left = cx + "px";
                core.style.top = cy + "px";
                var coreSize = Math.max(90, Math.min(width, height) * 0.20);
                core.style.width = coreSize + "px";
                core.style.height = coreSize + "px";
            }
        }

        function orbitPoint(ring, slot, slots, angleOffset) {
            var base = (slot / slots) * Math.PI * 2 + angleOffset;
            var angle = base + (ring === "outer" ? -spin * 0.62 : spin);
            var rx = ring === "outer" ? geo.outerX : geo.innerX;
            var ry = ring === "outer" ? geo.outerY : geo.innerY;
            var tilt = ring === "outer" ? -0.19 : 0.15;
            var ex = Math.cos(angle) * rx;
            var ey = Math.sin(angle) * ry;
            var depth = (Math.sin(angle) + 1) / 2;
            var minScale = ring === "outer" ? 0.55 : 0.30;

            return {
                x: geo.cx + ex * Math.cos(tilt) - ey * Math.sin(tilt),
                y: geo.cy + ex * Math.sin(tilt) + ey * Math.cos(tilt),
                depth: depth,
                scale: minScale + depth * (ring === "outer" ? 0.25 : 0.14),
                opacity: (ring === "outer" ? 0.45 : 0.62) + depth * (ring === "outer" ? 0.55 : 0.38),
                rot: Math.sin(angle) * (ring === "outer" ? -16 : -12)
            };
        }

        function render() {
            if (!geo || !desktop.matches) { return; }

            cardData.forEach(function (data, index) {
                var start = 0.60 + (index / cardData.length) * 0.04;
                var end = 0.78 + (index / cardData.length) * 0.10;
                var t = easeInOut(clamp01((progress - start) / (end - start)));
                /* The real products ride the inner ring; the outer ring is their echo. */
                var o = orbitPoint("inner", index, cardData.length, 0);

                if (t >= 0.999) {
                    data.el.style.transform = "";
                    data.el.style.opacity = "";
                    data.el.style.zIndex = "";
                    return;
                }

                /* Curve into place instead of cutting through the middle of the ring. */
                var dx = data.nx - o.x;
                var dy = data.ny - o.y;
                var distance = Math.sqrt(dx * dx + dy * dy) || 1;
                var bulge = (index % 2 === 0 ? 1 : -1) * Math.min(96, distance * 0.24);
                var controlX = (o.x + data.nx) / 2 - (dy / distance) * bulge;
                var controlY = (o.y + data.ny) / 2 + (dx / distance) * bulge;
                var inv = 1 - t;
                var x = inv * inv * o.x + 2 * inv * t * controlX + t * t * data.nx;
                var y = inv * inv * o.y + 2 * inv * t * controlY + t * t * data.ny;
                var scale = o.scale + (1 - o.scale) * t;
                var rotation = o.rot * (1 - t);
                var flight = clamp01((t - 0.08) / 0.84);
                var dip = Math.sin(Math.PI * flight);
                var opacity = (o.opacity + (1 - o.opacity) * t) * (1 - 0.35 * dip);

                data.el.style.transform = "translate3d(" + (x - data.nx).toFixed(2) + "px," +
                    (y - data.ny).toFixed(2) + "px,0) scale(" + scale.toFixed(3) +
                    ") rotate(" + rotation.toFixed(2) + "deg)";
                data.el.style.opacity = opacity.toFixed(3);
                data.el.style.zIndex = String(10 + Math.round(o.depth * 10));
            });

            ghosts.forEach(function (ghost, index) {
                var o = orbitPoint("outer", index, ghosts.length, 0);
                var fade = 1 - clamp01((progress - 0.24) / 0.34);
                var out = easeInOut(clamp01((progress - 0.28) / 0.42));
                var gx = o.x + (o.x - geo.cx) * out * 0.55;
                var gy = o.y + (o.y - geo.cy) * out * 0.55 - out * 30;

                ghost.style.transform = "translate(-50%,-50%) translate3d(" +
                    (gx - geo.cx).toFixed(1) + "px," + (gy - geo.cy).toFixed(1) + "px,0) scale(" +
                    (o.scale * (1 + out * 0.28)).toFixed(3) + ") rotate(" +
                    (o.rot + out * 30).toFixed(1) + "deg)";
                ghost.style.opacity = (o.opacity * fade).toFixed(3);
                ghost.style.zIndex = String(Math.round(o.depth * 10));
            });

            var copyOut = smooth(clamp01((progress - 0.30) / 0.18));
            copy.style.opacity = (1 - copyOut).toFixed(3);
            copy.style.transform = "translate3d(0," + (-22 * copyOut).toFixed(1) + "px,0)";
            copy.style.pointerEvents = copyOut > 0.4 ? "none" : "";

            var headIn = smooth(clamp01((progress - 0.78) / 0.14));
            head.style.opacity = headIn.toFixed(3);
            head.style.transform = "translate3d(0," + (16 * (1 - headIn)).toFixed(1) + "px,0)";
            head.style.visibility = headIn > 0.04 ? "visible" : "hidden";
            head.style.pointerEvents = headIn > 0.5 ? "" : "none";

            if (hint) { hint.style.opacity = (1 - clamp01(progress / 0.06)).toFixed(3); }
            if (bar) { bar.style.transform = "scaleX(" + progress.toFixed(4) + ")"; }

            var coreOut = smooth(clamp01((progress - 0.54) / 0.20));
            if (core) {
                core.style.opacity = (1 - coreOut).toFixed(3);
                core.style.transform = "translate(-50%,-50%) scale(" + (1 - 0.28 * coreOut).toFixed(3) + ")";
            }
            guides.forEach(function (guide) { guide.style.opacity = (1 - coreOut).toFixed(3); });

            root.classList.toggle("is-landed", progress > 0.92);
        }

        measure();
        render();

        M.scroll(function (value) {
            progress = clamp01(value);
            render();
        }, { target: root, offset: ["start start", "end end"] });

        var spinValue = M.motionValue(0);
        spinAnimation = M.animate(spinValue, 360, {
            duration: 90,
            ease: "linear",
            repeat: Infinity
        });
        spinValue.on("change", function (value) {
            spin = (value * Math.PI) / 180;
            render();
        });
        if (spinAnimation && spinAnimation.pause) { spinAnimation.pause(); }

        /* Only burn frames while the stage is on screen. */
        M.inView(root, function () {
            if (spinAnimation && spinAnimation.play) { spinAnimation.play(); }
            return function () {
                if (spinAnimation && spinAnimation.pause) { spinAnimation.pause(); }
            };
        }, { amount: 0.01 });

        if (reduceMotion && reduceMotion.addEventListener) {
            reduceMotion.addEventListener("change", function (event) {
                if (event.matches) {
                    root.classList.remove("is-3d");
                    root.classList.add("is-static");
                }
            });
        }

        var resizeTimer;
        window.addEventListener("resize", function () {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(function () {
                if (!desktop.matches) { return; }
                measure();
                render();
            }, 160);
        }, { passive: true });
    }

    /* ---- Product page: asynchronous, layered image column ------------- */
    function initProduct(root) {
        var M = window.Motion;
        if (!M || reduced()) { return; }

        var layers = Array.prototype.slice.call(root.querySelectorAll("[data-parallax]"));
        if (layers.length) {
            M.scroll(function (value) {
                var shift = (value - 0.5) * 2;
                layers.forEach(function (layer) {
                    var factor = parseFloat(layer.getAttribute("data-parallax")) || 0;
                    var baseRotation = parseFloat(layer.getAttribute("data-rotate")) || 0;
                    layer.style.transform = "translate3d(0," + (shift * factor * 46).toFixed(2) +
                        "px,0) rotate(" + (baseRotation + shift * factor * 3).toFixed(2) + "deg)";
                });
            }, { target: root, offset: ["start end", "end start"] });
        }

        var info = root.querySelector("[data-product-info]");
        if (info) {
            var infoBlocks = Array.prototype.slice.call(info.children);
            infoBlocks.forEach(function (child) {
                child.style.opacity = "0";
            });
            M.inView(info, function () {
                M.animate(infoBlocks, { opacity: 1, y: [18, 0] }, {
                    type: "spring",
                    stiffness: 220,
                    damping: 26,
                    mass: 0.9,
                    delay: M.stagger(0.07)
                });
            }, { amount: 0.15 });
        }
    }

    /* ---- In-view reveals for category tiles, trust rows and panels ----- */
    function initReveals() {
        var M = window.Motion;
        if (!M || reduced()) { return; }
        document.querySelectorAll("[data-reveal-group]").forEach(function (group) {
            var children = Array.prototype.slice.call(group.children);
            if (!children.length) { return; }
            children.forEach(function (child) { child.style.opacity = "0"; });
            M.inView(group, function () {
                M.animate(children, { opacity: 1, y: [16, 0] }, {
                    type: "spring",
                    stiffness: 240,
                    damping: 26,
                    mass: 0.85,
                    delay: M.stagger(0.06)
                });
            }, { amount: 0.15 });
        });
    }

    var showcase = document.querySelector("[data-showcase]");
    if (showcase) {
        try {
            initShowcase(showcase);
        } catch (error) {
            showcase.classList.remove("is-3d");
            showcase.classList.add("is-static");
        }
    }

    var productDetail = document.querySelector("[data-product]");
    if (productDetail) {
        try { initProduct(productDetail); } catch (error) { /* keep the static page */ }
    }

    try { initReveals(); } catch (error) { /* reveals are decorative */ }
})();
