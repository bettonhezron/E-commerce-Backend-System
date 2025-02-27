package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.ProductDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Product;
import com.hezron.ecommerce.repository.CategoryRepository;
import com.hezron.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get product By ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return convertToDTO(product);
    }

//create product
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    //Update product
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductFromDTO(product, productDTO);
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }
//Delete Product
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    //Advanced search for product - User facing
    public List<ProductDTO> advancedSearch(String name, Long categoryId, Double minPrice, Double maxPrice, Integer minStock){
            List<Product> products = productRepository.advancedSearch(
                    name, categoryId, minPrice, maxPrice, minStock);
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
    }

    //Advanced search for admin dashboard - include inactive products
    public List<ProductDTO> adminSearch(String name, Long categoryId, Double minPrice, Double maxPrice, Boolean active){
        List<Product> products = productRepository.adminSearch(
                name, categoryId, minPrice, maxPrice, active);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Get products with low stock(for inventory alerts)
    public List<ProductDTO> getLowStocksProducts(Integer threshold){
        List<Product> products = productRepository.findByStockQuantityLessThanAndActiveTrue(threshold);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Get newest products (for "New arrivals" section")
    public List<ProductDTO> getNewestProducts(){
        List<Product> products = productRepository.findTop10ByActiveTrueOrderByIdDesc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Get most expensive products (for "Premium Collections" section)
    public List<ProductDTO> getPremiumProducts(){
        List<Product> products = productRepository.findTop10ByActiveTrueOrderByIdDesc();
        return products
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

    }

    //Get Budget-friendly products (for "Budget Deals" section)
    public List<ProductDTO> getBudgetProducts(){
        List<Product> products = productRepository.findTop5ByActiveTrueOrderByPriceAsc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrls(dto.getImageUrls());
        product.setActive(Boolean.TRUE.equals(dto.isActive()));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setImageUrls(product.getImageUrls());
        dto.setActive(product.getActive());
        return dto;
    }
}
