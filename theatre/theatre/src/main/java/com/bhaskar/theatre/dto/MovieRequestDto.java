package com.bhaskar.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MovieRequestDto {

    String movieName;
    List<String> genre;
    int movieLength;
    String movieLanguage;
    String releaseDate;

}
