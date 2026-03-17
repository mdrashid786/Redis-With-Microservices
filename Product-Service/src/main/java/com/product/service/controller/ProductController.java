package com.product.service.controller;

import com.product.service.beans.Product;
import com.product.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
class ProductController {

    @Autowired
    private ProductService service;

    /**
     * GET /api/products
     * Get all products (cached)
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    /**
     * GET /api/products/{id}
     * Get single product (cached)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = service.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
        }
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/products/{id}/stock
     * Get stock (cached, called by Order Service)
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStock(@PathVariable Long id) {
        Integer stock = service.getStock(id);
        if (stock == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
        }
        return ResponseEntity.ok(stock);
    }

    /**
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createProduct(product));
    }

    /**
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {
        return ResponseEntity.ok(service.updateProduct(id, product));
    }

    /**
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.ok("Deleted");
    }

    /**
     * POST /api/products/{id}/stock/{quantity}
     * Called by Order Service
     */
    @PostMapping("/{id}/stock/{quantity}")
    public ResponseEntity<?> updateStock(
            @PathVariable Long id,
            @PathVariable Integer quantity) {
        service.updateStock(id, quantity);
        return ResponseEntity.ok("Stock updated");
    }
}
