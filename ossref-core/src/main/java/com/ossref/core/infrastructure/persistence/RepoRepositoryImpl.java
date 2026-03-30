package com.ossref.core.infrastructure.persistence;

import com.ossref.core.domain.repo.Repo;
import com.ossref.core.domain.repo.RepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RepoRepositoryImpl implements RepoRepository {

    private final RepoJpaRepository jpaRepository;

    @Override
    public Page<Repo> findByFilters(String fw, String arch, String q, Pageable pageable) {
        return jpaRepository.findByFilters(fw, arch, q, pageable);
    }

    @Override
    public Optional<Repo> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Repo> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Repo save(Repo repo) {
        return jpaRepository.save(repo);
    }

    @Override
    public List<Repo> saveAll(Iterable<Repo> repos) {
        return jpaRepository.saveAll(repos);
    }
}
