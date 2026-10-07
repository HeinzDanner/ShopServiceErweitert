import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepoTest {

    @Test
    void getProducts() {
        // GIVEN
        ProductRepo repo = new ProductRepo();

        // WHEN
        List<Product> actual = repo.getProducts();

        // THEN
        List<Product> expected = List.of(
                new Product("1", "Apfel", 10.0),
                new Product("2", "Birne", 8.0),
                new Product("3", "Banane", 12.5)
        );

        assertEquals(expected, actual);
    }

    @Test
    void getProductById_whenExists_returnsPresentOptional() {
        // GIVEN
        ProductRepo repo = new ProductRepo();

        // WHEN
        Optional<Product> actual = repo.getProductById("1");

        // THEN
        Optional<Product> expected =
                Optional.of(new Product("1", "Apfel", 10.0));

        assertEquals(expected, actual);
    }

    @Test
    void getProductById_whenUnknown_returnsEmptyOptional() {
        // GIVEN
        ProductRepo repo = new ProductRepo();

        // WHEN
        Optional<Product> actual = repo.getProductById("unknown");

        // THEN
        assertTrue(actual.isEmpty());
    }

    @Test
    void addProduct() {
        // GIVEN
        ProductRepo repo = new ProductRepo();
        Product newProduct = new Product("4", "Orange", 5.0);

        // WHEN
        Product actual = repo.addProduct(newProduct);

        // THEN
        Product expected = new Product("4", "Orange", 5.0);

        assertEquals(expected, actual);
        assertEquals(Optional.of(expected), repo.getProductById("4"));
    }

    @Test
    void removeProduct() {
        // GIVEN
        ProductRepo repo = new ProductRepo();

        // WHEN
        repo.removeProduct("1");

        // THEN
        assertTrue(repo.getProductById("1").isEmpty());
    }
}