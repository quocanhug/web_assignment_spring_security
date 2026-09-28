package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.dto.UserFormDTO;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;
    private final ProductService productService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listUsers(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model
    ) {
        Page<UserDTO> userPage = userService.findAll(
                keyword,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("activeNav", "admin-users");

        return "admin/user-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("userFormDTO")) {
            model.addAttribute("userFormDTO", new UserFormDTO());
        }
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "admin-users");
        return "admin/user-form";
    }

    @PostMapping("/new")
    public String createUser(
            @Valid @ModelAttribute("userFormDTO") UserFormDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }

        try {
            userService.createUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo người dùng thành công!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            return "admin/user-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        UserFormDTO dto = userService.findFormById(id);
        model.addAttribute("userFormDTO", dto);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "admin-users");
        return "admin/user-form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("userFormDTO") UserFormDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }

        try {
            userService.updateUser(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái kích hoạt tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thao tác thất bại: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
