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
      <h2 class="panel-title">Saved addresses</h2>
      <#if presets?size == 0>
        <p class="muted">No saved addresses yet — they’re stored automatically at checkout.</p>
      <#else>
        <#list presets as preset>
          <div class="address-row">
            <div>
              <strong>${preset.presetName!''}</strong>
              <#if preset.isDefault> <span class="chip">Default</span></#if>
              <div class="cat">${preset.street!''}, ${preset.city!''}, ${preset.country!''}</div>
            </div>
          </div>
        </#list>
      </#if>
    </div>
  </div>
</@ui.page>
