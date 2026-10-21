<#-- Shared layout + UI macros. Import as: <#import "partials/layout.ftl" as ui> -->
<#-- NOTE: macros below are intentionally kept on single lines so they never emit
     stray whitespace into attributes such as src="..." or value="...". -->

<#macro csrfField><#if csrf??><input type="hidden" name="${csrf.parameterName}" value="${csrf.token}"></#if></#macro>

<#-- Lucide icons (https://lucide.dev, ISC). Inline SVG keeps them offline, themeable
     via currentColor and free of an icon-font or runtime dependency. -->
<#macro lucide name size=20 cls="" stroke=1.8>
<#compress>
<svg class="icon<#if cls?has_content> ${cls}</#if>" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${stroke}" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
<#switch name>
  <#case "menu"><path d="M4 5h16"/><path d="M4 12h16"/><path d="M4 19h16"/><#break>
  <#case "x"><path d="M18 6 6 18"/><path d="m6 6 12 12"/><#break>
  <#case "sun"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/><#break>
  <#case "moon"><path d="M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401"/><#break>
  <#case "search"><path d="m21 21-4.34-4.34"/><circle cx="11" cy="11" r="8"/><#break>
  <#case "shopping-bag"><path d="M16 10a4 4 0 0 1-8 0"/><path d="M3.103 6.034h17.794"/><path d="M3.4 5.467a2 2 0 0 0-.4 1.2V20a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6.667a2 2 0 0 0-.4-1.2l-2-2.667A2 2 0 0 0 17 2H7a2 2 0 0 0-1.6.8z"/><#break>
  <#case "shopping-cart"><path d="m2.05 2.05 1.099-.028a1 1 0 0 1 1.008.815l2.69 14.347A1 1 0 0 0 7.83 18H18"/><path d="M4.563 5h16.435a1 1 0 0 1 .981 1.204l-1.026 6.226A2 2 0 0 1 18.962 14H6.25"/><circle cx="18" cy="20" r="2"/><circle cx="8" cy="20" r="2"/><#break>
  <#case "package"><path d="M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z"/><path d="M12 22V12"/><polyline points="3.29 7 12 12 20.71 7"/><path d="m7.5 4.27 9 5.15"/><#break>
  <#case "clipboard-list"><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/><#break>
  <#case "map-pin"><path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/><#break>
  <#case "info"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/><#break>
  <#case "arrow-up"><path d="m5 12 7-7 7 7"/><path d="M12 19V5"/><#break>
  <#case "arrow-right"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/><#break>
  <#case "truck"><path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/><#break>
  <#case "shield-check"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/><#break>
  <#case "rotate-ccw"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><#break>
  <#case "sparkles"><path d="M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z"/><path d="M20 2v4"/><path d="M22 4h-4"/><circle cx="4" cy="20" r="2"/><#break>
  <#case "chevron-down"><path d="m6 9 6 6 6-6"/><#break>
  <#case "star"><path d="M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z"/><#break>
  <#case "check"><path d="M20 6 9 17l-5-5"/><#break>
  <#case "plus"><path d="M5 12h14"/><path d="M12 5v14"/><#break>
  <#case "minus"><path d="M5 12h14"/><#break>
  <#case "boxes"><path d="M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z"/><path d="m7 16.5-4.74-2.85"/><path d="m7 16.5 5-3"/><path d="M7 16.5v5.17"/><path d="M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z"/><path d="m17 16.5-5-3"/><path d="m17 16.5 4.74-2.85"/><path d="M17 16.5v5.17"/><path d="M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z"/><path d="M12 8 7.26 5.15"/><path d="m12 8 4.74-2.85"/><path d="M12 13.5V8"/><#break>
  <#case "credit-card"><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/><path d="M6 14h2"/><#break>
  <#default><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>
</#switch>
</svg>
</#compress>
</#macro>

<#macro money amount>${currencySymbol}${(amount!0)?string("#,##0.00")}</#macro>

<#macro productImage p><#if p?? && p.mediaList?? && p.mediaList?size gt 0 && p.mediaList[0].url??>${p.mediaList[0].url}<#else>/images/placeholder.svg</#if></#macro>

<#-- Null-safe pricing helpers. Legacy rows may have a NULL embedded price. -->
<#function discountPercent price><#return (price.percentageDiscount)!0></#function>

<#function lineTotal snapshot><#return ((snapshot.grossAmount)!0 - (snapshot.grossDiscount)!0) * (100 - (snapshot.percentageDiscount)!0) / 100></#function>

