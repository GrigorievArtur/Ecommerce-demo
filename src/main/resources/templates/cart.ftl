<#import "partials/layout.ftl" as ui>
<#assign subtotal = (cart.price.grossPrice)!0>
<#assign ship = shippingFee!0>
<#assign grand = subtotal + ship>
<#assign threshold = freeShippingThreshold!0>
<#assign progress = 100>
<#if threshold gt 0><#assign progress = subtotal / threshold * 100></#if>
<#if progress gt 100><#assign progress = 100></#if>

<@ui.page title="Your cart" description="Review the items in your cart and proceed to secure checkout.">
  <div class="page-head">
    <h1>Your cart</h1>
    <#if cart.items?size gt 0>
      <form class="inline" method="post" action="/cart/clear" onsubmit="return confirm('Remove all items from your cart?');">
        <@ui.csrfField/>
        <button class="link-danger" type="submit">Clear cart</button>
      </form>
    </#if>
  </div>

  <#if cart.items?size == 0>
    <p class="empty">Your cart is empty. <a href="/products">Start shopping →</a></p>
  <#else>
    <div class="two-col">
      <div class="panel">
        <#list cart.items as item>
          <#assign snap = item.priceSnapshot>
          <#assign line = ui.lineTotal(snap)>
          <#assign qty = (snap.quantity)!1>
          <#assign pname = (item.product.name)!''>
          <div class="cart-item">
            <a href="/products/${(item.product.id)!''}">
              <img src="<@ui.productImage item.product/>"
                   alt="${pname}" onerror="this.onerror=null;this.src='/images/placeholder.svg'">
            </a>
            <div>
              <a class="cart-name" href="/products/${(item.product.id)!''}">${pname}</a>
              <div class="cat">${(item.product.categoryLabel)!''}</div>
              <div class="cart-controls">
                <form method="post" action="/cart/items/${item.product.id}/decrement">
                  <@ui.csrfField/>
                  <button class="icon-btn" type="submit" aria-label="Decrease quantity of ${pname}">−</button>
                </form>
                <span class="qty" aria-label="Quantity">${qty?string("0")}</span>
                <form method="post" action="/cart/items">
                  <@ui.csrfField/>
                  <input type="hidden" name="productId" value="${item.product.id}">
                  <input type="hidden" name="quantity" value="1">
                  <button class="icon-btn" type="submit" aria-label="Increase quantity of ${pname}">+</button>
                </form>
                <form method="post" action="/cart/items/${item.product.id}/remove">
                  <@ui.csrfField/>
                  <button class="link-danger" type="submit">Remove</button>
                </form>
              </div>
            </div>
            <div class="cart-price"><@ui.money line/></div>
          </div>
        </#list>
      </div>

      <div class="panel">
        <h2 class="panel-title">Order summary</h2>
        <div class="summary-row"><span>Items</span><span>${cartCount}</span></div>
        <div class="summary-row"><span>Subtotal</span><span><@ui.money subtotal/></span></div>
        <div class="summary-row"><span>Delivery</span><span><#if ship gt 0><@ui.money ship/><#else>Free</#if></span></div>
        <div class="summary-row summary-total"><span>Total</span><span><@ui.money grand/></span></div>

        <#if amountUntilFreeShipping?? && amountUntilFreeShipping gt 0>
          <div class="free-ship">
            <div class="free-ship-bar"><span style="width: ${progress?string("0")}%"></span></div>
            <p>Add <@ui.money amountUntilFreeShipping/> more for free delivery.</p>
          </div>
        <#else>
          <div class="free-ship is-met"><p>You’ve unlocked free delivery.</p></div>
        </#if>

        <div class="btn-stack">
          <a class="btn btn-primary btn-block" href="/checkout">Proceed to checkout</a>
          <a class="btn btn-ghost btn-block" href="/products">Continue shopping</a>
        </div>
      </div>
    </div>
  </#if>
</@ui.page>
