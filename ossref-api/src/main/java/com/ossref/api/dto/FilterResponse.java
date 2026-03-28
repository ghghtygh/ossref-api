package com.ossref.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FilterResponse {
    private List<FilterOption> frameworks;
    private List<FilterOption> architectures;

    @Getter
    @AllArgsConstructor
    public static class FilterOption {
        private String value;
        private String label;
    }
}
