import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @TempDir
    Path tempDir;

    @Test
    void processTransactions_createsOrdersUpdatesStatusAndReducesStock() throws IOException {
        Path transactions = tempDir.resolve("transactions.txt");
        Files.writeString(transactions, """
                addOrder A 1:2.5 2:1 3:0.5
                addOrder B 1:1
                setStatus A COMPLETED
                printOrders
                """);
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();
        ShopService shopService = new ShopService(productRepo, orderRepo, new IdService());

        Main.processTransactions(transactions, productRepo, orderRepo, shopService);

        List<Order> orders = orderRepo.getOrders();
        assertEquals(2, orders.size());
        assertTrue(orders.stream().anyMatch(order ->
                order.orderStatus() == OrderStatus.COMPLETED
                        && order.items().equals(List.of(
                        new OrderItem("1", 2.5),
                        new OrderItem("2", 1.0),
                        new OrderItem("3", 0.5)
                ))
        ));
        assertTrue(orders.stream().anyMatch(order ->
                order.orderStatus() == OrderStatus.PROCESSING
                        && order.items().equals(List.of(new OrderItem("1", 1.0)))
        ));
        assertEquals(6.5, productRepo.getProductById("1").orElseThrow().quantity());
        assertEquals(7.0, productRepo.getProductById("2").orElseThrow().quantity());
        assertEquals(12.0, productRepo.getProductById("3").orElseThrow().quantity());
    }
}
