package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findCategoryId(Long categoryId);
    List<Product>  findByActiveTrue();

    @Query("SELECT p FROM Product WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> search(String keyword);
}
