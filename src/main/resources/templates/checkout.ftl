<#import "partials/layout.ftl" as ui>
<#assign subtotal = total!0>
<#assign ship = shippingFee!0>
<#assign grand = (grandTotal)!(subtotal + ship)>
<#assign threshold = freeShippingThreshold!0>
<#assign progress = 100>
<#if threshold gt 0><#assign progress = subtotal / threshold * 100></#if>
<#if progress gt 100><#assign progress = 100></#if>

<@ui.page title="Checkout" description="Secure checkout — pay by card, PayPal or WebMoney.">
  <div class="page-head">
    <div>
      <span class="eyebrow">Secure checkout</span>
      <h1>Checkout</h1>
      <#if currentUser??><p>Signed in as ${currentUser.email}</p></#if>
    </div>
    <a class="btn btn-tertiary" href="/cart">Back to cart</a>
  </div>

  <#if paymentError??><div class="alert alert-error" role="alert">${paymentError}</div></#if>

  <#if isGuest>
    <div class="panel checkout-auth">
      <div class="checkout-auth-inner">
        <strong>Have an account?</strong>
        <a class="btn btn-primary btn-sm" href="/login?continue=/checkout">Sign in</a>
        <span class="muted">or continue as a guest below</span>
      </div>
      <p class="note">Guest checkout is available — you can order without creating an account.</p>
    </div>
  </#if>

  <div class="two-col">
    <form id="checkout-form" class="panel" method="post" action="/checkout" novalidate data-loading>
      <@ui.csrfField/>

      <div class="form-section">
        <#if !isGuest && presets?? && presets?size gt 0>
          <h2 class="panel-title">Delivery address</h2>
          <div class="saved-choices">
            <#list presets as preset>
              <label class="saved-choice">
                <input type="radio" name="shippingPresetId" value="${preset.id}" <#if (defaultPresetId?? && defaultPresetId == preset.id) || (selectedPresetId?? && selectedPresetId == preset.id)>checked</#if>>
                <span>
                  <strong>${preset.presetName!''} <#if preset.isDefault()><span class="chip chip-brand">Default</span></#if></strong>
                  <span class="muted">${preset.street!''}, ${preset.city!''}, ${preset.country!''}</span>
                </span>
              </label>
            </#list>
            <label class="saved-choice">
              <input type="radio" name="shippingPresetId" value="" <#if !(defaultPresetId??) && !(selectedPresetId??)>checked</#if>>
              <span>
                <strong>Use a new address</strong>
                <span class="muted">Enter the delivery details below</span>
              </span>
            </label>
          </div>
        </#if>
      </div>

      <div id="new-address-fields" class="form-section">
        <h2 class="panel-title">Delivery details</h2>
        <p class="note">We only use these details to deliver this order.</p>
        <div class="form-grid">
          <div class="field full">
            <label for="fullName">Full name</label>
            <input id="fullName" name="fullName" value="${(checkoutForm.fullName)!''}" required autocomplete="name">
            <#if fieldErrors?? && fieldErrors.fullName??><span class="err">${fieldErrors.fullName}</span></#if>
          </div>
          <div class="field">
            <label for="email">Email</label>
            <input id="email" type="email" name="email" value="${(checkoutForm.email)!''}" required autocomplete="email">
            <#if fieldErrors?? && fieldErrors.email??><span class="err">${fieldErrors.email}</span></#if>
          </div>
          <div class="field">
            <label for="phoneNumber">Phone</label>
            <input id="phoneNumber" name="phoneNumber" value="${(checkoutForm.phoneNumber)!''}" required autocomplete="tel">
            <#if fieldErrors?? && fieldErrors.phoneNumber??><span class="err">${fieldErrors.phoneNumber}</span></#if>
          </div>
          <div class="field">
            <label for="country">Country</label>
            <input id="country" name="country" value="${(checkoutForm.country)!''}" required autocomplete="country-name">
            <#if fieldErrors?? && fieldErrors.country??><span class="err">${fieldErrors.country}</span></#if>
          </div>
          <div class="field">
            <label for="state">State / county</label>
            <input id="state" name="state" value="${(checkoutForm.state)!''}" required autocomplete="address-level1">
            <#if fieldErrors?? && fieldErrors.state??><span class="err">${fieldErrors.state}</span></#if>
          </div>
          <div class="field">
            <label for="city">City</label>
            <input id="city" name="city" value="${(checkoutForm.city)!''}" required autocomplete="address-level2">
            <#if fieldErrors?? && fieldErrors.city??><span class="err">${fieldErrors.city}</span></#if>
          </div>
          <div class="field">
            <label for="street">Street</label>
            <input id="street" name="street" value="${(checkoutForm.street)!''}" required autocomplete="address-line1">
            <#if fieldErrors?? && fieldErrors.street??><span class="err">${fieldErrors.street}</span></#if>
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
            <input id="postalCode" name="postalCode" value="${(checkoutForm.postalCode)!''}" autocomplete="postal-code">
          </div>
          <#if !isGuest>
            <div id="save-address-row" class="field full">
              <label class="checkbox"><input type="checkbox" name="savePreset" value="true" <#if !(savePresetChecked??) || savePresetChecked>checked</#if>> Save this address to my account</label>
            </div>
            <div class="field">
              <label for="presetName">Address label</label>
              <input id="presetName" name="presetName" value="${(checkoutForm.presetName)!'Home'}" placeholder="Home">
            </div>
          </#if>
        </div>
      </div>

      <div class="form-section">
        <h2 class="panel-title">Payment method</h2>
        <p class="note">Sandbox gateway: <strong>${paymentProvider!''}</strong>. Cards ending in 0000 are declined.</p>
        <div class="radio-row pay-methods" role="radiogroup" aria-label="Payment method">
          <label class="radio pay-option">
            <input type="radio" name="method" value="CARD" <#if checkoutForm.cardSelected>checked</#if>>
            <svg class="pay-icon" viewBox="0 0 24 24" aria-hidden="true">
              <rect x="2" y="5" width="20" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/>
              <path d="M2 10h20" stroke="currentColor" stroke-width="1.7"/>
            </svg>
            <span>Credit / debit card</span>
          </label>
          <label class="radio pay-option">
            <input type="radio" name="method" value="PAYPAL" <#if checkoutForm.paypalSelected>checked</#if>>
            <svg class="pay-icon" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="11" fill="#0070ba"/>
              <text x="12" y="16" text-anchor="middle" font-size="13" font-weight="700" fill="#fff" font-family="system-ui, sans-serif">P</text>
            </svg>
            <span>PayPal</span>
          </label>
          <label class="radio pay-option">
            <input type="radio" name="method" value="WEBMONEY" <#if checkoutForm.webmoneySelected>checked</#if>>
            <svg class="pay-icon" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="11" fill="#0069b4"/>
              <text x="12" y="16" text-anchor="middle" font-size="12" font-weight="700" fill="#fff" font-family="system-ui, sans-serif">W</text>
            </svg>
            <span>WebMoney</span>
          </label>
        </div>

        <p id="redirect-note" class="note" hidden>You’ll be redirected to complete payment securely.</p>

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
      </div>

      <div class="actions">
        <button class="btn btn-primary btn-lg" type="submit">Pay <@ui.money grand/> securely</button>
      </div>
      <p class="note">By ordering you agree to our terms and 30-day returns policy.</p>
    </form>

    <aside class="panel" aria-label="Order summary">
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
        <a class="btn btn-ghost btn-block" href="/cart">Edit cart</a>
      </div>
    </aside>
  </div>
</@ui.page>
