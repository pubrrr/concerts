package com.bierchitekt.concerts;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record ConcertDTO(String title, LocalDate date, String link, List<String> genre, String location, String price) implements Serializable {
}