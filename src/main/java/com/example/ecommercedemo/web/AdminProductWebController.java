package com.example.ecommercedemo.web;

import com.example.ecommercedemo.analytics.AnalyticsService;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.services.products.ProductService;
import com.example.ecommercedemo.web.forms.ProductForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductWebController {

    private static final int PAGE_SIZE = 20;

    private final ProductService productService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("summary", analyticsService.summary());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(name = "q", required = false) String query,
                       Model model) {
        ProductFilter filter = ProductFilter.builder().query(query).build();
        model.addAttribute("products", productService.getAllProducts(
                filter,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"))));
        model.addAttribute("q", query);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("mode", "create");
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("productForm", ProductForm.from(productService.getProductById(id)));
        model.addAttribute("productId", id);
        model.addAttribute("mode", "edit");
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String save(@Valid @ModelAttribute("productForm") ProductForm form,
                       BindingResult binding,
                       @RequestParam(required = false) Long id,
                       Model model,
                       RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("mode", id == null ? "create" : "edit");
            model.addAttribute("productId", id);
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            return "admin/product-form";
        }
        if (id == null) {
            productService.createProduct(form.toCreateDTO());
            redirect.addFlashAttribute("flash", "Product created.");
        } else {
            productService.updateProductById(id, form.toUpdateDTO());
            redirect.addFlashAttribute("flash", "Product updated.");
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        productService.deleteProductById(id);
        redirect.addFlashAttribute("flash", "Product deleted.");
        return "redirect:/admin/products";
    }
}
