import java.time.Instant;
import java.util.List;

public record Order(
        String id,
        List<Product> products,
        OrderStatus orderStatus,
        Instant createdAt) {
}
