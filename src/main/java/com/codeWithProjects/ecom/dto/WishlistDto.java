package com.codeWithProjects.ecom.dto;


import lombok.Data;

@Data
public class WishlistDto {

    private Long userId;
    private Long productId;
    private String productName;
    private Long id;
    private byte[] returnedImg;
    private Long price;
    private String productDescription;

}
