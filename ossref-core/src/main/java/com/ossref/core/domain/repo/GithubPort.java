package com.ossref.core.domain.repo;

import java.util.Optional;

public interface GithubPort {

    Optional<GithubRepoInfo> getRepositoryInfo(String owner, String repo);

    record GithubRepoInfo(
            String description,
            String stars,
            String forks,
            String language,
            String lastCommit,
            String tree,
            String readme,
            String topics,
            String license,
            int contributors
    ) {}
}
