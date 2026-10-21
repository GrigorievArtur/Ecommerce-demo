<#import "partials/layout.ftl" as ui>
<@ui.page title="Home" description="Shop quality sets, minifigures and accessories with fast, secure checkout and easy returns.">
  <#-- The landing stage: every product card below starts life in one of two
       counter-rotating orbits and is transformed back into its normal grid slot
       as the page scrolls. Without JavaScript (or with reduced motion) the CSS
       stays in its static, fully readable fallback. -->
  <section class="showcase" data-showcase aria-labelledby="showcase-title">
    <div class="showcase-stage" data-showcase-stage>
      <div class="showcase-atmosphere" aria-hidden="true"></div>

      <div class="showcase-copy" data-showcase-copy>
        <span class="eyebrow"><@ui.lucide "sparkles" 15/> The brick constellation</span>
        <h1 id="showcase-title">Build something <span class="grad-text">brilliant</span> today.</h1>
        <p>Thousands of bricks, minifigures and accessories — curated, stocked and ready to ship. Watch the latest sets break orbit, then land straight into the shelves.</p>
        <div class="actions">
          <a class="btn btn-primary btn-lg" href="/products">Shop the catalogue <@ui.lucide "arrow-right" 18/></a>
          <a class="btn btn-on-dark" href="/products?sort=price-asc">Best value first</a>
        </div>
        <ul class="showcase-meta">
          <li><@ui.lucide "truck" 17/> Free delivery over <@ui.money freeShippingThreshold!0/></li>
          <li><@ui.lucide "shield-check" 17/> Secure checkout</li>
          <li><@ui.lucide "rotate-ccw" 17/> 30-day returns</li>
        </ul>
      </div>

      <div class="showcase-rings" aria-hidden="true" data-showcase-rings>
        <span class="showcase-core" data-showcase-core></span>
        <span class="ring-guide ring-guide-outer" data-ring-guide></span>
        <span class="ring-guide ring-guide-inner" data-ring-guide></span>
        <#if products?size gt 0>
          <div class="showcase-ghosts">
            <#list 0..<8 as i>
              <#assign orbitProduct = products[i % products?size]>
              <div class="ghost-card" data-ghost>
                <img src="<@ui.productImage orbitProduct/>" alt="" loading="lazy" decoding="async" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
                <span class="ghost-price"><@ui.priceBlock orbitProduct.price/></span>
              </div>
            </#list>
          </div>
        </#if>
      </div>

      <div class="showcase-deck">
        <div class="showcase-head" data-showcase-head>
          <div>
            <span class="eyebrow">New arrivals</span>
            <h2>Fresh off the shelf</h2>
          </div>
          <a class="btn btn-tertiary" href="/products">View all products <@ui.lucide "arrow-right" 16/></a>
        </div>

        <#if products?size == 0>
          <@ui.emptyState icon="box" title="The shelves are being stocked" text="No products are live yet. Add your first product in the admin panel and it will appear here." actionHref="/admin/products/new" actionLabel="Add a product"/>
        <#else>
          <div class="grid showcase-grid" data-showcase-grid>
            <#list products as p><@ui.productCard p/></#list>
          </div>
        </#if>
      </div>

      <div class="showcase-hint" data-showcase-hint>
        <span>Scroll to land the bricks</span>
        <@ui.lucide "chevron-down" 16/>
      </div>
      <div class="showcase-progress" aria-hidden="true"><span data-showcase-bar></span></div>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <h2>Shop by category</h2>
        <p>Pick a shelf and dive in.</p>
      </div>
      <a class="btn btn-tertiary" href="/products">View all products <@ui.lucide "arrow-right" 16/></a>
    </div>
    <div class="cat-tiles" data-reveal-group>
      <#list categories as c>
        <a class="cat-tile" href="/products?category=${c.name}">
          <@ui.lucide "boxes" 20 "cat-icon"/>
          <strong>${c.label}</strong>
          <span>Browse the range <@ui.lucide "arrow-right" 14/></span>
        </a>
      </#list>
    </div>
  </section>

  <section class="trust" aria-label="Why shop with us" data-reveal-group>
    <div>
      <span class="trust-icon"><@ui.lucide "truck" 20/></span>
      <h3>Fast delivery</h3>
      <p>Orders dispatched within 24 hours on business days.</p>
    </div>
    <div>
      <span class="trust-icon"><@ui.lucide "package" 20/></span>
      <h3>Free delivery over <@ui.money freeShippingThreshold!0/></h3>
      <p>Flat-rate <@ui.money shippingFlatRate/> below the threshold, free above it.</p>
    </div>
    <div>
      <span class="trust-icon"><@ui.lucide "shield-check" 20/></span>
      <h3>Secure payments</h3>
      <p>Pay by card, PayPal or WebMoney over encrypted connections.</p>
    </div>
    <div>
      <span class="trust-icon"><@ui.lucide "rotate-ccw" 20/></span>
      <h3>Easy returns</h3>
      <p>30-day hassle-free returns on every order.</p>
    </div>
  </section>
</@ui.page>
