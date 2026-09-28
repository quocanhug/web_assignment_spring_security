package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        long totalUsers = userService.countUsers();
        long totalProducts = productService.countProducts();
        long myProductsCount = (currentUser != null) ? productService.countProductsByUserId(currentUser.getId()) : 0;

        // Fetch top 6 latest products for the home page
        var latestProducts = productService.findAll("", PageRequest.of(0, 6, Sort.by("createdAt").descending()));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("myProductsCount", myProductsCount);
        model.addAttribute("latestProducts", latestProducts.getContent());
        model.addAttribute("activeNav", "home");

        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
