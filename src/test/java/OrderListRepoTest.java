import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderListRepoTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void getOrders() {
        //GIVEN
        OrderListRepo repo = new OrderListRepo();

        Product product = new Product("1", "Apfel");
        Order newOrder = new Order("1", List.of(product),
                OrderStatus.PROCESSING,
                CREATED_AT
        );
        repo.addOrder(newOrder);
        Order secondOrder = new Order("2", List.of(new Product("2", "Banane")),
                OrderStatus.COMPLETED, CREATED_AT.plusSeconds(60));
        repo.addOrder(secondOrder);

        //WHEN
        List<Order> actual = repo.getOrders();

        //THEN
        Product product1 = new Product("1", "Apfel");
        List<Order> expected = List.of(new Order("1", List.of(product1),
                OrderStatus.PROCESSING,
                CREATED_AT),
                new Order("2", List.of(new Product("2", "Banane")),
                        OrderStatus.COMPLETED, CREATED_AT.plusSeconds(60)));

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void getOrderById_preservesStatusAndCreatedAt(OrderStatus status) {
        //GIVEN
        OrderListRepo repo = new OrderListRepo();

        Product product = new Product("1", "Apfel");
        Order newOrder = new Order("1", List.of(product),
                status,
                CREATED_AT
        );
        repo.addOrder(newOrder);
        repo.addOrder(new Order("2", List.of(product), OrderStatus.COMPLETED,
                CREATED_AT.plusSeconds(60)));

        //WHEN
        Order actual = repo.getOrderById("1");

        //THEN
        Product product1 = new Product("1", "Apfel");
        Order expected = new Order("1", List.of(product1),
                status,
                CREATED_AT
        );

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void addOrder_preservesStatusAndCreatedAt(OrderStatus status) {
        //GIVEN
        OrderListRepo repo = new OrderListRepo();
        Product product = new Product("1", "Apfel");
        Order newOrder = new Order("1", List.of(product),
                status,
                CREATED_AT
        );

        //WHEN
        Order actual = repo.addOrder(newOrder);

        //THEN
        Product product1 = new Product("1", "Apfel");
        Order expected = new Order("1", List.of(product1),
                status,
                CREATED_AT
        );
        assertEquals(expected, actual);
        assertEquals(expected, repo.getOrderById("1"));
        assertEquals(List.of(expected), repo.getOrders());
    }

    @Test
    void removeOrder() {
        //GIVEN
        OrderListRepo repo = new OrderListRepo();
        Order order = new Order("1", List.of(new Product("1", "Apfel")),
                OrderStatus.PROCESSING, CREATED_AT);
        Order remainingOrder = new Order("2", List.of(new Product("2", "Banane")),
                OrderStatus.IN_DELIVERY, CREATED_AT.plusSeconds(60));
        repo.addOrder(order);
        repo.addOrder(remainingOrder);

        //WHEN
        repo.removeOrder("1");

        //THEN
        assertNull(repo.getOrderById("1"));
        assertEquals(List.of(remainingOrder), repo.getOrders());
    }

    @Test
    void getOrders_whenEmpty_returnsEmptyList() {
        OrderListRepo repo = new OrderListRepo();

        assertEquals(List.of(), repo.getOrders());
    }

    @Test
    void getOrderById_whenUnknown_returnsNull() {
        OrderListRepo repo = new OrderListRepo();
        repo.addOrder(new Order("1", List.of(new Product("1", "Apfel")),
                OrderStatus.PROCESSING, CREATED_AT));

        assertNull(repo.getOrderById("unknown"));
    }

    @Test
    void removeOrder_whenUnknown_keepsExistingOrders() {
        OrderListRepo repo = new OrderListRepo();
        Order order = new Order("1", List.of(new Product("1", "Apfel")),
                OrderStatus.PROCESSING, CREATED_AT);
        repo.addOrder(order);

        repo.removeOrder("unknown");

        assertEquals(List.of(order), repo.getOrders());
    }
}
