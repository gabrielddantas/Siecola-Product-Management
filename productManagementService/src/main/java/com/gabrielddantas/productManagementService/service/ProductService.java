package com.gabrielddantas.productManagementService.service;

import com.gabrielddantas.productManagementService.entity.Product;
import com.gabrielddantas.productManagementService.model.enums.EventType;
import com.gabrielddantas.productManagementService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductPublisherService productPublisherService;

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Optional<Product> getProductById(Long id) {
    return productRepository.findById(id);
  }

  public Optional<Product> getByCode(String code) {
    return productRepository.findByCode(code);
  }

  public Product createProduct(Product product) {
    if (getByCode(product.getCode()).isPresent()) {
      throw new NoSuchElementException(
          "Product with code " + product.getCode() + " already exists");
    }

    Product savedProduct = productRepository.save(product);
    productPublisherService.publishProductEvent(savedProduct, EventType.PRODUCT_CREATED, "Gabriel");

    return savedProduct;
  }

  public Product updateProduct(Product product) {
    if (getProductById(product.getId()).isEmpty()) {
      throw new NoSuchElementException("Product with id " + product.getId() + " does not exist");
    }
    Product updatedProduct = productRepository.save(product);
    productPublisherService.publishProductEvent(
        updatedProduct, EventType.PRODUCT_UPDATED, "Gabriel");
    return updatedProduct;
  }

  public void deleteProduct(Long id) {
    getProductById(id)
        .ifPresentOrElse(
            it -> {
              productRepository.delete(it);
              productPublisherService.publishProductEvent(it, EventType.PRODUCT_DELETED, "Gabriel");
            },
            () -> {
              throw new NoSuchElementException("Product with id " + id + " does not exist");
            });
  }
}
