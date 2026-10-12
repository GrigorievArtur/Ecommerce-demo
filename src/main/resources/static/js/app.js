// Progressive enhancement only — every page works without JavaScript.
(function () {
    "use strict";

    // Mobile navigation toggle.
    var toggle = document.querySelector(".nav-toggle");
    var nav = document.getElementById("primary-nav");
    if (toggle && nav) {
        toggle.addEventListener("click", function () {
            var open = nav.classList.toggle("open");
            toggle.setAttribute("aria-expanded", open ? "true" : "false");
        });
    }

    // Auto-dismiss flash messages.
    document.querySelectorAll(".alert").forEach(function (el) {
        setTimeout(function () {
            el.style.transition = "opacity .4s ease";
            el.style.opacity = "0";
            setTimeout(function () { el.remove(); }, 400);
        }, 5000);
    });

    // Hide card fields when a non-card payment method is selected.
    var methodInputs = document.querySelectorAll('input[name="method"]');
    var cardFields = document.getElementById("card-fields");
    function syncPaymentFields() {
        if (!cardFields || !methodInputs.length) return;
        var method = document.querySelector('input[name="method"]:checked');
        cardFields.hidden = !(method && method.value === "CARD");
    }
    methodInputs.forEach(function (input) { input.addEventListener("change", syncPaymentFields); });
    syncPaymentFields();
})();
