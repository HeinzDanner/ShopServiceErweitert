import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepo {
    private final List<Product> products;

    public ProductRepo() {
        products = new ArrayList<>();
        products.add(new Product("1", "Apfel", 10.0));
        products.add(new Product("2", "Birne", 8.0));
        products.add(new Product("3", "Banane", 12.5));
    }

    public List<Product> getProducts() {
        return products;
    }

    public Optional<Product> getProductById(String id) {
        return products.stream()
                .filter(product -> product.id().equals(id))
                .findFirst();
    }

    public Product addProduct(Product product) {
        products.add(product);
        return product;
    }

    public void removeProduct(String id) {
        products.removeIf(product -> product.id().equals(id));
    }

    public void updateProduct(Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).id().equals(updated.id())) {
                products.set(i, updated);
                return;
            }
        }
    }
}