package com.bierchitekt.concerts.persistence;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcertEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private LocalDate date;
    private String link;
    private List<String> genre;
    private String location;
    private String price;
    private boolean notified;

}