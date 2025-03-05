package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PagedResponseDTO;
import com.hezron.ecommerce.dto.ProductDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Category;
import com.hezron.ecommerce.model.Product;
import com.hezron.ecommerce.repository.CategoryRepository;
import com.hezron.ecommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Product operations
 * Contains both user-facing and admin functionality
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ================= USER-FACING METHODS =================

    /**
     * Get all active products
     */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findByIdWithCategory(id);
        if (product == null || !product.getActive()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return convertToDTO(product);
    }

    /**
     * Advanced search for products - User facing (only active products)
     */
    public List<ProductDTO> advancedSearch(String name, Long categoryId, Double minPrice, Double maxPrice, Integer minStock) {
        List<Product> products = productRepository.advancedSearch(
                name, categoryId, minPrice, maxPrice, minStock);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by name search
     */
    public List<ProductDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get newest products (for "New arrivals" section)
     */
    public List<ProductDTO> getNewestProducts() {
        List<Product> products = productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get most expensive products (for "Premium Collections" section)
     */
    public List<ProductDTO> getPremiumProducts() {
        List<Product> products = productRepository.findTop5ByActiveTrueOrderByPriceDesc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get Budget-friendly products (for "Budget Deals" section)
     */
    public List<ProductDTO> getBudgetProducts() {
        List<Product> products = productRepository.findTop5ByActiveTrueOrderByPriceAsc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Method to get all products by category
    public  List<ProductDTO> getProductsByCategory(Long categoryId){
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Product> products = productRepository.findByCategory_Id(categoryId);

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // ================= ADMIN-FACING METHODS =================

    /**
     * Create new product (admin only)
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    /**
     * Update existing product (admin only)
     */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductFromDTO(product, productDTO);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Soft delete product (admin only)
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    /**
     * Advanced search for admin dashboard - includes inactive products
     */
    public List<ProductDTO> adminSearch(String name, Long categoryId, Double minPrice, Double maxPrice, Boolean active) {
        List<Product> products = productRepository.adminSearch(
                name, categoryId, minPrice, maxPrice, active);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Advanced search with pagination for admin dashboard
     */
    public PagedResponseDTO<ProductDTO> adminSearchPaginated(
            String name, Long categoryId, Double minPrice, Double maxPrice, Boolean active,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.adminSearchPaginated(
                name, categoryId, minPrice, maxPrice, active, pageable);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponseDTO<>(
                productDTOs,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    /**
     * Get products with low stock (for inventory alerts)
     */
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findByStockQuantityLessThanAndActiveTrue(threshold);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Bulk update product status (admin only)
     */
    @Transactional
    public void bulkUpdateProductStatus(List<Long> ids, boolean active) {
        ids.forEach(id -> {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
            product.setActive(active);
            productRepository.save(product);
        });
    }

    /**
     * Bulk update product stock (admin only)
     */
    @Transactional
    public void bulkUpdateStock(@Valid List<ProductDTO> productDTOs) {
        productDTOs.forEach(dto -> {
            Product product = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + dto.getId() + " not found"));
            product.setStockQuantity(dto.getStockQuantity());
            productRepository.save(product);
        });
    }

    // ================= HELPER METHODS =================

    /**
     * Update product fields from DTO
     */
    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrls(dto.getImageUrls());
        product.setActive(dto.isActive());

        // Handle category update
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
    }

    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        dto.setImageUrls(product.getImageUrls());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}