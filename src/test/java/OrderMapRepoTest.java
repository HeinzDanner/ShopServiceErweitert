import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapRepoTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void updateOrder_changesOnlyStatusAndKeepsOriginalUnchanged() {
        OrderMapRepo repo = new OrderMapRepo();
        Order original = new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING, CREATED_AT);
        repo.addOrder(original);

        Order actual = repo.updateOrder("1", OrderStatus.IN_DELIVERY);

        Order expected = new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.IN_DELIVERY, CREATED_AT);

        assertEquals(expected, actual);
        assertEquals(expected, repo.getOrderById("1"));
        assertEquals(OrderStatus.PROCESSING, original.orderStatus());
    }

    @Test
    void getOrders() {
        // GIVEN
        OrderMapRepo repo = new OrderMapRepo();

        Order newOrder = new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING, CREATED_AT);
        repo.addOrder(newOrder);
        repo.addOrder(new Order("2", List.of(new OrderItem("2", 1.0)),
                OrderStatus.COMPLETED, CREATED_AT.plusSeconds(60)));

        // WHEN
        List<Order> actual = repo.getOrders();

        // THEN
        Set<Order> expected = Set.of(
                new Order("1", List.of(new OrderItem("1", 1.0)),
                        OrderStatus.PROCESSING, CREATED_AT),
                new Order("2", List.of(new OrderItem("2", 1.0)),
                        OrderStatus.COMPLETED, CREATED_AT.plusSeconds(60))
        );

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, Set.copyOf(actual));
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void getOrderById_preservesStatusAndCreatedAt(OrderStatus status) {
        // GIVEN
        OrderMapRepo repo = new OrderMapRepo();

        OrderItem item = new OrderItem("1", 1.0);
        Order newOrder = new Order("1", List.of(item), status, CREATED_AT);
        repo.addOrder(newOrder);
        repo.addOrder(new Order("2", List.of(item), OrderStatus.COMPLETED,
                CREATED_AT.plusSeconds(60)));

        // WHEN
        Order actual = repo.getOrderById("1");

        // THEN
        Order expected = new Order("1", List.of(new OrderItem("1", 1.0)),
                status, CREATED_AT);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void addOrder_preservesStatusAndCreatedAt(OrderStatus status) {
        // GIVEN
        OrderMapRepo repo = new OrderMapRepo();
        Order newOrder = new Order("1", List.of(new OrderItem("1", 1.0)),
                status, CREATED_AT);

        // WHEN
        Order actual = repo.addOrder(newOrder);

        // THEN
        Order expected = new Order("1", List.of(new OrderItem("1", 1.0)),
                status, CREATED_AT);

        assertEquals(expected, actual);
        assertEquals(expected, repo.getOrderById("1"));
        assertEquals(List.of(expected), repo.getOrders());
    }

    @Test
    void removeOrder() {
        // GIVEN
        OrderMapRepo repo = new OrderMapRepo();
        Order order = new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING, CREATED_AT);
        Order remainingOrder = new Order("2", List.of(new OrderItem("2", 1.0)),
                OrderStatus.IN_DELIVERY, CREATED_AT.plusSeconds(60));
        repo.addOrder(order);
        repo.addOrder(remainingOrder);

        // WHEN
        repo.removeOrder("1");

        // THEN
        assertNull(repo.getOrderById("1"));
        assertEquals(List.of(remainingOrder), repo.getOrders());
    }

    @Test
    void getOrders_whenEmpty_returnsEmptyList() {
        OrderMapRepo repo = new OrderMapRepo();

        assertEquals(List.of(), repo.getOrders());
    }

    @Test
    void getOrderById_whenUnknown_returnsNull() {
        OrderMapRepo repo = new OrderMapRepo();
        repo.addOrder(new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING, CREATED_AT));

        assertNull(repo.getOrderById("unknown"));
    }

    @Test
    void removeOrder_whenUnknown_keepsExistingOrders() {
        OrderMapRepo repo = new OrderMapRepo();
        Order order = new Order("1", List.of(new OrderItem("1", 1.0)),
                OrderStatus.PROCESSING, CREATED_AT);
        repo.addOrder(order);

        repo.removeOrder("unknown");

        assertEquals(List.of(order), repo.getOrders());
    }
}