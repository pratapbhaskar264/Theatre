package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.TheatreRequestDto;
import com.bhaskar.theatre.entity.Theatre;
import com.bhaskar.theatre.exception.TheatreNotFoundException;
import com.bhaskar.theatre.repository.TheatreRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

import static com.bhaskar.theatre.constant.ExceptionMessages.THEATRE_NOT_FOUND;

@Service
public class TheatreService {


        private final TheatreRespository theatreRepository;

    public TheatreService(TheatreRespository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }


    public Page<Theatre> getAllTheatres(int page, int size) {
            return theatreRepository.findAll(PageRequest.of(page, size));
    }


    public Page<Theatre> getAllTheatresByLocation(int page, int size, String location) {

        return theatreRepository.findAllByLocation(location, (Pageable) PageRequest.of(page, size));
    }


    public Theatre getTheatreById(long theaterId) {
        return theatreRepository.findById(theaterId)
                .orElseThrow(() -> new TheatreNotFoundException(THEATRE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    public Theatre createNewTheatre(TheatreRequestDto theaterRequestDto) {
        Theatre theater = Theatre.builder()
                .name(theaterRequestDto.getName())
                .location(theaterRequestDto.getLocation())
                .build();
        return theatreRepository.save(theater);
    }

    public Theatre updateTheatreById(long theaterId, TheatreRequestDto theaterRequestDto) {
        return theatreRepository.findById(theaterId)
                .map(theater -> {
                    theater.setName(theaterRequestDto.getName());
                    theater.setLocation(theater.getLocation());
                    return theatreRepository.save(theater);
                })
                .orElseThrow(() -> new TheatreNotFoundException(THEATRE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    public void deleteTheatreById(long theaterId) {
        theatreRepository.deleteById(theaterId);
        return;
    }
}

