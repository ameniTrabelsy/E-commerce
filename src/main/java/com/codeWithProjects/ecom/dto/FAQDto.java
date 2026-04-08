package com.codeWithProjects.ecom.dto;

import com.codeWithProjects.ecom.entity.Product;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
public class FAQDto {

    private Long id;

    private String question;

    private String answer;


    private Long productId;

}
