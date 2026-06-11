package com.example.cards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Column(nullable = false)
    private Double creditLimit;

    @Column(nullable = false)
    private Double amountUsed;

    @Column(nullable = false)
    private Double availableAmount;

    @Column(nullable = false)
    private Long personId;

    public enum CardType {
        CREDIT, DEBIT
    }
}
