package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Roles
            Role adminRole = roleRepository.findByNameIgnoreCase("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            // 2. Admin User
            User admin = userRepository.findByUsernameIgnoreCase("admin")
                    .orElseGet(() -> {
                        User u = User.builder()
                                .username("admin")
                                .email("admin@gmail.com")
                                .password(passwordEncoder.encode("123456"))
                                .fullName("Quản Trị Viên")
                                .images("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80")
                                .role(adminRole)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(u);
                    });

            // 3. Normal User 1
            User user1 = userRepository.findByUsernameIgnoreCase("user01")
                    .map(u -> {
                        u.setFullName("Đinh Quốc Anh");
                        return userRepository.save(u);
                    })
                    .orElseGet(() -> {
                        User u = User.builder()
                                .username("user01")
                                .email("user01@gmail.com")
                                .password(passwordEncoder.encode("123456"))
                                .fullName("Đinh Quốc Anh")
                                .images("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80")
                                .role(userRole)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(u);
                    });

            // 4. Normal User 2
            User user2 = userRepository.findByUsernameIgnoreCase("user02")
                    .orElseGet(() -> {
                        User u = User.builder()
                                .username("user02")
                                .email("user02@gmail.com")
                                .password(passwordEncoder.encode("123456"))
                                .fullName("Trần Thị Hoa")
                                .images("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80")
                                .role(userRole)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(u);
                    });

            // 5. Sample Products
            if (productRepository.count() == 0) {
                productRepository.save(Product.builder()
                        .name("Laptop Dell XPS 15")
                        .price(35000000.0)
                        .description("Laptop Dell XPS 15 Core i7 13th Gen, 32GB RAM, 1TB SSD, RTX 4060.")
                        .image("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=600&q=80")
                        .user(user1)
                        .createdAt(LocalDateTime.now().minusDays(3))
                        .build());

                productRepository.save(Product.builder()
                        .name("iPhone 15 Pro Max 256GB")
                        .price(29900000.0)
                        .description("Điện thoại Apple iPhone 15 Pro Max Titan tự nhiên, nguyên seal chính hãng VN/A.")
                        .image("https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=600&q=80")
                        .user(user1)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build());

                productRepository.save(Product.builder()
                        .name("Bàn phím cơ Keychron Q1 Pro")
                        .price(4200000.0)
                        .description("Bàn phím cơ Custom không dây nhôm nguyên khối, switch Gateron Jupiter Red.")
                        .image("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80")
                        .user(user1)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build());

                productRepository.save(Product.builder()
                        .name("Tai nghe Sony WH-1000XM5")
                        .price(7500000.0)
                        .description("Tai nghe chống ồn đỉnh cao của Sony, pin 30 giờ, âm thanh Hi-Res LDAC.")
                        .image("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80")
                        .user(admin)
                        .createdAt(LocalDateTime.now().minusHours(12))
                        .build());

                productRepository.save(Product.builder()
                        .name("Màn hình LG UltraFine 4K 27 inch")
                        .price(12500000.0)
                        .description("Màn hình chuyên đồ họa IPS 4K, 99% DCI-P3, hỗ trợ cổng USB Type-C 90W.")
                        .image("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=600&q=80")
                        .user(admin)
                        .createdAt(LocalDateTime.now().minusHours(6))
                        .build());

                productRepository.save(Product.builder()
                        .name("Đồng hồ Apple Watch Ultra 2")
                        .price(18500000.0)
                        .description("Đồng hồ thông minh thể thao chuyên nghiệp vỏ titan cao cấp, pin 72 giờ.")
                        .image("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80")
                        .user(user2)
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .build());

                log.info("Initialized sample users and products successfully.");
            }
        };
    }
}
