package com.ossref.batch.job;

import com.ossref.core.domain.Repo;
import com.ossref.core.github.GithubApiClient;
import com.ossref.core.github.dto.GithubRepoResponse;
import com.ossref.core.repository.RepoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubSyncJobConfig {

    private final RepoRepository repoRepository;
    private final GithubApiClient githubApiClient;

    @Bean
    public Job githubSyncJob(JobRepository jobRepository, Step githubSyncStep) {
        return new JobBuilder("githubSyncJob", jobRepository)
                .start(githubSyncStep)
                .build();
    }

    @Bean
    public Step githubSyncStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("githubSyncStep", jobRepository)
                .<Repo, Repo>chunk(5, transactionManager)
                .reader(repoReader())
                .processor(githubSyncProcessor())
                .writer(repoWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    public ItemReader<Repo> repoReader() {
        return new ItemReader<>() {
            private Iterator<Repo> iterator;

            @Override
            public Repo read() {
                if (iterator == null) {
                    List<Repo> repos = repoRepository.findAll();
                    log.info("GitHub 동기화 대상 레포지토리: {}개", repos.size());
                    iterator = repos.iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemProcessor<Repo, Repo> githubSyncProcessor() {
        return repo -> {
            String owner = repo.getOwner();
            String name = repo.getName();
            log.info("동기화 시작: {}/{}", owner, name);

            Optional<GithubRepoResponse> repoResponse = githubApiClient.getRepository(owner, name);
            if (repoResponse.isEmpty()) {
                log.warn("레포지토리 정보 조회 실패, 건너뜀: {}/{}", owner, name);
                return null; // 프로세서에서 null 반환 시 해당 아이템은 writer로 전달되지 않음
            }

            GithubRepoResponse ghRepo = repoResponse.get();

            // 스타, 포크 수 포맷팅
            String stars = GithubApiClient.formatCount(ghRepo.getStargazersCount());
            String forks = GithubApiClient.formatCount(ghRepo.getForksCount());

            // 마지막 커밋 시점
            Optional<OffsetDateTime> lastCommitDate = githubApiClient.getLastCommitDate(owner, name);
            String commit = lastCommitDate.map(GithubApiClient::formatRelativeTime).orElse(repo.getCommit());

            // 트리 구조
            List<String> treeList = githubApiClient.getTreeStructure(owner, name);
            String tree = treeList.isEmpty() ? repo.getTree() : String.join("|", treeList);

            // README
            String readme = githubApiClient.getReadme(owner, name).orElse(repo.getReadme());

            // 토픽
            String topics = (ghRepo.getTopics() != null && !ghRepo.getTopics().isEmpty())
                    ? String.join(",", ghRepo.getTopics())
                    : repo.getTopics();

            // 라이선스
            String license = (ghRepo.getLicense() != null)
                    ? ghRepo.getLicense().getSpdxId()
                    : repo.getLicense();

            // 컨트리뷰터 수
            int contributors = githubApiClient.getContributorCount(owner, name);

            // 언어
            String lang = (ghRepo.getLanguage() != null) ? ghRepo.getLanguage() : repo.getLang();

            // 설명
            String desc = (ghRepo.getDescription() != null) ? ghRepo.getDescription() : repo.getDesc();

            repo.updateFromGithub(desc, stars, commit, tree, readme, topics, license, contributors, forks, lang);

            log.info("동기화 완료: {}/{} (stars={}, forks={}, contributors={})", owner, name, stars, forks, contributors);
            return repo;
        };
    }

    @Bean
    public ItemWriter<Repo> repoWriter() {
        return repos -> {
            repoRepository.saveAll(repos);
            log.info("{}개 레포지토리 저장 완료", repos.size());
        };
    }
}
