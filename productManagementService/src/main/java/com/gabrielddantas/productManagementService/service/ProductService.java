package com.gabrielddantas.productManagementService.service;

import com.gabrielddantas.productManagementService.entity.Product;
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

    return productRepository.save(product);
  }

  public Product updateProduct(Product product) {
    if (getProductById(product.getId()).isEmpty()) {
      throw new NoSuchElementException("Product with id " + product.getId() + " does not exist");
    }
    return productRepository.save(product);
  }

  public void deleteProduct(Long id) {
    if (getProductById(id).isEmpty()) {
      throw new NoSuchElementException("Product with id " + id + " does not exist");
    }
    productRepository.deleteById(id);
  }
}
