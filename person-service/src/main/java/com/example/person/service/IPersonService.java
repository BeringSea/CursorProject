package com.example.person.service;

import com.example.person.dto.PersonRequestDto;
import com.example.person.dto.PersonResponseDto;

import java.util.List;

public interface IPersonService {
    PersonResponseDto createPerson(PersonRequestDto request);
    PersonResponseDto getPersonById(Long id);
    PersonResponseDto getPersonByEmail(String email);
    List<PersonResponseDto> getAllPersons();
    PersonResponseDto updatePerson(Long id, PersonRequestDto request);
    void deletePerson(Long id);
}
