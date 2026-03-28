package com.ossref.api.dto;

import com.ossref.core.domain.Repo;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class RepoResponse {
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

    public static RepoResponse from(Repo repo) {
        return RepoResponse.builder()
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
                .build();
    }
}
