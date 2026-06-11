package com.example.person.dto;

import lombok.Data;

@Data
public class CardDto {
    private Long id;
    private String cardNumber;
    private String cardType;
    private Double creditLimit;
    private Double amountUsed;
    private Double availableAmount;
    private Long personId;
}
