package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.ProductFormDTO;

public interface ProductService {

    Page<ProductDTO> findAll(String keyword, Pageable pageable);

    Page<ProductDTO> findByUserId(Long userId, String keyword, Pageable pageable);

    ProductDTO findById(Long id);

    ProductFormDTO findFormById(Long id);

    ProductDTO createProduct(ProductFormDTO dto, Long userId);

    ProductDTO updateProduct(Long id, ProductFormDTO dto, Long currentUserId, boolean isAdmin);

    void deleteProduct(Long id, Long currentUserId, boolean isAdmin);

    long countProducts();

    long countProductsByUserId(Long userId);
}
