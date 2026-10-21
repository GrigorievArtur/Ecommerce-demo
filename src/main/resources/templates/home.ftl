<#import "partials/layout.ftl" as ui>
<@ui.page title="Home" description="Shop quality sets, minifigures and accessories with fast, secure checkout and easy returns.">
  <section class="hero">
    <span class="eyebrow">New season · fresh bricks</span>
    <h1>Build something brilliant today.</h1>
    <p>Thousands of bricks, minifigures and accessories — curated, stocked and ready to ship. Free returns within 30 days, secure checkout every time.</p>
    <div class="actions">
      <a class="btn btn-primary btn-lg" href="/products">Shop the catalogue</a>
      <a class="btn btn-on-dark" href="/products?sort=price-asc">Best value first</a>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <h2>Shop by category</h2>
        <p>Pick a shelf and dive in.</p>
      </div>
      <a class="btn btn-tertiary" href="/products">View all products</a>
    </div>
    <div class="cat-tiles">
      <#list categories as c>
        <a class="cat-tile" href="/products?category=${c.name}">
          <strong>${c.label}</strong>
          <span>Browse the range</span>
        </a>
      </#list>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <h2>New arrivals</h2>
        <p>The latest additions to our shelves.</p>
      </div>
      <a class="btn btn-tertiary" href="/products">View all products</a>
    </div>

    <#if products?size == 0>
      <@ui.emptyState icon="box" title="The shelves are being stocked" text="No products are live yet. Add your first product in the admin panel and it will appear here." actionHref="/admin/products/new" actionLabel="Add a product"/>
    <#else>
      <div class="grid">
        <#list products as p><@ui.productCard p/></#list>
      </div>
    </#if>
  </section>

  <section class="trust" aria-label="Why shop with us">
    <div>
      <h3>Fast delivery</h3>
      <p>Orders dispatched within 24 hours on business days.</p>
    </div>
    <div>
      <h3>Free delivery over <@ui.money freeShippingThreshold!0/></h3>
      <p>Flat-rate <@ui.money shippingFlatRate/> below the threshold, free above it.</p>
    </div>
    <div>
      <h3>Secure payments</h3>
      <p>Pay by card, PayPal or WebMoney over encrypted connections.</p>
    </div>
    <div>
      <h3>Easy returns</h3>
      <p>30-day hassle-free returns on every order.</p>
    </div>
  </section>
</@ui.page>
