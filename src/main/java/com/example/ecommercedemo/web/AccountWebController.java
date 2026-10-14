package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.exceptions.ResourceNotFoundException;
import com.example.ecommercedemo.services.orders.OrderService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import com.example.ecommercedemo.web.forms.AddressForm;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountWebController {

    private static final int MAX_ADDRESSES = 20;

    private final OrderService orderService;
    private final ShippingPresetService shippingPresetService;

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal User user, Model model) {
        populateAccountModel(user, model);
        return "account";
    }

    @GetMapping("/account/addresses/new")
    public String newAddressForm(@AuthenticationPrincipal User user, Model model) {
        AddressForm form = new AddressForm();
        form.setFullName((user.getFirstname() + " " + user.getLastname()).trim());
        form.setEmail(user.getEmail());
        model.addAttribute("addressForm", form);
        model.addAttribute("mode", "create");
        return "account/address-form";
    }

    @GetMapping("/account/addresses/{id}/edit")
    public String editAddressForm(@PathVariable Long id,
                                  @AuthenticationPrincipal User user,
                                  Model model) {
        ShippingPreset preset = requireOwnedPreset(id, user);
        model.addAttribute("addressForm", AddressForm.from(preset));
        model.addAttribute("addressId", id);
        model.addAttribute("mode", "edit");
        return "account/address-form";
    }

    @PostMapping("/account/addresses")
    public String createAddress(@Valid @ModelAttribute("addressForm") AddressForm form,
                                BindingResult binding,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            return "account/address-form";
        }
        shippingPresetService.saveShippingPreset(form.toCreateDTO(), user);
        redirect.addFlashAttribute("flash", "Address saved.");
        return "redirect:/account";
    }

    @PostMapping("/account/addresses/{id}")
    public String updateAddress(@PathVariable Long id,
                                @Valid @ModelAttribute("addressForm") AddressForm form,
                                BindingResult binding,
                                @AuthenticationPrincipal User user,
                                Model model,
                                RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("addressId", id);
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            return "account/address-form";
        }
        shippingPresetService.updateShippingPreset(id, form.toUpdateDTO(), user);
        redirect.addFlashAttribute("flash", "Address updated.");
        return "redirect:/account";
    }

    @PostMapping("/account/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Long id,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirect) {
        shippingPresetService.deleteShippingPreset(id, user);
        redirect.addFlashAttribute("flash", "Address removed.");
        return "redirect:/account";
    }

    @PostMapping("/account/addresses/{id}/default")
    public String makeDefault(@PathVariable Long id,
                              @AuthenticationPrincipal User user,
                              RedirectAttributes redirect) {
        UpdateShippingPresetDTO defaultOnly = new UpdateShippingPresetDTO();
        defaultOnly.setIsDefault(true);
        shippingPresetService.updateShippingPreset(id, defaultOnly, user);
        redirect.addFlashAttribute("flash", "Default address updated.");
        return "redirect:/account";
    }

    private ShippingPreset requireOwnedPreset(Long id, User user) {
        try {
            return shippingPresetService.getShippingPresetEntity(id, user);
        } catch (EntityNotFoundException notFound) {
            throw new ResourceNotFoundException("Address " + id + " was not found");
        }
    }

    private void populateAccountModel(User user, Model model) {
        model.addAttribute("orders", orderService.getOrdersForUser(user));
        model.addAttribute("presets", shippingPresetService
                .getShippingPresetPagedEntities(PageRequest.of(0, MAX_ADDRESSES), user)
                .getContent());
    }
}
