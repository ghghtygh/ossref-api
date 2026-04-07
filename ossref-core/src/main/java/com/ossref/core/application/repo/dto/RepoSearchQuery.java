package com.ossref.core.application.repo.dto;

public record RepoSearchQuery(
        String fw,
        String arch,
        String q,
        int page,
        int size
) {
    private static final int MAX_SIZE = 100;
    private static final int MAX_QUERY_LENGTH = 100;

    public RepoSearchQuery {
        if (fw == null) fw = "all";
        if (arch == null) arch = "all";
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > MAX_SIZE) size = MAX_SIZE;
        if (q != null && q.length() > MAX_QUERY_LENGTH) q = q.substring(0, MAX_QUERY_LENGTH);
    }
}
