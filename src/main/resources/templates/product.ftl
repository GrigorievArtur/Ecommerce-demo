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

  <div class="product-detail" data-product>
    <#-- Sticky image column: the main image, a blurred depth layer and a floating
         product chip move at different scroll speeds to build real layering. -->
    <div class="gallery" data-gallery>
      <div class="gallery-layout">
        <#if product.mediaList?? && product.mediaList?size gt 1>
          <div class="gallery-thumbs" role="group" aria-label="Product images">
            <#list product.mediaList as m>
              <button class="gallery-thumb<#if m?index == 0> active</#if>" type="button" data-gallery-thumb="${m.url}" aria-pressed="<#if m?index == 0>true<#else>false</#if>" aria-label="Show image ${m?index + 1}">
                <img src="${m.url}" alt="" loading="lazy" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
              </button>
            </#list>
          </div>
        </#if>

        <div class="gallery-stage" data-gallery-stage>
          <span class="gallery-glow" aria-hidden="true"></span>
          <div class="gallery-layer gallery-back" data-parallax="0.55" data-rotate="4" aria-hidden="true"></div>

          <figure class="gallery-main" data-parallax="-0.14">
            <img data-gallery-main src="<@ui.productImage product/>" alt="${(product.name)!''}"
                 onerror="this.onerror=null;this.src='/images/placeholder.svg'">
          </figure>

          <span class="gallery-ring" aria-hidden="true"></span>

          <div class="gallery-chip" data-parallax="-0.42">
            <span class="gallery-chip-icon"><@ui.lucide "sparkles" 16/></span>
            <span>
              <strong>${(product.categoryLabel)!'New arrival'}</strong>
              <small><#if product.available>Ready to ship<#else>Back soon</#if></small>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="stack product-info" data-product-info>
      <div>
        <span class="eyebrow">${(product.categoryLabel)!''}</span>
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
              <button type="button" data-step="-1" aria-label="Decrease quantity"><@ui.lucide "minus" 16/></button>
              <input id="qty" type="number" name="quantity" value="1" min="1" inputmode="numeric" <#if product.stock??>max="${product.stock}"</#if>>
              <button type="button" data-step="1" aria-label="Increase quantity"><@ui.lucide "plus" 16/></button>
            </div>
          </div>
          <div class="add-row">
            <button class="btn btn-primary" type="submit" <#if !product.available>disabled</#if>>
              <#if product.available><@ui.lucide "shopping-bag" 18/>Add to cart<#else>Sold out</#if>
            </button>
          </div>
        </form>
        <ul class="product-perks">
          <li><@ui.lucide "truck" 16/> Dispatched within 24 hours</li>
          <li><@ui.lucide "shield-check" 16/> Encrypted checkout</li>
          <li><@ui.lucide "rotate-ccw" 16/> 30-day returns</li>
        </ul>
      </div>

      <div class="panel">
        <h2 class="panel-title">About this set</h2>
        <p>${(product.description)!''}</p>
      </div>

      <div class="panel">
        <h2 class="panel-title">Delivery &amp; returns</h2>
        <ul class="spec-list">
          <li><span>Dispatch</span><strong>Within 24 hours</strong></li>
          <li><span>Delivery</span><strong>Free over <@ui.money freeShippingThreshold!0/></strong></li>
          <li><span>Returns</span><strong>30 days, free</strong></li>
        </ul>
      </div>
    </div>
  </div>

  <#if related?? && related?size gt 0>
    <section class="related">
      <div class="section-head">
        <div>
          <h2>You may also like</h2>
          <p>More from ${(product.categoryLabel)!''}.</p>
        </div>
        <a class="btn btn-tertiary" href="/products?category=${(product.categoryName)!''}">View category <@ui.lucide "arrow-right" 16/></a>
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
