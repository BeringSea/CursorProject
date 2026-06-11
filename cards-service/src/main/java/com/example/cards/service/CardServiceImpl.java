package com.example.cards.service;

import com.example.cards.dto.CardRequestDto;
import com.example.cards.dto.CardResponseDto;
import com.example.cards.entity.Card;
import com.example.cards.exception.CardAlreadyExistsException;
import com.example.cards.exception.CardNotFoundException;
import com.example.cards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements ICardService {

    private final CardRepository cardRepository;

    @Override
    public CardResponseDto createCard(CardRequestDto request) {
        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new CardAlreadyExistsException(
                    "Card with number '%s' already exists".formatted(request.getCardNumber()));
        }
        Card card = Card.builder()
                .cardNumber(request.getCardNumber())
                .cardType(request.getCardType())
                .creditLimit(request.getCreditLimit())
                .amountUsed(request.getAmountUsed())
                .availableAmount(request.getCreditLimit() - request.getAmountUsed())
                .personId(request.getPersonId())
                .build();
        return toDto(cardRepository.save(card));
    }

    @Override
    public CardResponseDto getCardById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public CardResponseDto getCardByNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .map(this::toDto)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card with number '%s' not found".formatted(cardNumber)));
    }

    @Override
    public List<CardResponseDto> getCardsByPersonId(Long personId) {
        return cardRepository.findByPersonId(personId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<CardResponseDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CardResponseDto updateCard(Long id, CardRequestDto request) {
        Card card = findById(id);
        card.setCardNumber(request.getCardNumber());
        card.setCardType(request.getCardType());
        card.setCreditLimit(request.getCreditLimit());
        card.setAmountUsed(request.getAmountUsed());
        card.setAvailableAmount(request.getCreditLimit() - request.getAmountUsed());
        card.setPersonId(request.getPersonId());
        return toDto(cardRepository.save(card));
    }

    @Override
    public void deleteCard(Long id) {
        findById(id);
        cardRepository.deleteById(id);
    }

    private Card findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card with id '%d' not found".formatted(id)));
    }

    private CardResponseDto toDto(Card card) {
        return CardResponseDto.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .cardType(card.getCardType())
                .creditLimit(card.getCreditLimit())
                .amountUsed(card.getAmountUsed())
                .availableAmount(card.getAvailableAmount())
                .personId(card.getPersonId())
                .build();
    }
}
