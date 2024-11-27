package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConcertMapper {

        List<ConcertDTO> toConcertDto(List<ConcertEntity> concerts);

        ConcertEntity toConcertEntity(ConcertDTO concertDTO);
}
