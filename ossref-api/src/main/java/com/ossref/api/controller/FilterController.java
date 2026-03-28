package com.ossref.api.controller;

import com.ossref.api.dto.FilterResponse;
import com.ossref.api.dto.FilterResponse.FilterOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

    @GetMapping
    public FilterResponse getFilters() {
        List<FilterOption> frameworks = List.of(
                new FilterOption("all", "전체"),
                new FilterOption("spring", "Spring Boot"),
                new FilterOption("fastapi", "FastAPI"),
                new FilterOption("express", "Express"),
                new FilterOption("nestjs", "NestJS")
        );

        List<FilterOption> architectures = List.of(
                new FilterOption("all", "전체"),
                new FilterOption("layered", "Layered"),
                new FilterOption("clean", "Clean"),
                new FilterOption("hexagonal", "Hexagonal"),
                new FilterOption("mvc", "MVC")
        );

        return new FilterResponse(frameworks, architectures);
    }
}
