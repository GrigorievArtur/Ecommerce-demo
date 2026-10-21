<#-- Shared layout + UI macros. Import as: <#import "partials/layout.ftl" as ui> -->
<#-- NOTE: macros below are intentionally kept on single lines so they never emit
     stray whitespace into attributes such as src="..." or value="...". -->

<#macro csrfField><#if csrf??><input type="hidden" name="${csrf.parameterName}" value="${csrf.token}"></#if></#macro>

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
      <#case "cart">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 4h2l2.4 10.2a1 1 0 0 0 1 .8h8.2a1 1 0 0 0 1-.8L20 7H6"/><circle cx="9" cy="19" r="1.3"/><circle cx="17" cy="19" r="1.3"/></svg>
      <#break>
      <#case "search">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"><circle cx="11" cy="11" r="6.5"/><path d="m16 16 4 4"/></svg>
      <#break>
      <#case "box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 7.5 12 3l8.5 4.5v9L12 21l-8.5-4.5z"/><path d="M3.5 7.5 12 12l8.5-4.5M12 12v9"/></svg>
      <#break>
      <#case "orders">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3h12a1 1 0 0 1 1 1v17l-3-2-2 2-2-2-2 2-2-2-3 2V4a1 1 0 0 1 1-1Z"/><path d="M9 8h6M9 12h6"/></svg>
      <#break>
      <#case "pin">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s7-5.5 7-11a7 7 0 1 0-14 0c0 5.5 7 11 7 11Z"/><circle cx="12" cy="10" r="2.5"/></svg>
      <#break>
      <#default>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
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
      <span class="nav-toggle-bar"></span>
      <span class="nav-toggle-bar"></span>
      <span class="nav-toggle-bar"></span>
    </button>
    <nav class="nav" id="primary-nav" aria-label="Main">
      <a href="/products"<#if currentPath?starts_with("/products")> class="active" aria-current="page"</#if>>Shop</a>
      <#if currentUser?? && currentUser.admin><a href="/admin"<#if currentPath?starts_with("/admin")> class="active" aria-current="page"</#if>>Admin</a></#if>
      <a class="cart-link" href="/cart"<#if currentPath?starts_with("/cart")> class="active" aria-current="page"</#if>>Cart <span class="pill" aria-live="polite">${cartCount}</span><span class="sr-only"> items in cart</span></a>
      <#if currentUser??>
        <a href="/account"<#if currentPath?starts_with("/account")> class="active" aria-current="page"</#if>>${currentUser.displayName}</a>
        <form class="inline" method="post" action="/logout"><@csrfField/><button class="btn btn-ghost btn-sm" type="submit">Sign out</button></form>
      <#else>
        <a href="/login"<#if currentPath?starts_with("/login")> class="active" aria-current="page"</#if>>Sign in</a>
        <a class="btn btn-primary btn-sm" href="/register">Create account</a>
      </#if>
      <button class="theme-toggle" type="button" data-theme-toggle aria-label="Switch colour theme" title="Switch colour theme">
        <svg class="icon-sun" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M12 2.5v2.4M12 19.1v2.4M2.5 12h2.4M19.1 12h2.4M5.2 5.2l1.7 1.7M17.1 17.1l1.7 1.7M18.8 5.2l-1.7 1.7M6.9 17.1l-1.7 1.7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
        <svg class="icon-moon" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 14.5A8.5 8.5 0 1 1 9.5 4a7 7 0 0 0 10.5 10.5Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
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
<button class="back-to-top" type="button" data-back-to-top aria-label="Back to top" hidden>↑</button>
<script src="/js/app.js" defer></script>
</body>
</html>
</#macro>
