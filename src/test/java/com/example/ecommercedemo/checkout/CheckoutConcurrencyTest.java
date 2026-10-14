package com.example.ecommercedemo.checkout;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.repositories.orders.OrderRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.checkouts.CheckoutService;
import com.example.ecommercedemo.services.products.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the two concurrency guarantees around checkout:
 * <ol>
 *     <li>two carts buying the last unit cannot both win (no oversell);</li>
 *     <li>the same cart checked out twice produces one order (no double sell).</li>
 * </ol>
 * Real threads, real transactions, real row locks.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:concurrency-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false"
})
class CheckoutConcurrencyTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private Helpers helpers;

    @Test
    void twoCartsRaceForLastUnit_exactlyOneOrderWins() throws Exception {
        orderRepo.deleteAll();
        Product product = saveProduct(1);
        User buyerA = helpers.createMockUser();
        User buyerB = helpers.createMockUser();
        addItem(buyerA, product, 1);
        addItem(buyerB, product, 1);

        List<Boolean> outcomes = runConcurrently(() -> checkout(buyerA), () -> checkout(buyerB));

        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(orderRepo.count()).isEqualTo(1);
        assertThat(currentStock(product)).isZero();
    }

    @Test
    void sameCartCheckedOutTwice_createsOneOrder() throws Exception {
        orderRepo.deleteAll();
        Product product = saveProduct(5);
        User buyer = helpers.createMockUser();
        addItem(buyer, product, 1);

        List<Boolean> outcomes = runConcurrently(() -> checkout(buyer), () -> checkout(buyer));

        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(orderRepo.count()).isEqualTo(1);
        assertThat(currentStock(product)).isEqualTo(4);
    }

    @Test
    void concurrentStockDecrements_neverGoNegative() throws Exception {
        Product product = saveProduct(1);

        List<Boolean> outcomes = runConcurrently(() -> decrement(product), () -> decrement(product));

        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(currentStock(product)).isZero();
    }

    // ---- helpers ----

    private boolean checkout(User user) {
        try {
            checkoutService.checkoutNew(CartOwner.of(user, null), shippingAddress(), false);
            return true;
        } catch (Exception lostRace) {
            return false;
        }
    }

    private boolean decrement(Product product) {
        try {
            productService.decrementStock(product.getId(), 1);
            return true;
        } catch (Exception lostRace) {
            return false;
        }
    }

    private void addItem(User user, Product product, int quantity) {
        cartService.addItem(CartOwner.of(user, null),
                CreateItemDTO.builder().productId(product.getId()).quantity(quantity).build());
    }

    private Product saveProduct(int stock) {
        return productRepo.save(Product.builder()
                .name("Race Set " + UUID.randomUUID().toString().substring(0, 8))
                .description("A product used by the checkout concurrency test.")
                .category(Category.DEMO1)
                .stock(stock)
                .price(Price.builder()
                        .grossPrice(new BigDecimal("10.00"))
                        .percentageDiscount(BigDecimal.ZERO)
                        .quantity(BigDecimal.ONE)
                        .build())
                .build());
    }

    private CreateShippingPresetDTO shippingAddress() {
        return CreateShippingPresetDTO.builder()
                .presetName("Race")
                .isDefault(false)
                .fullName("Race Buyer")
                .email("race@example.com")
                .phoneNumber("5551234")
                .country("US")
                .state("CA")
                .city("Los Angeles")
                .street("1 Main St")
                .build();
    }

    private int currentStock(Product product) {
        return productRepo.findById(product.getId()).orElseThrow().getStock();
    }

    @SafeVarargs
    private final List<Boolean> runConcurrently(Callable<Boolean>... tasks) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tasks.length);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(pool.submit(() -> {
                start.await();
                return task.call();
            }));
        }
        start.countDown();
        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> future : futures) {
            results.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdownNow();
        return results;
    }
}
