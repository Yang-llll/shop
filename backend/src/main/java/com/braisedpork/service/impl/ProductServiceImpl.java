package com.braisedpork.service.impl;

import com.braisedpork.dto.ProductDTO;
import com.braisedpork.dto.ProductPageResult;
import com.braisedpork.entity.Product;
import com.braisedpork.exception.BusinessException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.repository.ProductRepository;
import com.braisedpork.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDTO create(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .originalPrice(productDTO.getOriginalPrice())
                .stock(productDTO.getStock())
                .category(productDTO.getCategory())
                .imageUrl(productDTO.getImageUrl())
                .images(productDTO.getImages())
                .status(parseStatus(productDTO.getStatus()))
                .salesCount(0L)
                .build();

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    @Override
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));

        if (productDTO.getName() != null) product.setName(productDTO.getName());
        if (productDTO.getDescription() != null) product.setDescription(productDTO.getDescription());
        if (productDTO.getPrice() != null) product.setPrice(productDTO.getPrice());
        if (productDTO.getOriginalPrice() != null) product.setOriginalPrice(productDTO.getOriginalPrice());
        if (productDTO.getStock() != null) product.setStock(productDTO.getStock());
        if (productDTO.getCategory() != null) product.setCategory(productDTO.getCategory());
        if (productDTO.getImageUrl() != null) product.setImageUrl(productDTO.getImageUrl());
        if (productDTO.getImages() != null) product.setImages(productDTO.getImages());
        if (productDTO.getStatus() != null) product.setStatus(parseStatus(productDTO.getStatus()));

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("商品不存在");
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
        return convertToDTO(product);
    }

    @Override
    public ProductPageResult list(String category, int page, int size) {
        validatePage(page, size);
        List<Product> allProducts;
        if (category != null && !category.isEmpty()) {
            allProducts = productRepository.findByStatus(Product.Status.ON_SALE);
            allProducts = allProducts.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().contains(category))
                    .collect(Collectors.toList());
        } else {
            allProducts = productRepository.findByStatus(Product.Status.ON_SALE);
        }

        int total = allProducts.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<ProductDTO> products = allProducts.subList(from, to).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ProductPageResult.builder()
                .products(products)
                .totalElements((long) total)
                .totalPages((total + size - 1) / size)
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @Override
    public ProductPageResult search(String keyword, String category, int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.searchProducts(keyword, category, null, pageable);

        List<ProductDTO> products = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ProductPageResult.builder()
                .products(products)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    private Product.Status parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Product.Status.ON_SALE;
        }
        try {
            return Product.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "无效的商品状态：" + status);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分页参数不合法");
        }
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .images(product.getImages())
                .status(product.getStatus().name())
                .salesCount(product.getSalesCount())
                .build();
    }
}
