package com.example.cards.service;

import com.example.cards.dto.CardRequestDto;
import com.example.cards.dto.CardResponseDto;

import java.util.List;

public interface ICardService {
    CardResponseDto createCard(CardRequestDto request);
    CardResponseDto getCardById(Long id);
    CardResponseDto getCardByNumber(String cardNumber);
    List<CardResponseDto> getCardsByPersonId(Long personId);
    List<CardResponseDto> getAllCards();
    CardResponseDto updateCard(Long id, CardRequestDto request);
    void deleteCard(Long id);
}
