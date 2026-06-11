package com.example.person.dto;

import com.example.person.entity.Person;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PersonResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private Person.Gender gender;
    private String address;
    private List<CardDto> cards;
}
