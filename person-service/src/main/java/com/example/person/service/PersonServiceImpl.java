package com.example.person.service;

import com.example.person.client.CardsClient;
import com.example.person.dto.CardDto;
import com.example.person.dto.PersonRequestDto;
import com.example.person.dto.PersonResponseDto;
import com.example.person.entity.Person;
import com.example.person.exception.PersonAlreadyExistsException;
import com.example.person.exception.PersonNotFoundException;
import com.example.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements IPersonService {

    private final PersonRepository personRepository;
    private final CardsClient cardsClient;

    @Override
    public PersonResponseDto createPerson(PersonRequestDto request) {
        if (personRepository.existsByEmail(request.getEmail())) {
            throw new PersonAlreadyExistsException(
                    "Person with email '%s' already exists".formatted(request.getEmail()));
        }
        if (personRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new PersonAlreadyExistsException(
                    "Person with mobile number '%s' already exists".formatted(request.getMobileNumber()));
        }
        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();
        return toDto(personRepository.save(person), Collections.emptyList());
    }

    @Override
    public PersonResponseDto getPersonById(Long id) {
        Person person = findById(id);
        List<CardDto> cards = fetchCards(id);
        return toDto(person, cards);
    }

    @Override
    public PersonResponseDto getPersonByEmail(String email) {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new PersonNotFoundException(
                        "Person with email '%s' not found".formatted(email)));
        List<CardDto> cards = fetchCards(person.getId());
        return toDto(person, cards);
    }

    @Override
    public List<PersonResponseDto> getAllPersons() {
        return personRepository.findAll().stream()
                .map(p -> toDto(p, fetchCards(p.getId())))
                .toList();
    }

    @Override
    public PersonResponseDto updatePerson(Long id, PersonRequestDto request) {
        Person person = findById(id);
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setMobileNumber(request.getMobileNumber());
        person.setDateOfBirth(request.getDateOfBirth());
        person.setGender(request.getGender());
        person.setAddress(request.getAddress());
        List<CardDto> cards = fetchCards(id);
        return toDto(personRepository.save(person), cards);
    }

    @Override
    public void deletePerson(Long id) {
        findById(id);
        personRepository.deleteById(id);
    }

    private Person findById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(
                        "Person with id '%d' not found".formatted(id)));
    }

    private List<CardDto> fetchCards(Long personId) {
        try {
            return cardsClient.getCardsByPersonId(personId);
        } catch (Exception ex) {
            log.warn("Could not fetch cards for person {}: {}", personId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private PersonResponseDto toDto(Person person, List<CardDto> cards) {
        return PersonResponseDto.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .mobileNumber(person.getMobileNumber())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .address(person.getAddress())
                .cards(cards)
                .build();
    }
}
