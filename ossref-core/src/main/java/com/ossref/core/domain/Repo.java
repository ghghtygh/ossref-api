package com.ossref.core.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String owner;

    @Column(name = "description", length = 1000)
    private String desc;

    private String stars;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String fw;

    @Column(nullable = false)
    private String arch;

    @Column(nullable = false)
    private String lang;

    @Column(name = "last_commit")
    private String commit;

    @Column(name = "tree", length = 2000)
    private String tree;

    @Column(length = 10000)
    private String readme;

    private String topics;

    private String license;

    private Integer contributors;

    private String forks;

    @Column(name = "arch_description", length = 1000)
    private String archDescription;

    @Builder
    public Repo(String name, String owner, String desc, String stars, String url,
                String fw, String arch, String lang, String commit, String tree,
                String readme, String topics, String license, Integer contributors,
                String forks, String archDescription) {
        this.name = name;
        this.owner = owner;
        this.desc = desc;
        this.stars = stars;
        this.url = url;
        this.fw = fw;
        this.arch = arch;
        this.lang = lang;
        this.commit = commit;
        this.tree = tree;
        this.readme = readme;
        this.topics = topics;
        this.license = license;
        this.contributors = contributors;
        this.forks = forks;
        this.archDescription = archDescription;
    }

    public void updateFromGithub(String desc, String stars, String commit, String tree,
                                  String readme, String topics, String license,
                                  Integer contributors, String forks, String lang) {
        this.desc = desc;
        this.stars = stars;
        this.commit = commit;
        this.tree = tree;
        this.readme = readme;
        this.topics = topics;
        this.license = license;
        this.contributors = contributors;
        this.forks = forks;
        this.lang = lang;
    }
}
