package com.ossref.core.application.repo;

import com.ossref.core.application.repo.dto.RepoDetailResult;
import com.ossref.core.application.repo.dto.RepoSearchQuery;
import com.ossref.core.application.repo.dto.RepoSummaryResult;
import com.ossref.core.domain.repo.Repo;
import com.ossref.core.domain.repo.RepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepoQueryService {

    private final RepoRepository repoRepository;

    public Page<RepoSummaryResult> search(RepoSearchQuery query) {
        String fw = "all".equals(query.fw()) ? null : query.fw();
        String arch = "all".equals(query.arch()) ? null : query.arch();
        String q = (query.q() == null || query.q().isBlank()) ? null : query.q();

        Page<Repo> page = repoRepository.findByFilters(fw, arch, q, PageRequest.of(query.page() - 1, query.size()));
        return page.map(RepoSummaryResult::from);
    }

    public Optional<RepoDetailResult> findById(Long id) {
        return repoRepository.findById(id).map(RepoDetailResult::from);
    }
}
