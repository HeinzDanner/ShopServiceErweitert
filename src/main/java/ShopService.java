import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class ShopService {
    private ProductRepo productRepo = new ProductRepo();
    private OrderRepo orderRepo = new OrderMapRepo();

    public Order addOrder(List<String> productIds) {
        List<Product> products = new ArrayList<>();
        for (String productId : productIds) {
            Product productToOrder = productRepo
                    .getProductById(productId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Product mit der Id: " + productId + " konnte nicht bestellt werden!"
                    ));
            products.add(productToOrder);
        }

        Order newOrder = new Order(
                UUID.randomUUID().toString(),
                products,
                OrderStatus.PROCESSING,
                Instant.now()
        );

        return orderRepo.addOrder(newOrder);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepo.getOrders().stream()
                .filter(order -> order.orderStatus() == status)
                .toList();
    }

    public Order updateOrder(String id, OrderStatus newStatus) {
        return orderRepo.updateOrder(id, newStatus);
    }
    
}