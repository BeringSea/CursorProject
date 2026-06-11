package com.example.cards;

import com.example.cards.dto.CardRequestDto;
import com.example.cards.dto.CardResponseDto;
import com.example.cards.entity.Card;
import com.example.cards.service.ICardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICardService cardService;

    private CardRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CardRequestDto();
        validRequest.setCardNumber("4111-1111-1111-1111");
        validRequest.setCardType(Card.CardType.CREDIT);
        validRequest.setCreditLimit(5000.0);
        validRequest.setAmountUsed(1000.0);
        validRequest.setPersonId(1L);
    }

    @Test
    void createCard_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardNumber", is("4111-1111-1111-1111")))
                .andExpect(jsonPath("$.cardType", is("CREDIT")))
                .andExpect(jsonPath("$.creditLimit", is(5000.0)))
                .andExpect(jsonPath("$.amountUsed", is(1000.0)))
                .andExpect(jsonPath("$.availableAmount", is(4000.0)))
                .andExpect(jsonPath("$.personId", is(1)));
    }

    @Test
    void getCardById_shouldReturnCard() throws Exception {
        CardResponseDto created = cardService.createCard(validRequest);

        mockMvc.perform(get("/api/cards/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.cardNumber", is("4111-1111-1111-1111")));
    }

    @Test
    void getCardByNumber_shouldReturnCard() throws Exception {
        cardService.createCard(validRequest);

        mockMvc.perform(get("/api/cards/number/{cardNumber}", "4111-1111-1111-1111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber", is("4111-1111-1111-1111")));
    }

    @Test
    void getCardsByPersonId_shouldReturnList() throws Exception {
        cardService.createCard(validRequest);

        mockMvc.perform(get("/api/cards/person/{personId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].personId", is(1)));
    }

    @Test
    void getAllCards_shouldReturnAllCards() throws Exception {
        cardService.createCard(validRequest);

        CardRequestDto second = new CardRequestDto();
        second.setCardNumber("5500-0000-0000-0004");
        second.setCardType(Card.CardType.DEBIT);
        second.setCreditLimit(2000.0);
        second.setAmountUsed(500.0);
        second.setPersonId(2L);
        cardService.createCard(second);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void updateCard_shouldReturnUpdatedCard() throws Exception {
        CardResponseDto created = cardService.createCard(validRequest);
        validRequest.setAmountUsed(2000.0);

        mockMvc.perform(put("/api/cards/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountUsed", is(2000.0)))
                .andExpect(jsonPath("$.availableAmount", is(3000.0)));
    }

    @Test
    void deleteCard_shouldReturnNoContent() throws Exception {
        CardResponseDto created = cardService.createCard(validRequest);

        mockMvc.perform(delete("/api/cards/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_withMissingFields_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(404)))
                .andExpect(jsonPath("$.errorMessage", containsString("999")));
    }
}
