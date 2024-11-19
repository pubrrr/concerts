package com.bierchitekt.concerts;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertEntity {
    @Id
    String title;
    LocalDate date;
    String link;
    List<String> genre;
    String location;

}