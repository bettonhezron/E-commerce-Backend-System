package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find active categories
    List<Category> findByActiveTrue();

    // Find by name (for searching)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find by name and active status
    Optional<Category> findByNameIgnoreCaseAndActiveTrue(String name);

    // Find root categories (categories without a parent)
    List<Category> findByParentIsNull();

    // Custom search with optional filters
    @Query("SELECT c FROM Category c " +
            "WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:active IS NULL OR c.active = :active)")
    Page<Category> searchCategories(
            @Param("name") String name,
            @Param("active") Boolean active,
            Pageable pageable
    );

    // Find all subcategories of a given parent category
    List<Category> findByParent_Id(Long parentId);


}