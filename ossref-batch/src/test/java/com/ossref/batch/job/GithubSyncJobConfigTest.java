package com.ossref.batch.job;

import com.ossref.core.domain.repo.GithubPort;
import com.ossref.core.domain.repo.GithubPort.GithubRepoInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("GithubSyncJob 통합 테스트")
class GithubSyncJobConfigTest {

    private static final int INITIAL_REPO_COUNT = 6;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @MockitoBean
    private GithubPort githubPort;

    @AfterEach
    void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("모든 레포 동기화가 성공하면 Job이 COMPLETED 상태로 끝난다")
    void job_completes_when_all_repos_sync_successfully() throws Exception {
        when(githubPort.getRepositoryInfo(anyString(), anyString()))
                .thenReturn(Optional.of(stubGithubInfo()));

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
    }

    @Test
    @DisplayName("Step은 V2 마이그레이션의 모든 레포(6건)를 chunk 단위로 처리한다")
    void step_processes_all_seeded_repos() throws Exception {
        when(githubPort.getRepositoryInfo(anyString(), anyString()))
                .thenReturn(Optional.of(stubGithubInfo()));

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getStepName()).isEqualTo("githubSyncStep");
        assertThat(step.getReadCount()).isEqualTo(INITIAL_REPO_COUNT);
        assertThat(step.getWriteCount()).isEqualTo(INITIAL_REPO_COUNT);
        assertThat(step.getSkipCount()).isZero();
        verify(githubPort, atLeastOnce()).getRepositoryInfo(anyString(), anyString());
    }

    @Test
    @DisplayName("일부 레포에서 예외가 발생해도 skip 정책에 따라 Job은 COMPLETED로 끝난다")
    void job_completes_with_skip_when_processor_throws() throws Exception {
        when(githubPort.getRepositoryInfo(anyString(), anyString()))
                .thenReturn(Optional.of(stubGithubInfo()));
        when(githubPort.getRepositoryInfo(eq("spring-projects"), anyString()))
                .thenThrow(new RuntimeException("GitHub API rate limit"));

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getSkipCount()).isPositive();
        assertThat(step.getReadCount() - step.getSkipCount()).isEqualTo(step.getWriteCount());
    }

    @Test
    @DisplayName("GithubPort가 빈 결과를 반환하는 레포는 writer로 전달되지 않는다")
    void repos_with_empty_github_info_are_filtered_out() throws Exception {
        when(githubPort.getRepositoryInfo(anyString(), anyString()))
                .thenReturn(Optional.empty());

        JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParameters());

        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(step.getReadCount()).isEqualTo(INITIAL_REPO_COUNT);
        assertThat(step.getWriteCount()).isZero();
        assertThat(step.getFilterCount()).isEqualTo(INITIAL_REPO_COUNT);
    }

    private JobParameters uniqueParameters() {
        return new JobParametersBuilder()
                .addLong("runAt", System.nanoTime())
                .toJobParameters();
    }

    private GithubRepoInfo stubGithubInfo() {
        return new GithubRepoInfo(
                "synced description",
                "10k",
                "1k",
                "Java",
                "1일 전",
                "src/",
                "# Synced",
                "topic1,topic2",
                "MIT",
                42
        );
    }
}
