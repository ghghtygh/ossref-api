package com.ossref.core.infrastructure.github;

import com.ossref.core.domain.repo.GithubPort;
import com.ossref.core.infrastructure.github.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GithubApiAdapter implements GithubPort {

    private final RestClient restClient;

    public GithubApiAdapter(@Value("${github.token:}") String token) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        if (token != null && !token.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        this.restClient = builder
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .build();
    }

    @Override
    public Optional<GithubRepoInfo> getRepositoryInfo(String owner, String repo) {
        try {
            GithubRepoResponse ghRepo = fetchRepository(owner, repo);
            if (ghRepo == null) return Optional.empty();

            String stars = formatCount(ghRepo.getStargazersCount());
            String forks = formatCount(ghRepo.getForksCount());
            String lastCommit = fetchLastCommitDate(owner, repo)
                    .map(GithubApiAdapter::formatRelativeTime).orElse(null);
            List<String> treeList = fetchTreeStructure(owner, repo);
            String tree = treeList.isEmpty() ? null : String.join("|", treeList);
            String readme = fetchReadme(owner, repo).orElse(null);
            String topics = (ghRepo.getTopics() != null && !ghRepo.getTopics().isEmpty())
                    ? String.join(",", ghRepo.getTopics()) : null;
            String license = (ghRepo.getLicense() != null) ? ghRepo.getLicense().getSpdxId() : null;
            int contributors = fetchContributorCount(owner, repo);
            String language = ghRepo.getLanguage();
            String description = ghRepo.getDescription();

            return Optional.of(new GithubRepoInfo(
                    description, stars, forks, language, lastCommit,
                    tree, readme, topics, license, contributors
            ));
        } catch (Exception e) {
            log.error("GitHub 레포지토리 정보 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Optional.empty();
        }
    }

    private GithubRepoResponse fetchRepository(String owner, String repo) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch repo: " + res.getStatusCode());
                    })
                    .body(GithubRepoResponse.class);
        } catch (Exception e) {
            log.warn("GitHub API 호출 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return null;
        }
    }

    private Optional<OffsetDateTime> fetchLastCommitDate(String owner, String repo) {
        try {
            List<GithubCommitResponse> commits = restClient.get()
                    .uri("/repos/{owner}/{repo}/commits?per_page=1", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch commits: " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (commits != null && !commits.isEmpty()) {
                return Optional.ofNullable(commits.get(0).getCommit().getCommitter().getDate());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("커밋 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Optional.empty();
        }
    }

    private List<String> fetchTreeStructure(String owner, String repo) {
        try {
            GithubTreeResponse treeResponse = restClient.get()
                    .uri("/repos/{owner}/{repo}/git/trees/HEAD?recursive=1", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch tree: " + res.getStatusCode());
                    })
                    .body(GithubTreeResponse.class);

            if (treeResponse == null || treeResponse.getTree() == null) {
                return Collections.emptyList();
            }

            return treeResponse.getTree().stream()
                    .filter(node -> !node.getPath().startsWith("."))
                    .filter(node -> node.getPath().chars().filter(c -> c == '/').count() <= 3)
                    .map(node -> {
                        String path = node.getPath();
                        return "tree".equals(node.getType()) ? path + "/" : path;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("트리 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Optional<String> fetchReadme(String owner, String repo) {
        try {
            String readme = restClient.get()
                    .uri("/repos/{owner}/{repo}/readme", owner, repo)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.raw+json")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch readme: " + res.getStatusCode());
                    })
                    .body(String.class);

            if (readme != null && readme.length() > 5000) {
                readme = readme.substring(0, 5000) + "\n\n... (truncated)";
            }
            return Optional.ofNullable(readme);
        } catch (Exception e) {
            log.warn("README 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Optional.empty();
        }
    }

    private int fetchContributorCount(String owner, String repo) {
        try {
            List<GithubContributorResponse> contributors = restClient.get()
                    .uri("/repos/{owner}/{repo}/contributors?per_page=100&anon=true", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch contributors: " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            return contributors != null ? contributors.size() : 0;
        } catch (Exception e) {
            log.warn("컨트리뷰터 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return 0;
        }
    }

    static String formatCount(int count) {
        if (count >= 1000) {
            double value = count / 1000.0;
            if (value == (int) value) {
                return (int) value + "k";
            }
            return String.format("%.1fk", value);
        }
        return String.valueOf(count);
    }

    static String formatRelativeTime(OffsetDateTime dateTime) {
        if (dateTime == null) return null;

        OffsetDateTime now = OffsetDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (days == 0) return "오늘";
        if (days == 1) return "어제";
        if (days < 7) return days + "일 전";
        if (days < 30) return (days / 7) + "주 전";
        if (days < 365) return (days / 30) + "개월 전";
        return (days / 365) + "년 전";
    }

    static class GithubApiException extends RuntimeException {
        GithubApiException(String message) {
            super(message);
        }
    }
}
