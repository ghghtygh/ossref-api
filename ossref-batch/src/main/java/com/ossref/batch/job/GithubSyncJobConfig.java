package com.ossref.batch.job;

import com.ossref.core.application.repo.RepoSyncService;
import com.ossref.core.domain.repo.Repo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubSyncJobConfig {

    private final RepoSyncService repoSyncService;

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
                .writer(chunk -> log.info("{}개 레포지토리 동기화 처리 완료", chunk.size()))
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
                    List<Repo> repos = repoSyncService.findAllForSync();
                    log.info("GitHub 동기화 대상 레포지토리: {}개", repos.size());
                    iterator = repos.iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemProcessor<Repo, Repo> githubSyncProcessor() {
        return repoSyncService::sync;
    }
}
