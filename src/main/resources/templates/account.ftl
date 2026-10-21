<#import "partials/layout.ftl" as ui>
<@ui.page title="My account" description="View your order history, saved addresses and returns.">
  <div class="page-head">
    <div>
      <span class="eyebrow">Account</span>
      <h1>My account</h1>
      <p>${currentUser.email}</p>
    </div>
    <a class="btn btn-ghost" href="/returns">Returns &amp; refunds</a>
  </div>

  <div class="stack">
    <section class="panel">
      <h2 class="panel-title">Order history</h2>
      <#if orders?size == 0>
        <@ui.emptyState icon="orders" title="No orders yet" text="When you place an order it will appear here, with a one-click way to start a return." actionHref="/products" actionLabel="Browse the catalogue"/>
      <#else>
        <#list orders as order>
          <#assign orderTotal = 0>
          <#list order.items as item>
            <#assign orderTotal = orderTotal + ui.lineTotal(item.priceSnapshot)>
          </#list>
          <article class="order-card">
            <div class="order-head">
              <div>
                <strong>Order #${order.id}</strong>
                <div class="cat">${order.items?size} item<#if order.items?size != 1>s</#if></div>
              </div>
              <div class="price"><span class="price-now"><@ui.money orderTotal/></span></div>
            </div>
            <div class="order-items">
              <#list order.items as item>
                <div>${(item.product.name)!''} × ${((item.priceSnapshot.quantity)!1)?string("0")}</div>
              </#list>
            </div>
            <div class="actions">
              <a class="btn btn-ghost btn-sm" href="/returns">Request a return</a>
            </div>
          </article>
        </#list>
      </#if>
    </section>

    <section class="panel">
      <div class="page-head">
        <div>
          <h2 class="panel-title">Saved addresses</h2>
          <p class="muted">Used to speed up checkout.</p>
        </div>
        <a class="btn btn-primary btn-sm" href="/account/addresses/new">Add address</a>
      </div>
      <#if presets?size == 0>
        <@ui.emptyState icon="pin" title="No saved addresses" text="Add an address once and it will be ready to pick at checkout." actionHref="/account/addresses/new" actionLabel="Add your first address"/>
      <#else>
        <div class="address-grid">
          <#list presets as preset>
            <article class="address-card<#if preset.isDefault()> is-default</#if>">
              <strong>${preset.presetName!''} <#if preset.isDefault()><span class="chip chip-brand">Default</span></#if></strong>
              <address>
                ${preset.fullName!''}<br>
                ${preset.street!''} ${preset.building!''} ${preset.apartment!''}<br>
                ${preset.city!''}, ${preset.state!''} ${preset.postalCode!''}<br>
                ${preset.country!''}
              </address>
              <div class="address-actions">
                <a class="btn btn-ghost btn-sm" href="/account/addresses/${preset.id}/edit">Edit</a>
                <#if !preset.isDefault()>
                  <form class="inline" method="post" action="/account/addresses/${preset.id}/default">
                    <@ui.csrfField/>
                    <button class="btn btn-ghost btn-sm" type="submit">Make default</button>
                  </form>
                </#if>
                <form class="inline" method="post" action="/account/addresses/${preset.id}/delete" onsubmit="return confirm('Delete this address?');">
                  <@ui.csrfField/>
                  <button class="link-danger" type="submit">Delete</button>
                </form>
              </div>
            </article>
          </#list>
        </div>
      </#if>
    </section>
  </div>
</@ui.page>
