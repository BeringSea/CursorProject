package com.example.cards.dto;

import com.example.cards.entity.Card;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardResponseDto {

    private Long id;
    private String cardNumber;
    private Card.CardType cardType;
    private Double creditLimit;
    private Double amountUsed;
    private Double availableAmount;
    private Long personId;
}
