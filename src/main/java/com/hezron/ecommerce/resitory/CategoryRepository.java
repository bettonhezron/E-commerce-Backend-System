package com.hezron.ecommerce.resitory;


import com.hezron.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentId(Long parentId);
    List<Category> findByIdNull();
}
