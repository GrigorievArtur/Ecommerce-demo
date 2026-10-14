<#import "partials/layout.ftl" as ui>
<@ui.page title="My account" description="View your order history, saved addresses and returns.">
  <div class="page-head">
    <h1>My account</h1>
    <p class="muted">${currentUser.email}</p>
  </div>

  <div class="stack">
    <div class="panel">
      <h2 class="panel-title">Order history</h2>
      <#if orders?size == 0>
        <p class="empty">You haven’t placed any orders yet. <a href="/products">Start shopping →</a></p>
      <#else>
        <#list orders as order>
          <#assign orderTotal = 0>
          <#list order.items as item>
            <#assign orderTotal = orderTotal + ui.lineTotal(item.priceSnapshot)>
          </#list>
          <div class="order-card">
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
          </div>
        </#list>
      </#if>
    </div>

    <div class="panel">
      <div class="page-head">
        <div>
          <h2 class="panel-title">Saved addresses</h2>
          <p class="muted">Used to speed up checkout.</p>
        </div>
        <a class="btn btn-primary btn-sm" href="/account/addresses/new">+ Add address</a>
      </div>
      <#if presets?size == 0>
        <p class="muted">No saved addresses yet — add one and it’ll be ready at checkout.</p>
      <#else>
        <div class="address-grid">
          <#list presets as preset>
            <div class="address-card<#if preset.isDefault()> is-default</#if>">
              <strong>${preset.presetName!''} <#if preset.isDefault()><span class="chip">Default</span></#if></strong>
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
            </div>
          </#list>
        </div>
      </#if>
    </div>
  </div>
</@ui.page>
