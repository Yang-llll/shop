package com.braisedpork.service;

import com.braisedpork.dto.ProductDTO;
import com.braisedpork.dto.ProductPageResult;

public interface ProductService {

    ProductDTO create(ProductDTO productDTO);

    ProductDTO update(Long id, ProductDTO productDTO);

    void delete(Long id);

    ProductDTO getById(Long id);

    ProductPageResult list(String category, int page, int size);

    ProductPageResult search(String keyword, String category, int page, int size);
}
