package com.example.person;

import com.example.person.client.CardsClient;
import com.example.person.dto.CardDto;
import com.example.person.dto.PersonRequestDto;
import com.example.person.dto.PersonResponseDto;
import com.example.person.entity.Person;
import com.example.person.service.IPersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IPersonService personService;

    @MockBean
    private CardsClient cardsClient;

    private PersonRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new PersonRequestDto();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setMobileNumber("+1234567890");
        validRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
        validRequest.setGender(Person.Gender.MALE);
        validRequest.setAddress("123 Main St, Springfield");

        when(cardsClient.getCardsByPersonId(anyLong())).thenReturn(List.of());
    }

    @Test
    void createPerson_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.mobileNumber", is("+1234567890")));
    }

    @Test
    void getPersonById_shouldReturnPerson() throws Exception {
        PersonResponseDto created = personService.createPerson(validRequest);

        mockMvc.perform(get("/api/persons/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.cards", is(empty())));
    }

    @Test
    void getPersonById_withCards_shouldIncludeCards() throws Exception {
        PersonResponseDto created = personService.createPerson(validRequest);

        CardDto card = new CardDto();
        card.setId(1L);
        card.setCardNumber("4111-1111-1111-1111");
        card.setCardType("CREDIT");
        card.setCreditLimit(5000.0);
        card.setAmountUsed(1000.0);
        card.setAvailableAmount(4000.0);
        card.setPersonId(created.getId());

        when(cardsClient.getCardsByPersonId(created.getId())).thenReturn(List.of(card));

        mockMvc.perform(get("/api/persons/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].cardNumber", is("4111-1111-1111-1111")));
    }

    @Test
    void getPersonByEmail_shouldReturnPerson() throws Exception {
        personService.createPerson(validRequest);

        mockMvc.perform(get("/api/persons/email/{email}", "john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void getAllPersons_shouldReturnList() throws Exception {
        personService.createPerson(validRequest);

        PersonRequestDto second = new PersonRequestDto();
        second.setFirstName("Jane");
        second.setLastName("Smith");
        second.setEmail("jane.smith@example.com");
        second.setMobileNumber("+0987654321");
        second.setDateOfBirth(LocalDate.of(1995, 3, 20));
        second.setGender(Person.Gender.FEMALE);
        second.setAddress("456 Oak Ave, Portland");
        personService.createPerson(second);

        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void updatePerson_shouldReturnUpdatedPerson() throws Exception {
        PersonResponseDto created = personService.createPerson(validRequest);
        validRequest.setFirstName("Jonathan");
        validRequest.setAddress("999 New St, Chicago");

        mockMvc.perform(put("/api/persons/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jonathan")))
                .andExpect(jsonPath("$.address", is("999 New St, Chicago")));
    }

    @Test
    void deletePerson_shouldReturnNoContent() throws Exception {
        PersonResponseDto created = personService.createPerson(validRequest);

        mockMvc.perform(delete("/api/persons/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/persons/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPerson_withMissingFields_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPerson_withDuplicateEmail_shouldReturnConflict() throws Exception {
        personService.createPerson(validRequest);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode", is(409)));
    }

    @Test
    void getPersonById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/persons/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(404)))
                .andExpect(jsonPath("$.errorMessage", containsString("999")));
    }
}
