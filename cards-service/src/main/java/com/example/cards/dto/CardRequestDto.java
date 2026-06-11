package com.example.cards.dto;

import com.example.cards.entity.Card;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CardRequestDto {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Card type is required")
    private Card.CardType cardType;

    @NotNull(message = "Credit limit is required")
    @Positive(message = "Credit limit must be positive")
    private Double creditLimit;

    @NotNull(message = "Amount used is required")
    private Double amountUsed;

    @NotNull(message = "Person ID is required")
    private Long personId;
}
