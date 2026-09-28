package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.ProductFormDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "filter", required = false, defaultValue = "all") String filter,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Page<ProductDTO> productPage;

        boolean filterMy = "my".equalsIgnoreCase(filter) && currentUser != null;

        if (filterMy) {
            productPage = productService.findByUserId(
                    currentUser.getId(),
                    keyword,
                    PageRequest.of(page, size, Sort.by("id").descending())
            );
        } else {
            productPage = productService.findAll(
                    keyword,
                    PageRequest.of(page, size, Sort.by("id").descending())
            );
        }

        long totalProducts = productService.countProducts();
        long myProductsCount = (currentUser != null) ? productService.countProductsByUserId(currentUser.getId()) : 0;

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("myProductsCount", myProductsCount);
        model.addAttribute("activeNav", "products");

        return "product/product-list";
    }

    @GetMapping("/{id}")
    public String productDetail(
            @PathVariable("id") Long id,
            Model model
    ) {
        ProductDTO product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("activeNav", "products");
        return "product/product-detail";
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("productFormDTO")) {
            model.addAttribute("productFormDTO", new ProductFormDTO());
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "new-product");
        return "product/product-form";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createProduct(
            @Valid @ModelAttribute("productFormDTO") ProductFormDTO dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "product/product-form";
        }

        try {
            productService.createProduct(dto, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            log.error("Error creating product", e);
            model.addAttribute("errorMessage", "Không thể thêm sản phẩm: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "product/product-form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        ProductFormDTO dto = productService.findFormById(id);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !dto.getUserId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("productFormDTO", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "products");
        return "product/product-form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("productFormDTO") ProductFormDTO dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "product/product-form";
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            productService.updateProduct(id, dto, currentUser.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin sản phẩm thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            log.error("Error updating product", e);
            model.addAttribute("errorMessage", "Không thể cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "product/product-form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProduct(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            productService.deleteProduct(id, currentUser.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/products";
    }
}