<#macro priceBlock price>
<#local pct = (price.percentageDiscount)!0>
<#local gross = (price.grossPrice)!0>
<#if pct gt 0><#local sale = gross * (100 - pct) / 100><span class="price-now"><@money sale/></span> <s class="price-was"><@money gross/></s><#else><span class="price-now"><@money gross/></span></#if>
</#macro>

<#macro stockBadge p><#if !p.available><span class="stock out">Sold out</span><#elseif p.stock?? && p.stock <= 5><span class="stock low">Only ${p.stock} left in stock</span><#elseif p.stock??><span class="stock in">${p.stock} in stock</span><#else><span class="stock in">Available to order</span></#if></#macro>

<#-- Deliberate empty states: an icon, a short explanation and one clear action. -->
<#macro emptyState icon title text actionHref="" actionLabel="">
<div class="empty-state">
  <span class="empty-icon" aria-hidden="true">
    <#switch icon>
      <#case "cart"><@lucide "shopping-cart" 24/><#break>
      <#case "search"><@lucide "search" 24/><#break>
      <#case "box"><@lucide "package" 24/><#break>
      <#case "orders"><@lucide "clipboard-list" 24/><#break>
      <#case "pin"><@lucide "map-pin" 24/><#break>
      <#default><@lucide "info" 24/>
    </#switch>
  </span>
  <h2>${title}</h2>
  <p>${text}</p>
  <#if actionHref?has_content><div class="actions"><a class="btn btn-primary" href="${actionHref}">${actionLabel}</a></div></#if>
</div>
</#macro>

<#macro productCard p>
<#local pct = (p.price.percentageDiscount)!0>
<article class="card">
  <a class="card-media" href="/products/${p.id}" aria-label="${(p.name)!''}">
    <img src="<@productImage p/>" alt="${(p.name)!''}" loading="lazy" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
    <#if pct gt 0><span class="badge">-${pct?string("0")}%</span></#if>
    <#if !p.available><span class="badge badge-out">Sold out</span></#if>
  </a>
  <div class="card-body">
    <span class="cat">${(p.categoryLabel)!''}</span>
    <h3 class="card-title"><a href="/products/${p.id}">${(p.name)!''}</a></h3>
    <div class="price"><@priceBlock p.price/></div>
    <p class="stock-line"><@stockBadge p/></p>
    <form method="post" action="/cart/items" data-loading>
      <@csrfField/>
      <input type="hidden" name="productId" value="${p.id}">
      <input type="hidden" name="quantity" value="1">
      <button class="btn btn-soft btn-block" type="submit" <#if !p.available>disabled</#if>>Add to cart</button>
    </form>
  </div>
</article>
</#macro>

<#-- Windowed pagination. "path" is the URL without query, "query" is the extra params. -->
<#macro pagination current total path query="">
<#if total gt 1>
<nav class="pagination" aria-label="Pagination">
  <#if current gt 0><a class="page nav" rel="prev" href="${path}?page=${current - 1}<#if query?has_content>&amp;${query}</#if>"><span aria-hidden="true">‹</span><span class="sr-only">Previous page</span></a><#else><span class="page nav disabled" aria-disabled="true" aria-hidden="true">‹</span></#if>
  <#list 0..<total as i>
    <#if i == current>
      <span class="page current" aria-current="page">${i + 1}</span>
    <#elseif i == 0 || i == total - 1 || (i >= current - 2 && i <= current + 2)>
      <a class="page" href="${path}?page=${i}<#if query?has_content>&amp;${query}</#if>">${i + 1}</a>
    <#elseif i == 1 && current gt 3>
      <span class="page gap" aria-hidden="true">…</span>
    <#elseif i == total - 2 && current lt total - 4>
      <span class="page gap" aria-hidden="true">…</span>
    </#if>
  </#list>
  <#if current lt total - 1><a class="page nav" rel="next" href="${path}?page=${current + 1}<#if query?has_content>&amp;${query}</#if>"><span aria-hidden="true">›</span><span class="sr-only">Next page</span></a><#else><span class="page nav disabled" aria-disabled="true" aria-hidden="true">›</span></#if>
</nav>
</#if>
</#macro>

