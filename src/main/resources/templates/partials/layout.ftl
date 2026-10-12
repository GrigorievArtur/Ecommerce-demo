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

<#macro stockBadge p><#if !p.available><span class="stock out">Sold out</span><#elseif p.stock?? && p.stock <= 5><span class="stock low">Only ${p.stock} left</span><#elseif p.stock??><span class="stock in">In stock</span><#else><span class="stock in">Available</span></#if></#macro>

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
    <form method="post" action="/cart/items">
      <@csrfField/>
      <input type="hidden" name="productId" value="${p.id}">
      <input type="hidden" name="quantity" value="1">
      <button class="btn btn-primary btn-block" type="submit" <#if !p.available>disabled</#if>>Add to cart</button>
    </form>
  </div>
</article>
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
  <link rel="icon" href="/images/placeholder.svg">
  <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<header class="site-header">
  <div class="container header-inner">
    <a class="brand" href="/">${appName}<span>.</span></a>
    <form class="search" action="/products" method="get" role="search">
      <label class="sr-only" for="site-search">Search products</label>
      <input id="site-search" type="search" name="q" value="${(q)!''}" placeholder="Search sets, minifigures, accessories…">
      <button class="btn btn-ghost" type="submit">Search</button>
    </form>
    <button class="nav-toggle" type="button" aria-expanded="false" aria-controls="primary-nav" aria-label="Toggle navigation">
      <span class="nav-toggle-bar"></span>
      <span class="nav-toggle-bar"></span>
      <span class="nav-toggle-bar"></span>
    </button>
    <nav class="nav" id="primary-nav" aria-label="Main">
      <a href="/products">Shop</a>
      <#if currentUser?? && currentUser.admin><a href="/admin">Admin</a></#if>
      <a class="cart-link" href="/cart">Cart <span class="pill">${cartCount}</span></a>
      <#if currentUser??>
        <a href="/account">${currentUser.displayName}</a>
        <form class="inline" method="post" action="/logout"><@csrfField/><button class="btn btn-ghost btn-sm" type="submit">Sign out</button></form>
      <#else>
        <a href="/login">Sign in</a>
        <a class="btn btn-primary btn-sm" href="/register">Create account</a>
      </#if>
    </nav>
  </div>
  <div class="catbar">
    <div class="container catbar-inner">
      <a href="/products"<#if !selectedCategory??> class="active"</#if>>All products</a>
      <#list categories as c>
        <a href="/products?category=${c.name}"<#if selectedCategory?? && selectedCategory.name == c.name> class="active"</#if>>${c.label}</a>
      </#list>
    </div>
  </div>
</header>

<main>
  <div class="container">
    <#if flash??><div class="alert alert-ok" role="status">${flash}</div></#if>
    <#if flashError??><div class="alert alert-error" role="alert">${flashError}</div></#if>
    <#nested>
  </div>
</main>

<footer class="site-footer">
  <div class="container footer-grid">
    <div>
      <h4>${appName}</h4>
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
    </div>
  </div>
  <div class="container copyright">© ${year} ${appName}. All rights reserved.</div>
</footer>
<script src="/js/app.js" defer></script>
</body>
</html>
</#macro>
