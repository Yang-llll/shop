package com.braisedpork.controller;

import com.braisedpork.dto.ProductDTO;
import com.braisedpork.entity.Product;
import java.util.List;
import com.braisedpork.dto.ProductPageResult;
import com.braisedpork.dto.Result;
import com.braisedpork.entity.SiteConfig;
import com.braisedpork.repository.ProductRepository;
import com.braisedpork.repository.SiteConfigRepository;
import com.braisedpork.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final SiteConfigRepository siteConfigRepository;

    @GetMapping
    public ResponseEntity<Result<ProductPageResult>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageIndex = Math.max(0, page - 1);
        ProductPageResult result;
        if (keyword != null && !keyword.isEmpty()) {
            result = productService.search(keyword, category, pageIndex, size);
        } else {
            result = productService.list(category, pageIndex, size);
        }
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/top-sales")
    public ResponseEntity<Result<List<ProductDTO>>> getTopSales() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        SiteConfig lastUpdate = siteConfigRepository.findByConfigKey("top_sales_updated_at")
                .orElseGet(() -> { SiteConfig s = new SiteConfig(); s.setConfigKey("top_sales_updated_at"); s.setConfigValue(""); return s; });

        if (lastUpdate.getConfigValue() != null && !lastUpdate.getConfigValue().isEmpty()) {
            try {
                java.time.LocalDateTime lastTime = java.time.LocalDateTime.parse(lastUpdate.getConfigValue());
                if (now.minusDays(7).isBefore(lastTime)) {
                    List<Product> cached = productRepository.findByStatus(Product.Status.ON_SALE);
                    List<ProductDTO> top5 = cached.stream()
                            .sorted((a, b) -> Long.compare(b.getSalesCount() != null ? b.getSalesCount() : 0L,
                                                           a.getSalesCount() != null ? a.getSalesCount() : 0L))
                            .limit(5)
                            .map(p -> com.braisedpork.dto.ProductDTO.builder()
                                    .id(p.getId()).name(p.getName()).description(p.getDescription())
                                    .price(p.getPrice()).originalPrice(p.getOriginalPrice())
                                    .stock(p.getStock()).category(p.getCategory())
                                    .imageUrl(p.getImageUrl()).images(p.getImages())
                                    .status(p.getStatus().name()).salesCount(p.getSalesCount())
                                    .build())
                            .collect(java.util.stream.Collectors.toList());
                    return ResponseEntity.ok(Result.success(top5));
                }
            } catch (java.time.format.DateTimeParseException e) {
                log.warn("TOP5更新时间配置格式无效，将重新计算排行榜：{}", lastUpdate.getConfigValue());
            }
        }

        List<Product> allOnSale = productRepository.findByStatus(Product.Status.ON_SALE);
        List<ProductDTO> top5 = allOnSale.stream()
                .sorted((a, b) -> Long.compare(b.getSalesCount() != null ? b.getSalesCount() : 0L,
                                               a.getSalesCount() != null ? a.getSalesCount() : 0L))
                .limit(5)
                .map(p -> com.braisedpork.dto.ProductDTO.builder()
                        .id(p.getId()).name(p.getName()).description(p.getDescription())
                        .price(p.getPrice()).originalPrice(p.getOriginalPrice())
                        .stock(p.getStock()).category(p.getCategory())
                        .imageUrl(p.getImageUrl()).images(p.getImages())
                        .status(p.getStatus().name()).salesCount(p.getSalesCount())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        lastUpdate.setConfigValue(now.toString());
        siteConfigRepository.save(lastUpdate);

        return ResponseEntity.ok(Result.success(top5));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<ProductDTO>> getById(@PathVariable Long id) {
        ProductDTO product = productService.getById(id);
        return ResponseEntity.ok(Result.success(product));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<ProductDTO>> create(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.create(productDTO);
        return ResponseEntity.ok(Result.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<ProductDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.update(id, productDTO);
        return ResponseEntity.ok(Result.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(Result.success(null));
    }
}
