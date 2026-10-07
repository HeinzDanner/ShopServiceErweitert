import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public class ShopService {
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final IdService idService;

    public Order addOrder(List<String> productIds) {
        List<OrderItem> items = productIds.stream()
                .map(id -> new OrderItem(id, 1.0))
                .toList();
        return addOrderWithQuantities(items);
    }

    public Order addOrderWithQuantities(List<OrderItem> items) {
        Map<String, BigDecimal> requestedByProduct = new LinkedHashMap<>();
        for (OrderItem item : items) {
            if (!Double.isFinite(item.quantity()) || item.quantity() <= 0) {
                throw new IllegalArgumentException("Ungültige Menge für Produkt " + item.productId());
            }

            BigDecimal totalRequested = requestedByProduct.getOrDefault(item.productId(), BigDecimal.ZERO)
                    .add(BigDecimal.valueOf(item.quantity()));
            if (!Double.isFinite(totalRequested.doubleValue())) {
                throw new IllegalArgumentException("Ungültige Gesamtmenge für Produkt " + item.productId());
            }
            requestedByProduct.put(item.productId(), totalRequested);
        }

        Map<String, Product> productsById = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> request : requestedByProduct.entrySet()) {
            Product product = productRepo.getProductById(request.getKey())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Product mit der Id: " + request.getKey() + " konnte nicht bestellt werden!"
                    ));

            if (!Double.isFinite(product.quantity()) || product.quantity() < 0) {
                throw new IllegalStateException("Ungültiger Bestand für Produkt " + product.id());
            }
            if (BigDecimal.valueOf(product.quantity()).compareTo(request.getValue()) < 0) {
                throw new IllegalStateException(
                        "Nicht genug Bestand für Produkt " + product.id()
                                + " (angefragt: " + request.getValue()
                                + ", verfügbar: " + product.quantity() + ")"
                );
            }
            productsById.put(product.id(), product);
        }

        for (Map.Entry<String, BigDecimal> request : requestedByProduct.entrySet()) {
            Product product = productsById.get(request.getKey());
            Product updated = new Product(
                    product.id(),
                    product.name(),
                    BigDecimal.valueOf(product.quantity()).subtract(request.getValue()).doubleValue()
            );
            productRepo.updateProduct(updated);
        }

        Order newOrder = new Order(
                idService.generateId(),
                new ArrayList<>(items),
                OrderStatus.PROCESSING,
                Instant.now()
        );

        orderRepo.addOrder(newOrder);
        return newOrder;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepo.getOrders().stream()
                .filter(order -> order.orderStatus() == status)
                .toList();
    }

    public Order updateOrder(String id, OrderStatus newStatus) {
        return orderRepo.updateOrder(id, newStatus);
    }

    public Map<OrderStatus, Order> getOldestOrderPerStatus() {
        Map<OrderStatus, Order> oldestByStatus = new HashMap<>();

        for (Order order : orderRepo.getOrders()) {
            OrderStatus status = order.orderStatus();

            if (!oldestByStatus.containsKey(status)) {
                oldestByStatus.put(status, order);
            } else {
                Order currentOldest = oldestByStatus.get(status);
                if (order.createdAt().isBefore(currentOldest.createdAt())) {
                    oldestByStatus.put(status, order);
                }
            }
        }

        return oldestByStatus;
    }

}