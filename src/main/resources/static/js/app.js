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
    var gallery = document.querySelector("[data-gallery]");
    if (gallery) {
        var mainImage = gallery.querySelector("[data-gallery-main]");
        gallery.querySelectorAll("[data-gallery-thumb]").forEach(function (thumb) {
            on(thumb, "click", function () {
                if (mainImage) { mainImage.src = thumb.getAttribute("data-gallery-thumb"); }
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
})();
