package vn.iotstar.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private Double price;
    private String description;
    private String image;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private LocalDateTime createdAt;
}
