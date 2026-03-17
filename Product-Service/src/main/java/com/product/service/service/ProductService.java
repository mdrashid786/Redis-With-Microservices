package com.product.service.service;

import com.product.service.beans.Product;
import com.product.service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Cacheable(value = "product_list", key = "'all'")
    public List<Product> getAllProducts() {
        System.out.println("🔍 DB Hit: getAllProducts");
        return repository.findAll();
    }

    @Cacheable(value = "product_by_id", key = "#id")
    public Product getProductById(Long id) {
        System.out.println("🔍 DB Hit: getProductById(" + id + ")");
        return repository.findById(id).orElse(null);
    }

    @Cacheable(value = "product_stock", key = "#id")
    public Integer getStock(Long id) {
        System.out.println("🔍 DB Hit: getStock(" + id + ")");
        Product product = repository.findById(id).orElse(null);
        return product != null ? product.getStock() : null;
    }

    @CacheEvict(value = {"product_list"}, allEntries = true)
    public Product createProduct(Product product) {
        System.out.println("✅ Creating product: " + product.getName());
        return repository.save(product);
    }

    @CacheEvict(value = {"product_list", "product_by_id"}, allEntries = true)
    public Product updateProduct(Long id, Product updated) {
        Product existing = repository.findById(id).orElseThrow();
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        System.out.println("✅ Updated product: " + id);
        return repository.save(existing);
    }

    @CacheEvict(value = {"product_list", "product_by_id"}, allEntries = true)
    public void deleteProduct(Long id) {
        repository.deleteById(id);
        System.out.println("✅ Deleted product: " + id);
    }

    /**
     * Called by Order Service to update stock
     * Clears cache immediately
     */
    @CacheEvict(value = "product_stock", key = "#id")
    public void updateStock(Long id, Integer newStock) {
        Product product = repository.findById(id).orElseThrow();
        product.setStock(newStock);
        repository.save(product);
        System.out.println("✅ Stock updated: product=" + id + ", newStock=" + newStock);
        // Cache cleared by @CacheEvict above
    }
}