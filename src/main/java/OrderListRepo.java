import java.util.ArrayList;
import java.util.List;

public class OrderListRepo implements OrderRepo {
    private List<Order> orders = new ArrayList<>();

    public List<Order> getOrders() {
        return orders;
    }

    public Order getOrderById(String id) {
        for (Order order : orders) {
            if (order.id().equals(id)) {
                return order;
            }
        }
        return null;
    }

    public Order addOrder(Order newOrder) {
        orders.add(newOrder);
        return newOrder;
    }

    public void removeOrder(String id) {
        for (Order order : orders) {
            if (order.id().equals(id)) {
                orders.remove(order);
                return;
            }
        }
    }

    @Override
    public Order updateOrder(String id, OrderStatus newStatus) {
        for (int i = 0; i < orders.size(); i++) {
            Order existing = orders.get(i);
            if (existing.id().equals(id)) {
                Order updated = existing.withOrderStatus(newStatus);
                orders.set(i, updated);
                return updated;
            }
        }
        return null;
    }

}
