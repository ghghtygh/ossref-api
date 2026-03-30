package com.ossref.core.application.repo.dto;

public record RepoSearchQuery(
        String fw,
        String arch,
        String q,
        int page,
        int size
) {
    public RepoSearchQuery {
        if (fw == null) fw = "all";
        if (arch == null) arch = "all";
        if (page < 1) page = 1;
        if (size < 1) size = 20;
    }
}
