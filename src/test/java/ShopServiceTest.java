import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShopServiceTest {

    @Test
    void addOrderTest() {
        //GIVEN
        ShopService shopService = new ShopService();
        List<String> productsIds = List.of("1");
        Instant beforeCreation = Instant.now();

        //WHEN
        Order actual = shopService.addOrder(productsIds);
        Instant afterCreation = Instant.now();

        //THEN
        assertNotNull(actual);
        assertEquals(List.of(new Product("1", "Apfel")), actual.products());
        assertNotNull(actual.id());
        assertDoesNotThrow(() -> UUID.fromString(actual.id()));
        assertEquals(OrderStatus.PROCESSING, actual.orderStatus());
        assertNotNull(actual.createdAt());
        assertFalse(actual.createdAt().isBefore(beforeCreation));
        assertFalse(actual.createdAt().isAfter(afterCreation));
    }

    @Test
    void addOrderTest_whenCalledTwice_generatesDifferentIds() {
        ShopService shopService = new ShopService();

        Order firstOrder = shopService.addOrder(List.of("1"));
        Order secondOrder = shopService.addOrder(List.of("1"));

        assertNotNull(firstOrder);
        assertNotNull(secondOrder);
        assertNotEquals(firstOrder.id(), secondOrder.id());
    }

    @Test
    void addOrderTest_whenInvalidProductId_throwsException() {
        // GIVEN
        ShopService shopService = new ShopService();
        List<String> productIds = List.of("1", "2");

        // WHEN / THEN
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> shopService.addOrder(productIds)
        );

        assertEquals(
                "Product mit der Id: 2 konnte nicht bestellt werden!",
                exception.getMessage()
        );
    }


    @Test
    void getOrdersByStatus_returnsOnlyMatchingOrders() {
        // GIVEN
        ShopService shopService = new ShopService();

        Order processingOrder1 = shopService.addOrder(List.of("1"));
        Order processingOrder2 = shopService.addOrder(List.of("1"));

        // Status einer Bestellung ändern, damit Mischung entsteht
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
        ShopService shopService = new ShopService();
        shopService.addOrder(List.of("1"));

        // WHEN
        List<Order> actual = shopService.getOrdersByStatus(OrderStatus.IN_DELIVERY);

        // THEN
        assertTrue(actual.isEmpty());
    }
}
