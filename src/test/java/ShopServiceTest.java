import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {

    @ParameterizedTest
    @MethodSource("invalidQuantities")
    void addOrderWithQuantities_rejectsNonPositiveOrNonFiniteQuantities(double quantity) {
        ProductRepo productRepo = new ProductRepo();
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        assertThrows(
                IllegalArgumentException.class,
                () -> shopService.addOrderWithQuantities(List.of(new OrderItem("1", quantity)))
        );
        assertEquals(10.0, productRepo.getProductById("1").orElseThrow().quantity());
        assertTrue(orderRepo.getOrders().isEmpty());
    }

    private static Stream<Double> invalidQuantities() {
        return Stream.of(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    void addOrderWithQuantities_aggregatesDuplicateProductLinesWhenReducingStock() {
        ProductRepo productRepo = new ProductRepo();
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        Order order = shopService.addOrderWithQuantities(List.of(
                new OrderItem("1", 4.0),
                new OrderItem("1", 6.0)
        ));

        assertEquals(List.of(new OrderItem("1", 4.0), new OrderItem("1", 6.0)), order.items());
        assertEquals(0.0, productRepo.getProductById("1").orElseThrow().quantity());
    }

    @Test
    void addOrderWithQuantities_handlesDecimalDuplicateLinesWithoutFloatingPointOverdraw() {
        ProductRepo productRepo = new ProductRepo();
        productRepo.addProduct(new Product("4", "Test", 0.3));
        ShopService shopService = new ShopService(productRepo, new OrderMapRepo(), new IdService());

        shopService.addOrderWithQuantities(List.of(
                new OrderItem("4", 0.1),
                new OrderItem("4", 0.2)
        ));

        assertEquals(0.0, productRepo.getProductById("4").orElseThrow().quantity());
    }

    @Test
    void addOrderWithQuantities_rejectsCombinedDuplicateQuantityWithoutChangingStock() {
        ProductRepo productRepo = new ProductRepo();
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        assertThrows(
                IllegalStateException.class,
                () -> shopService.addOrderWithQuantities(List.of(
                        new OrderItem("1", 6.0),
                        new OrderItem("1", 6.0)
                ))
        );

        assertEquals(10.0, productRepo.getProductById("1").orElseThrow().quantity());
        assertTrue(orderRepo.getOrders().isEmpty());
    }

    @Test
    void addOrderWithQuantities_rejectsInsufficientStockWithoutPartiallyReducingOtherProducts() {
        ProductRepo productRepo = new ProductRepo();
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        assertThrows(
                IllegalStateException.class,
                () -> shopService.addOrderWithQuantities(List.of(
                        new OrderItem("1", 2.0),
                        new OrderItem("2", 9.0)
                ))
        );

        assertEquals(10.0, productRepo.getProductById("1").orElseThrow().quantity());
        assertEquals(8.0, productRepo.getProductById("2").orElseThrow().quantity());
        assertTrue(orderRepo.getOrders().isEmpty());
    }

    @Test
    void addOrderWithQuantities_rejectsNonFiniteProductStock() {
        ProductRepo productRepo = new ProductRepo();
        productRepo.addProduct(new Product("4", "Test", Double.NaN));
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        assertThrows(
                IllegalStateException.class,
                () -> shopService.addOrderWithQuantities(List.of(new OrderItem("4", 1.0)))
        );
        assertTrue(orderRepo.getOrders().isEmpty());
    }

    @Test
    void addOrderTest() {
        // GIVEN
        ShopService shopService = new ShopService(
                new ProductRepo(),
                new OrderMapRepo(),
                new IdService()
        );
        List<String> productIds = List.of("1");
        Instant beforeCreation = Instant.now();

        // WHEN
        Order actual = shopService.addOrder(productIds);
        Instant afterCreation = Instant.now();

        // THEN
        assertNotNull(actual);
        assertEquals(List.of(new OrderItem("1", 1.0)), actual.items());
        assertNotNull(actual.id());
        assertDoesNotThrow(() -> UUID.fromString(actual.id()));
        assertEquals(OrderStatus.PROCESSING, actual.orderStatus());
        assertNotNull(actual.createdAt());
        assertFalse(actual.createdAt().isBefore(beforeCreation));
        assertFalse(actual.createdAt().isAfter(afterCreation));
    }

    @Test
    void addOrderTest_whenCalledTwice_generatesDifferentIds() {
        // GIVEN
        ShopService shopService = new ShopService(
                new ProductRepo(),
                new OrderMapRepo(),
                new IdService()
        );

        // WHEN
        Order firstOrder = shopService.addOrder(List.of("1"));
        Order secondOrder = shopService.addOrder(List.of("1"));

        // THEN
        assertNotNull(firstOrder);
        assertNotNull(secondOrder);
        assertNotEquals(firstOrder.id(), secondOrder.id());
    }

    @Test
    void addOrderTest_whenInvalidProductId_throwsException() {
        // GIVEN
        ShopService shopService = new ShopService(
                new ProductRepo(),
                new OrderMapRepo(),
                new IdService()
        );
        List<String> productIds = List.of("1", "999");

        // WHEN / THEN
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> shopService.addOrder(productIds)
        );

        assertEquals(
                "Product mit der Id: 999 konnte nicht bestellt werden!",
                exception.getMessage()
        );
    }

    @Test
    void getOrdersByStatus_returnsOnlyMatchingOrders() {
        // GIVEN
        ShopService shopService = new ShopService(
                new ProductRepo(),
                new OrderMapRepo(),
                new IdService()
        );

        Order processingOrder1 = shopService.addOrder(List.of("1"));
        Order processingOrder2 = shopService.addOrder(List.of("1"));

        shopService.updateOrder(processingOrder2.id(), OrderStatus.COMPLETED);

        // WHEN
        List<Order> actual = shopService.getOrdersByStatus(OrderStatus.PROCESSING);

        // THEN
        assertEquals(1, actual.size());
        assertEquals(processingOrder1.id(), actual.get(0).id());
        assertEquals(OrderStatus.PROCESSING, actual.get(0).orderStatus());
    }

    @Test
    void getOrdersByStatus_whenNoOrderMatches_returnsEmptyList() {
        // GIVEN
        ShopService shopService = new ShopService(
                new ProductRepo(),
                new OrderMapRepo(),
                new IdService()
        );
        shopService.addOrder(List.of("1"));

        // WHEN
        List<Order> actual = shopService.getOrdersByStatus(OrderStatus.IN_DELIVERY);

        // THEN
        assertTrue(actual.isEmpty());
    }

    @Test
    void getOldestOrderPerStatus_returnsOldestPerStatus() {
        // GIVEN
        ProductRepo productRepo = new ProductRepo();
        OrderMapRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(
                productRepo,
                orderRepo,
                new IdService()
        );

        Order processingOld = new Order(
                "p-old",
                List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING,
                Instant.parse("2024-01-01T10:00:00Z")
        );
        Order processingNew = new Order(
                "p-new",
                List.of(new OrderItem("1", 2.0)),
                OrderStatus.PROCESSING,
                Instant.parse("2024-01-01T11:00:00Z")
        );

        Order completedOld = new Order(
                "c-old",
                List.of(new OrderItem("2", 1.0)),
                OrderStatus.COMPLETED,
                Instant.parse("2024-01-01T09:00:00Z")
        );
        Order completedNew = new Order(
                "c-new",
                List.of(new OrderItem("2", 3.0)),
                OrderStatus.COMPLETED,
                Instant.parse("2024-01-01T12:00:00Z")
        );

        orderRepo.addOrder(processingOld);
        orderRepo.addOrder(processingNew);
        orderRepo.addOrder(completedOld);
        orderRepo.addOrder(completedNew);

        // WHEN
        Map<OrderStatus, Order> result = shopService.getOldestOrderPerStatus();

        // THEN
        assertEquals(2, result.size());
        assertEquals(processingOld, result.get(OrderStatus.PROCESSING));
        assertEquals(completedOld, result.get(OrderStatus.COMPLETED));
    }
}