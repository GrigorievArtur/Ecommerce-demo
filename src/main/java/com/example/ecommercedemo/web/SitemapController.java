package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.services.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * SEO endpoints: a dynamic {@code sitemap.xml} and {@code robots.txt}.
 */
@Controller
@RequiredArgsConstructor
public class SitemapController {

    private final ProductService productService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<ProductDTO> products = productService
                .getAllProducts(new ProductFilter(), PageRequest.of(0, 5000))
                .getContent();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        appendUrl(xml, "/", "1.0");
        appendUrl(xml, "/products", "0.9");
        appendUrl(xml, "/returns", "0.3");
        products.forEach(product -> appendUrl(xml, "/products/" + product.getId(), "0.8"));
        xml.append("</urlset>\n");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return "User-agent: *\n"
                + "Allow: /\n"
                + "Disallow: /admin\n"
                + "Disallow: /account\n"
                + "Disallow: /cart\n"
                + "Disallow: /checkout\n"
                + "Sitemap: " + baseUrl + "/sitemap.xml\n";
    }

    private void appendUrl(StringBuilder xml, String path, String priority) {
        xml.append("  <url><loc>").append(baseUrl).append(path)
                .append("</loc><priority>").append(priority).append("</priority></url>\n");
    }
}
