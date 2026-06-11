package com.example.person.client;

import com.example.person.dto.CardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

// Graceful degradation is handled in PersonServiceImpl.fetchCards() via try-catch.
@FeignClient(name = "cards-service", url = "${cards-service.url}")
public interface CardsClient {

    @GetMapping("/api/cards/person/{personId}")
    List<CardDto> getCardsByPersonId(@PathVariable Long personId);
}
