package com.ossref.core.application.repo;

import com.ossref.core.domain.repo.GithubPort;
import com.ossref.core.domain.repo.GithubPort.GithubRepoInfo;
import com.ossref.core.domain.repo.Repo;
import com.ossref.core.domain.repo.RepoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoSyncService {

    private final RepoRepository repoRepository;
    private final GithubPort githubPort;

    public List<Repo> findAllForSync() {
        return repoRepository.findAll();
    }

    @Transactional
    public Repo sync(Repo repo) {
        String owner = repo.getOwner();
        String name = repo.getName();

        Optional<GithubRepoInfo> info = githubPort.getRepositoryInfo(owner, name);
        if (info.isEmpty()) {
            log.warn("GitHub 정보 조회 실패, 건너뜀: {}/{}", owner, name);
            return null;
        }

        GithubRepoInfo ghInfo = info.get();
        repo.updateFromGithub(
                ghInfo.description(), ghInfo.stars(), ghInfo.lastCommit(),
                ghInfo.tree(), ghInfo.readme(), ghInfo.topics(),
                ghInfo.license(), ghInfo.contributors(), ghInfo.forks(),
                ghInfo.language()
        );

        Repo saved = repoRepository.save(repo);
        log.info("동기화 완료: {}/{} (stars={}, forks={}, contributors={})",
                owner, name, ghInfo.stars(), ghInfo.forks(), ghInfo.contributors());
        return saved;
    }
}
