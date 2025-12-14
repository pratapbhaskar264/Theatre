package com.bhaskar.theatre.dto;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Data
@Builder
public class PagedApiResponseDto {
    int totalPages;
    long totalElements;
    List<?> currentPageData;
    int currentCount;


}