package com.ossref.core.domain.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RepoRepository {

    Page<Repo> findByFilters(String fw, String arch, String q, Pageable pageable);

    Optional<Repo> findById(Long id);

    List<Repo> findAll();

    Repo save(Repo repo);

    List<Repo> saveAll(Iterable<Repo> repos);
}
