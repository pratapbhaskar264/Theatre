package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Theatre;
import com.bhaskar.theatre.repository.TheatreRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class TheatreService {


        private final TheatreRespository theatreRepository;

    public TheatreService(TheatreRespository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }


    public Page<Theatre> getAllTheatres(int page, int size) {
            return theatreRepository.findAll(PageRequest.of(page, size));
    }


}
