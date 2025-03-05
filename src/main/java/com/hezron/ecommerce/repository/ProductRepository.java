package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ----------------- USER-FACING METHODS -----------------

    /**
     * Find all active products
     */
    List<Product> findByActiveTrue();

    /**
     * Find products by category
     */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    /**
     * Simple name search methods
     */
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    /**
     * Price filtering methods
     */
    List<Product> findByPriceBetweenAndActiveTrue(Double minPrice, Double maxPrice);
    List<Product> findByPriceGreaterThanEqualAndActiveTrue(Double minPrice);
    List<Product> findByPriceLessThanEqualAndActiveTrue(Double maxPrice);

    /**
     * Combined search methods
     */
    List<Product> findByNameContainingIgnoreCaseAndPriceBetweenAndActiveTrue(String name, Double minPrice, Double maxPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceGreaterThanEqualAndActiveTrue(String name, Double minPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceLessThanEqualAndActiveTrue(String name, Double maxPrice);

    /**
     * Description search
     */
    List<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String keyword);

    /**
     * Find products with low stock
     */
    List<Product> findByStockQuantityLessThanAndActiveTrue(Integer threshold);
    List<Product> findByStockQuantityGreaterThanAndActiveTrue(Integer threshold);

    /**
     * Featured products queries
     */
    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc(); // Newest products
    List<Product> findTop5ByActiveTrueOrderByPriceDesc(); // Premium products
    List<Product> findTop5ByActiveTrueOrderByPriceAsc(); // Budget products

    /**
     * Advanced search for users - only active products
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
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
            @Param("minStock") Integer minStock
    );

    // ----------------- ADMIN-FACING METHODS -----------------

    /**
     * Find inactive products (admin only)
     */
    List<Product> findByActiveFalse();

    /**
     * Advanced search for admin dashboard - includes inactive products
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
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
            @Param("active") Boolean active
    );

    /**
     * Advanced search for admin dashboard with pagination
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:active IS NULL OR p.active = :active)")
    Page<Product> adminSearchPaginated(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("active") Boolean active,
            Pageable pageable
    );

    /**
     * Find product by ID with eagerly loaded category
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Product findByIdWithCategory(@Param("id") Long id);

    List<Product> findByCategory_Id(Long categoryId);
}