<#macro page title description="">
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${title} · ${appName}</title>
  <meta name="description" content="${description}">
  <meta name="theme-color" content="#e2231a">
  <#-- Resolve the deferred CSRF token in the head: a large page can fill the
       response buffer before the first form, and a later lazy lookup would try
       to create a session after the response is committed. -->
  <#if csrf??><meta name="csrf-param" content="${csrf.parameterName}"><meta name="csrf-token" content="${csrf.token}"></#if>
  <meta property="og:title" content="${title} · ${appName}">
  <meta property="og:description" content="${description}">
  <meta property="og:type" content="website">
  <meta name="twitter:card" content="summary_large_image">
  <link rel="canonical" href="${canonicalUrl!('/')}">
  <link rel="icon" type="image/svg+xml" href="/images/favicon.svg">
  <script>try{var t=localStorage.getItem('theme');if(t==='dark'||t==='light'){document.documentElement.setAttribute('data-theme',t);}}catch(e){}</script>
  <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<header class="site-header">
  <div class="container header-inner">
    <a class="brand" href="/" aria-label="${appName} home">${appName}<span>.</span></a>
    <form class="search" action="/products" method="get" role="search">
      <label class="sr-only" for="site-search">Search products</label>
      <input id="site-search" type="search" name="q" value="${(q)!''}" placeholder="Search sets, minifigures, accessories…" autocomplete="off">
      <button class="btn btn-ghost" type="submit">Search</button>
    </form>
    <button class="nav-toggle" type="button" aria-expanded="false" aria-controls="primary-nav" aria-label="Toggle navigation">
      <@lucide "menu" 22 "icon-menu"/>
      <@lucide "x" 22 "icon-close"/>
    </button>
    <nav class="nav" id="primary-nav" aria-label="Main">
      <a href="/products"<#if currentPath?starts_with("/products")> class="active" aria-current="page"</#if>>Shop</a>
      <#if currentUser?? && currentUser.admin><a href="/admin"<#if currentPath?starts_with("/admin")> class="active" aria-current="page"</#if>>Admin</a></#if>
      <a class="cart-link" href="/cart"<#if currentPath?starts_with("/cart")> class="active" aria-current="page"</#if>><@lucide "shopping-bag" 17/>Cart <span class="pill" aria-live="polite">${cartCount}</span><span class="sr-only"> items in cart</span></a>
      <#if currentUser??>
        <a href="/account"<#if currentPath?starts_with("/account")> class="active" aria-current="page"</#if>>${currentUser.displayName}</a>
        <form class="inline" method="post" action="/logout"><@csrfField/><button class="btn btn-ghost btn-sm" type="submit">Sign out</button></form>
      <#else>
        <a href="/login"<#if currentPath?starts_with("/login")> class="active" aria-current="page"</#if>>Sign in</a>
        <a class="btn btn-primary btn-sm" href="/register">Create account</a>
      </#if>
      <button class="theme-toggle" type="button" data-theme-toggle aria-label="Switch colour theme" title="Switch colour theme">
        <@lucide "sun" 18 "icon-sun"/>
        <@lucide "moon" 18 "icon-moon"/>
      </button>
    </nav>
  </div>
  <div class="catbar">
    <div class="container catbar-inner">
      <a href="/products"<#if !selectedCategory??> class="active"</#if>>All products</a>
      <#list categories as c>
        <a href="/products?category=${c.name}"<#if selectedCategory?? && selectedCategory.name == c.name> class="active" aria-current="page"</#if>>${c.label}</a>
      </#list>
    </div>
  </div>
</header>

<main id="main">
  <div class="container">
    <#if flash??><div class="alert alert-ok" role="status"><span>${flash}</span><button class="alert-close" type="button" aria-label="Dismiss message" data-dismiss="alert">×</button></div></#if>
    <#if flashError??><div class="alert alert-error" role="alert"><span>${flashError}</span><button class="alert-close" type="button" aria-label="Dismiss message" data-dismiss="alert">×</button></div></#if>
    <#nested>
  </div>
</main>

<footer class="site-footer">
  <div class="container footer-grid">
    <div>
      <h4 class="footer-brand">${appName}</h4>
      <p>Quality sets, minifigures and accessories — shipped fast and packed with care.</p>
    </div>
    <div>
      <h4>Shop</h4>
      <ul>
        <li><a href="/products">All products</a></li>
        <#list categories as c><li><a href="/products?category=${c.name}">${c.label}</a></li></#list>
      </ul>
    </div>
    <div>
      <h4>Help</h4>
      <ul>
        <li><a href="/returns">Returns &amp; refunds</a></li>
        <li><a href="/account">My account</a></li>
        <li><a href="/cart">My cart</a></li>
      </ul>
    </div>
    <div>
      <h4>Secure shopping</h4>
      <p>All card payments are processed over encrypted connections. We never store your card details.</p>
      <p class="pay-strip">Card · PayPal · WebMoney</p>
    </div>
  </div>
  <div class="container copyright">© ${year} ${appName}. All rights reserved. Built for bricks, minifigures and the people who love them.</div>
</footer>
<button class="back-to-top" type="button" data-back-to-top aria-label="Back to top" hidden><@lucide "arrow-up" 20/></button>
<script src="/js/vendor/motion.14.5.0.min.js" defer></script>
<script src="/js/app.js" defer></script>
</body>
</html>
</#macro>
