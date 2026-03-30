package com.ossref.core.application.repo.dto;

import com.ossref.core.domain.repo.Repo;

import java.util.Arrays;
import java.util.List;

public record RepoDetailResult(
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
        List<String> tree,
        String readme,
        List<String> topics,
        String license,
        Integer contributors,
        String forks,
        String archDescription
) {
    public static RepoDetailResult from(Repo repo) {
        return new RepoDetailResult(
                repo.getId(), repo.getName(), repo.getOwner(), repo.getDesc(),
                repo.getStars(), repo.getUrl(), repo.getFw(), repo.getArch(),
                repo.getLang(), repo.getCommit(),
                repo.getTree() != null ? Arrays.asList(repo.getTree().split("\\|")) : List.of(),
                repo.getReadme(),
                repo.getTopics() != null ? Arrays.asList(repo.getTopics().split(",")) : List.of(),
                repo.getLicense(), repo.getContributors(), repo.getForks(),
                repo.getArchDescription()
        );
    }
}
