package com.example.ecommercedemo.configs;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.enums.products.MediaTypes;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.products.ProductMedia;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.repositories.users.UserRepo;
import com.example.ecommercedemo.services.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a small demo catalogue and an admin account on first start.
 * Disable with {@code app.seed.enabled=false} in production.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final UserService userService;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Value("${app.admin.email:admin@legostore.test}")
    private String adminEmail;

    @Value("${app.admin.password:admin12345}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (productRepo.count() == 0) {
            seedProducts();
        }
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            User admin = userService.createUser(CreateUserDTO.builder()
                    .firstname("Store")
                    .lastname("Admin")
                    .email(adminEmail)
                    .password(adminPassword)
                    .build());
            userService.makeAdmin(admin.getId());
        }
    }

    private void seedProducts() {
        productRepo.saveAll(List.of(
                product("Galaxy Explorer Set", "Build a glowing 1,200-piece starship with rotating engines and a display stand.",
                        "129.99", "10", 24, Category.DEMO1, "lego-1"),
                product("Castle Siege Deluxe", "A medieval fortress with drawbridge, catapult and six minifigures ready for battle.",
                        "89.50", "0", 12, Category.DEMO1, "lego-2"),
                product("City Fire Station", "Three-storey fire station with a working garage, truck and four emergency responders.",
                        "74.00", "15", 8, Category.DEMO1, "lego-3"),
                product("Space Ranger Minifigure", "Collectible space ranger with jetpack, helmet and interchangeable visor.",
                        "12.99", "0", 120, Category.DEMO2, "lego-4"),
                product("Royal Knight Minifigure", "Armoured knight with sword, shield and removable cape.",
                        "9.99", "5", 75, Category.DEMO2, "lego-5"),
                product("Pirate Captain Minifigure", "Swashbuckling captain with cutlass, parrot and treasure map.",
                        "11.50", "0", 40, Category.DEMO2, "lego-6"),
                product("Storage Brick 4-Stud", "Stackable storage brick that keeps thousands of loose pieces tidy.",
                        "24.99", "20", 60, Category.DEMO3, "lego-7"),
                product("Baseplate 32x32", "Classic grey studded baseplate — the foundation for any build.",
                        "16.00", "0", 90, Category.DEMO3, "lego-8"),
                product("Minifigure Display Case", "Wall-mountable case that shows off up to sixteen favourite minifigures.",
                        "39.95", "10", 5, Category.DEMO3, "lego-9")
        ));
    }

    private Product product(String name, String description, String price, String discount,
                            int stock, Category category, String imageSeed) {
        return Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .stock(stock)
                .price(Price.builder()
                        .grossPrice(new BigDecimal(price))
                        .percentageDiscount(new BigDecimal(discount))
                        .quantity(BigDecimal.ONE)
                        .build())
                .mediaList(List.of(ProductMedia.builder()
                        .mediaType(MediaTypes.IMAGE)
                        .url("https://picsum.photos/seed/" + imageSeed + "/600/450")
                        .build()))
                .build();
    }
}
