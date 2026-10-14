<#import "partials/layout.ftl" as ui>
<#assign pct = (product.price.percentageDiscount)!0>
<#assign gross = (product.price.grossPrice)!0>
<#if pct gt 0>
  <#assign unit = gross * (100 - pct) / 100>
<#else>
  <#assign unit = gross>
</#if>

<@ui.page title=(product.name)!'Product' description=(product.description)!''>
  <nav class="breadcrumb" aria-label="Breadcrumb">
    <a href="/">Home</a> / <a href="/products">Shop</a> /
    <a href="/products?category=${(product.categoryName)!''}">${(product.categoryLabel)!''}</a> / ${(product.name)!''}
  </nav>

  <div class="product-detail">
    <div class="gallery" data-gallery>
      <div class="gallery-main">
        <img data-gallery-main src="<@ui.productImage product/>" alt="${(product.name)!''}"
             onerror="this.onerror=null;this.src='/images/placeholder.svg'">
      </div>
      <#if product.mediaList?? && product.mediaList?size gt 1>
        <div class="gallery-thumbs" role="group" aria-label="Product images">
          <#list product.mediaList as m>
            <button class="gallery-thumb<#if m?index == 0> active</#if>" type="button" data-gallery-thumb="${m.url}" aria-pressed="<#if m?index == 0>true<#else>false</#if>" aria-label="Show image ${m?index + 1}">
              <img src="${m.url}" alt="" loading="lazy" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
            </button>
          </#list>
        </div>
      </#if>
    </div>

    <div class="stack">
      <div>
        <span class="cat">${(product.categoryLabel)!''}</span>
        <h1>${(product.name)!''}</h1>
        <div class="price"><@ui.priceBlock product.price/></div>
        <p class="stock-line"><@ui.stockBadge product/></p>
      </div>

      <div class="buy-box">
        <form method="post" action="/cart/items" data-loading>
          <@ui.csrfField/>
          <input type="hidden" name="productId" value="${product.id}">
          <div class="field qty-field">
            <label for="qty">Quantity</label>
            <div class="qty-stepper" data-stepper>
              <button type="button" data-step="-1" aria-label="Decrease quantity">−</button>
              <input id="qty" type="number" name="quantity" value="1" min="1" inputmode="numeric" <#if product.stock??>max="${product.stock}"</#if>>
              <button type="button" data-step="1" aria-label="Increase quantity">+</button>
            </div>
          </div>
          <div class="add-row">
            <button class="btn btn-primary" type="submit" <#if !product.available>disabled</#if>>
              <#if product.available>Add to cart<#else>Sold out</#if>
            </button>
          </div>
        </form>
        <p class="note">Secure checkout · 30-day returns · Dispatched within 24h</p>
      </div>

      <div class="panel">
        <h2 class="panel-title">Description</h2>
        <p>${(product.description)!''}</p>
      </div>
    </div>
  </div>

  <#if related?? && related?size gt 0>
    <section class="related">
      <div class="page-head">
        <div>
          <h2>You may also like</h2>
          <p>More from ${(product.categoryLabel)!''}.</p>
        </div>
        <a href="/products?category=${(product.categoryName)!''}">View category →</a>
      </div>
      <div class="grid">
        <#list related as p><@ui.productCard p/></#list>
      </div>
    </section>
  </#if>

  <script type="application/ld+json">
  <#outputformat "JSON">
  {
    "@context": "https://schema.org",
    "@type": "Product",
    "name": "${(product.name)!''}",
    "description": "${(product.description)!''}",
    "category": "${(product.categoryLabel)!''}",
    "image": "<@ui.productImage product/>",
    "offers": {
      "@type": "Offer",
      "price": "${unit?string('0.00')}",
      "priceCurrency": "${currency}",
      "availability": <#if product.available>"https://schema.org/InStock"<#else>"https://schema.org/OutOfStock"</#if>
    }
  }
  </#outputformat>
  </script>
</@ui.page>
