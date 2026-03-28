package com.ossref.core.github;

import com.ossref.core.github.dto.*;
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
public class GithubApiClient {

    private final RestClient restClient;

    public GithubApiClient(@Value("${github.token:}") String token) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        if (token != null && !token.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        this.restClient = builder
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .build();
    }

    /**
     * 레포지토리 기본 정보 조회 (stars, forks, description, language, topics, license)
     */
    public Optional<GithubRepoResponse> getRepository(String owner, String repo) {
        try {
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/repos/{owner}/{repo}", owner, repo)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, (req, res) -> {
                                throw new GithubApiException("Failed to fetch repo: " + res.getStatusCode());
                            })
                            .body(GithubRepoResponse.class)
            );
        } catch (Exception e) {
            log.warn("GitHub API 호출 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 최근 커밋 목록 조회 (마지막 커밋 시점 계산용)
     */
    public Optional<OffsetDateTime> getLastCommitDate(String owner, String repo) {
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

    /**
     * 디렉토리 트리 구조 조회 (기본 브랜치 기준, 1단계 깊이)
     */
    public List<String> getTreeStructure(String owner, String repo) {
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

            // 주요 소스 디렉토리만 필터링 (최대 2단계 깊이)
            return treeResponse.getTree().stream()
                    .filter(node -> "tree".equals(node.getType()))
                    .map(GithubTreeResponse.TreeNode::getPath)
                    .filter(path -> {
                        long depth = path.chars().filter(c -> c == '/').count();
                        return depth <= 2;
                    })
                    .filter(path -> !path.startsWith("."))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("트리 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * README 내용 조회
     */
    public Optional<String> getReadme(String owner, String repo) {
        try {
            String readme = restClient.get()
                    .uri("/repos/{owner}/{repo}/readme", owner, repo)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.raw+json")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch readme: " + res.getStatusCode());
                    })
                    .body(String.class);

            // README가 너무 길면 앞부분만 저장
            if (readme != null && readme.length() > 5000) {
                readme = readme.substring(0, 5000) + "\n\n... (truncated)";
            }
            return Optional.ofNullable(readme);
        } catch (Exception e) {
            log.warn("README 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 컨트리뷰터 수 조회
     */
    public int getContributorCount(String owner, String repo) {
        try {
            List<GithubContributorResponse> contributors = restClient.get()
                    .uri("/repos/{owner}/{repo}/contributors?per_page=1&anon=true", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch contributors: " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            // Link 헤더에서 전체 수를 가져올 수 없으므로, 전체 목록을 가져오는 대신
            // per_page=100으로 여러 페이지를 조회하거나 근사치를 사용
            // 여기서는 간단히 per_page=100으로 한 페이지만 조회
            List<GithubContributorResponse> allContributors = restClient.get()
                    .uri("/repos/{owner}/{repo}/contributors?per_page=100&anon=true", owner, repo)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException("Failed to fetch contributors: " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            return allContributors != null ? allContributors.size() : 0;
        } catch (Exception e) {
            log.warn("컨트리뷰터 조회 실패 - {}/{}: {}", owner, repo, e.getMessage());
            return 0;
        }
    }

    /**
     * 스타 수를 사람이 읽기 쉬운 형식으로 변환 (예: 7200 → "7.2k")
     */
    public static String formatCount(int count) {
        if (count >= 1000) {
            double value = count / 1000.0;
            if (value == (int) value) {
                return (int) value + "k";
            }
            return String.format("%.1fk", value);
        }
        return String.valueOf(count);
    }

    /**
     * 마지막 커밋 시점을 상대 시간 문자열로 변환 (예: "2주 전", "3개월 전")
     */
    public static String formatRelativeTime(OffsetDateTime dateTime) {
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

    public static class GithubApiException extends RuntimeException {
        public GithubApiException(String message) {
            super(message);
        }
    }
}
