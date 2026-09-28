package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.ProductFormDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.ProductService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Product> products = productRepository.searchAll(kw, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findByUserId(Long userId, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Product> products = productRepository.searchByUserId(userId, kw, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFormDTO findFormById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + id));

        return ProductFormDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .image(product.getImage())
                .userId(product.getUser().getId())
                .build();
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductFormDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng sở hữu."));

        String imageUrl = "/images/product-default.png";
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploaded = cloudinaryService.uploadImage(dto.getImageFile());
            if (uploaded != null) {
                imageUrl = uploaded;
            }
        }

        Product product = Product.builder()
                .name(dto.getName().trim())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .image(imageUrl)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        product = productRepository.save(product);
        log.info("Product created with id: {} by user: {}", product.getId(), user.getUsername());
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductFormDTO dto, Long currentUserId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm cần cập nhật."));

        // Check ownership or admin
        if (!isAdmin && !product.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa sản phẩm này.");
        }

        product.setName(dto.getName().trim());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploaded = cloudinaryService.uploadImage(dto.getImageFile());
            if (uploaded != null) {
                product.setImage(uploaded);
            }
        }

        product = productRepository.save(product);
        log.info("Product updated with id: {}", product.getId());
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long currentUserId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm cần xóa."));

        if (!isAdmin && !product.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa sản phẩm này.");
        }

        productRepository.delete(product);
        log.info("Product deleted with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsByUserId(Long userId) {
        return productRepository.countByUserId(userId);
    }
}
