package com.ossref.core.application.repo.dto;

import com.ossref.core.domain.repo.Repo;

import java.util.Arrays;
import java.util.List;

public record RepoSummaryResult(
        Long id,
        String name,
        String owner,
        String desc,
        String stars,
        String url,
        String fw,
        String arch,
        String lang,
        String commit,
        List<String> tree
) {
    public static RepoSummaryResult from(Repo repo) {
        return new RepoSummaryResult(
                repo.getId(), repo.getName(), repo.getOwner(), repo.getDesc(),
                repo.getStars(), repo.getUrl(), repo.getFw(), repo.getArch(),
                repo.getLang(), repo.getCommit(),
                repo.getTree() != null ? Arrays.asList(repo.getTree().split("\\|")) : List.of()
        );
    }
}
