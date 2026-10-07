import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ProductRepo productRepo = new ProductRepo();
        OrderRepo orderRepo = new OrderMapRepo();
        IdService idService = new IdService();
        ShopService shopService = new ShopService(productRepo, orderRepo, idService);

        try {
            processTransactions(Path.of("transactions.txt"), productRepo, orderRepo, shopService);
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen von transactions.txt: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Fehler beim Verarbeiten der Befehle: " + e.getMessage());
        }
    }

    static void processTransactions(
            Path transactionsPath,
            ProductRepo productRepo,
            OrderRepo orderRepo,
            ShopService shopService
    ) throws IOException {
        if (!Files.exists(transactionsPath)) {
            System.out.println("Hinweis: transactions.txt nicht gefunden. Keine Befehle ausgeführt.");
            return;
        }
        Map<String, String> aliasToOrderId = new HashMap<>();
        List<String> lines = Files.readAllLines(transactionsPath);

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0];

            switch (command) {
                case "addOrder" -> {
                    if (parts.length < 3) {
                        System.out.println("Ungültige addOrder-Zeile: " + line);
                        continue;
                    }

                    String alias = parts[1];
                    List<OrderItem> items = new java.util.ArrayList<>();

                    for (int i = 2; i < parts.length; i++) {
                        String token = parts[i];
                        String[] split = token.split(":");
                        if (split.length != 2) {
                            System.out.println("Ungültiges Produktformat (erwartet id:menge): " + token);
                            items.clear();
                            break;
                        }

                        String productId = split[0];
                        double quantity = Double.parseDouble(split[1]);
                        items.add(new OrderItem(productId, quantity));
                    }

                    if (items.isEmpty()) {
                        continue;
                    }

                    Order created = shopService.addOrderWithQuantities(items);
                    aliasToOrderId.put(alias, created.id());
                }
                case "setStatus" -> {
                    if (parts.length != 3) {
                        System.out.println("Ungültige setStatus-Zeile: " + line);
                        continue;
                    }

                    String alias = parts[1];
                    String orderId = aliasToOrderId.get(alias);

                    if (orderId == null) {
                        System.out.println("Unbekannter Alias: " + alias);
                        continue;
                    }

                    OrderStatus newStatus = OrderStatus.valueOf(parts[2]);
                    shopService.updateOrder(orderId, newStatus);
                }
                case "printOrders" -> {
                    System.out.println("=== ORDERS ===");
                    for (Order order : orderRepo.getOrders()) {
                        System.out.println(
                                "id=" + order.id()
                                        + ", status=" + order.orderStatus()
                                        + ", createdAt=" + order.createdAt()
                        );

                        for (OrderItem item : order.items()) {
                            String name = productRepo.getProductById(item.productId())
                                    .map(Product::name)
                                    .orElse("unbekannt");

                            System.out.println(
                                    "  - id=" + item.productId()
                                            + ", name=" + name
                                            + ", menge=" + item.quantity()
                            );
                        }
                    }
                }
                default -> System.out.println("Unbekannter Befehl: " + line);
            }
        }
    }
}