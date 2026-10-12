package com.example.ecommercedemo.web;

import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.orders.OrderService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AccountWebController {

    private final OrderService orderService;
    private final ShippingPresetService shippingPresetService;

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("orders", orderService.getOrdersForUser(user));
        model.addAttribute("presets",
                shippingPresetService.getShippingPresetPagedEntities(PageRequest.of(0, 20), user).getContent());
        return "account";
    }
}
