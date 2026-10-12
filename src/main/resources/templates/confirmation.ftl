<#import "partials/layout.ftl" as ui>
<@ui.page title="Order confirmed" description="Thank you — your order has been placed.">
  <div class="panel center">
    <h1>Thank you! Your order is confirmed.</h1>
    <p class="cat">Order reference #${order.id} · Transaction ${transactionId!''}</p>
    <p>We’ve emailed a receipt to you. You can track this order any time from your account.</p>
    <div class="actions actions-center">
      <a class="btn btn-primary" href="/products">Continue shopping</a>
      <a class="btn btn-ghost" href="/account">View order history</a>
    </div>
  </div>

  <div class="two-col panel-spaced">
    <div class="panel">
      <h2 class="panel-title">Items</h2>
      <#list order.items as item>
        <#assign line = ui.lineTotal(item.priceSnapshot)>
        <#assign qty = (item.priceSnapshot.quantity)!1>
        <div class="summary-row">
          <span>${(item.product.name)!''} × ${qty?string("0")}</span>
          <span><@ui.money line/></span>
        </div>
      </#list>
    </div>
    <div class="panel">
      <h2 class="panel-title">Delivery address</h2>
      <#if order.shippingPreset??>
        <p>
          ${order.shippingPreset.fullName!''}<br>
          ${order.shippingPreset.street!''} ${order.shippingPreset.building!''}<br>
          ${order.shippingPreset.city!''}, ${order.shippingPreset.state!''} ${order.shippingPreset.postalCode!''}<br>
          ${order.shippingPreset.country!''}
        </p>
      </#if>
    </div>
  </div>
</@ui.page>
