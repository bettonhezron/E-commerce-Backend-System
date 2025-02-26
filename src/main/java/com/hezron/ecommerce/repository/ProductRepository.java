package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Arrays;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    //Basic filtering methods
    List<Product>  findByActiveTrue();
    List<Product> findByActiveFalse();
    List<Product> findByCategoryIdAndActiveTrue(Long product);

    //Simple search methods
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Product> findByPriceBetweenAndActiveTrue(Double minPrice, Double maxPrice);
    List<Product> findByPriceGreaterThanEqualAndActiveTrue(Double minPrice);
    List<Product> findByPriceLessThanEqualAndActiveTrue(Double maxPrice);

    //Combined Search methods
    List<Product> findByNameContainingIgnoreCaseAndPriceBetweenAndActiveTrue(String name, Double minPrice, Double maxPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceGreaterThanEqualAndActiveTrue(String name, Double minPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceLessThanEqualAndActiveTrue(String name, Double maxPrice);

    //Stock Filtering
    List<Product> findByStockQuantityLessThanAndActiveTrue(Integer threshold);
    List<Product> findByStockQuantityGreaterThanAndActiveTrue(Integer threshold);


    //Description Search
    List<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String keyword);

    // Advanced search with JPQL
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:minStock IS NULL OR p.stockQuantity >= :minStock) AND " +
            "p.active = true")
    List<Product> advancedSearch(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minStock") Integer minStock);

    // Admin specific search including inactive products
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:active IS NULL OR p.active = :active)")
    List<Product> adminSearch(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("active") Boolean active);

    //Newest products (for featured section or "new arrivals")
    List<Product> findTop10ByActiveTrueOrderByIdDesc();

    //Most/least expensive products
    List<Product> findTop5ByActiveTrueOrderByPriceDesc();
    List<Product>  findTop5ByActiveTrueOrderByPriceAsc();



}
