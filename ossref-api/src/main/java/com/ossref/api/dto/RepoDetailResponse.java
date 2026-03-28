package com.ossref.api.dto;

import com.ossref.core.domain.Repo;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class RepoDetailResponse {
    private Long id;
    private String name;
    private String owner;
    private String desc;
    private String stars;
    private String url;
    private String fw;
    private String arch;
    private String lang;
    private String commit;
    private List<String> tree;
    private String readme;
    private List<String> topics;
    private String license;
    private Integer contributors;
    private String forks;
    private String archDescription;

    public static RepoDetailResponse from(Repo repo) {
        return RepoDetailResponse.builder()
                .id(repo.getId())
                .name(repo.getName())
                .owner(repo.getOwner())
                .desc(repo.getDesc())
                .stars(repo.getStars())
                .url(repo.getUrl())
                .fw(repo.getFw())
                .arch(repo.getArch())
                .lang(repo.getLang())
                .commit(repo.getCommit())
                .tree(repo.getTree() != null ? Arrays.asList(repo.getTree().split("\\|")) : List.of())
                .readme(repo.getReadme())
                .topics(repo.getTopics() != null ? Arrays.asList(repo.getTopics().split(",")) : List.of())
                .license(repo.getLicense())
                .contributors(repo.getContributors())
                .forks(repo.getForks())
                .archDescription(repo.getArchDescription())
                .build();
    }
}
