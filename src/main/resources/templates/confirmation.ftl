<#import "partials/layout.ftl" as ui>
<@ui.page title="Order confirmed" description="Thank you — your order has been placed.">
  <div class="panel center">
    <p class="cat">Order #${order.id}</p>
    <h1>Thank you! Your order is confirmed.</h1>
    <p class="muted">Transaction ${transactionId!''}. Keep this reference handy — you can review the order any time from your account.</p>
    <div class="actions actions-center">
      <a class="btn btn-primary" href="/products">Continue shopping</a>
      <a class="btn btn-ghost" href="/account">View order history</a>
      <button class="btn btn-ghost" type="button" onclick="window.print()">Print receipt</button>
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
      <div class="summary-row"><span>Subtotal</span><span><@ui.money subtotal!0/></span></div>
      <div class="summary-row"><span>Delivery</span><span><#if (shippingFee!0) gt 0><@ui.money shippingFee/><#else>Free</#if></span></div>
      <div class="summary-row summary-total"><span>Total paid</span><span><@ui.money grandTotal!0/></span></div>
    </div>
    <div class="panel">
      <h2 class="panel-title">Delivery address</h2>
      <#if order.shippingPreset??>
        <address>
          ${order.shippingPreset.fullName!''}<br>
          ${order.shippingPreset.street!''} ${order.shippingPreset.building!''} ${order.shippingPreset.apartment!''}<br>
          ${order.shippingPreset.city!''}, ${order.shippingPreset.state!''} ${order.shippingPreset.postalCode!''}<br>
          ${order.shippingPreset.country!''}
        </address>
        <p class="note">Estimated delivery: 3–5 business days.</p>
      <#else>
        <p class="muted">No delivery address was saved with this order.</p>
      </#if>
    </div>
  </div>
</@ui.page>
