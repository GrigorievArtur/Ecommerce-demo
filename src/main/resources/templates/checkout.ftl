<#import "partials/layout.ftl" as ui>
<@ui.page title="Checkout" description="Secure checkout — enter your delivery details and pay by card or wallet.">
  <div class="page-head">
    <h1>Checkout</h1>
    <p class="muted">Signed in as ${currentUser.email}</p>
  </div>

  <#if paymentError??><div class="alert alert-error">${paymentError}</div></#if>

  <div class="two-col">
    <form class="panel" method="post" action="/checkout" novalidate>
      <@ui.csrfField/>

      <h2 class="panel-title">Delivery details</h2>
      <div class="form-grid">
        <div class="field full">
          <label for="fullName">Full name</label>
          <input id="fullName" name="fullName" value="${(checkoutForm.fullName)!''}" required>
          <#if fieldErrors?? && fieldErrors.fullName??><span class="err">${fieldErrors.fullName}</span></#if>
        </div>
        <div class="field">
          <label for="email">Email</label>
          <input id="email" type="email" name="email" value="${(checkoutForm.email)!''}" required>
          <#if fieldErrors?? && fieldErrors.email??><span class="err">${fieldErrors.email}</span></#if>
        </div>
        <div class="field">
          <label for="phoneNumber">Phone</label>
          <input id="phoneNumber" name="phoneNumber" value="${(checkoutForm.phoneNumber)!''}" required>
          <#if fieldErrors?? && fieldErrors.phoneNumber??><span class="err">${fieldErrors.phoneNumber}</span></#if>
        </div>
        <div class="field">
          <label for="country">Country</label>
          <input id="country" name="country" value="${(checkoutForm.country)!''}" required>
        </div>
        <div class="field">
          <label for="state">State / county</label>
          <input id="state" name="state" value="${(checkoutForm.state)!''}" required>
        </div>
        <div class="field">
          <label for="city">City</label>
          <input id="city" name="city" value="${(checkoutForm.city)!''}" required>
        </div>
        <div class="field">
          <label for="street">Street</label>
          <input id="street" name="street" value="${(checkoutForm.street)!''}" required>
        </div>
        <div class="field">
          <label for="building">Building</label>
          <input id="building" name="building" value="${(checkoutForm.building)!''}">
        </div>
        <div class="field">
          <label for="apartment">Apartment</label>
          <input id="apartment" name="apartment" value="${(checkoutForm.apartment)!''}">
        </div>
        <div class="field">
          <label for="postalCode">Postal code</label>
          <input id="postalCode" name="postalCode" value="${(checkoutForm.postalCode)!''}">
        </div>
      </div>

      <h2 class="panel-title panel-title-spaced">Payment</h2>
      <p class="note">Sandbox gateway: <strong>${paymentProvider!''}</strong>. Use any card except one ending in 0000.</p>
      <div class="radio-row">
        <label class="radio"><input type="radio" name="method" value="CARD" <#if checkoutForm.cardSelected>checked</#if>> Credit / debit card</label>
        <label class="radio"><input type="radio" name="method" value="WALLET" <#if checkoutForm.walletSelected>checked</#if>> Wallet</label>
      </div>

      <div id="card-fields" class="form-grid card-fields">
        <div class="field full">
          <label for="cardNumber">Card number</label>
          <input id="cardNumber" name="cardNumber" value="${(checkoutForm.cardNumber)!''}" inputmode="numeric" autocomplete="cc-number" placeholder="4242 4242 4242 4242">
        </div>
        <div class="field full">
          <label for="cardHolder">Name on card</label>
          <input id="cardHolder" name="cardHolder" value="${(checkoutForm.cardHolder)!''}" autocomplete="cc-name">
        </div>
        <div class="field">
          <label for="expiry">Expiry (MM/YY)</label>
          <input id="expiry" name="expiry" value="${(checkoutForm.expiry)!''}" placeholder="12/28">
        </div>
        <div class="field">
          <label for="cvc">CVC</label>
          <input id="cvc" name="cvc" value="${(checkoutForm.cvc)!''}" inputmode="numeric" autocomplete="cc-csc" placeholder="123">
        </div>
      </div>

      <div class="actions">
        <button class="btn btn-primary" type="submit">Pay <@ui.money total/> securely</button>
      </div>
      <p class="note">By ordering you agree to our terms and 30-day returns policy.</p>
    </form>

    <div class="panel">
      <h2 class="panel-title">Order summary</h2>
      <#list cart.items as item>
        <#assign snap = item.priceSnapshot>
        <#assign line = ui.lineTotal(snap)>
        <#assign qty = (snap.quantity)!1>
        <div class="summary-row">
          <span>${(item.product.name)!''} × ${qty?string("0")}</span>
          <span><@ui.money line/></span>
        </div>
      </#list>
      <div class="summary-row"><span>Delivery</span><span>Free</span></div>
      <div class="summary-row summary-total"><span>Total</span><span><@ui.money total/></span></div>
      <div class="btn-stack">
        <a class="btn btn-ghost btn-block" href="/cart">Edit cart</a>
      </div>
    </div>
  </div>
</@ui.page>
