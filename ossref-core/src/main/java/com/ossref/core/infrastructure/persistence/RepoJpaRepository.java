package com.ossref.core.infrastructure.persistence;

import com.ossref.core.domain.repo.Repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RepoJpaRepository extends JpaRepository<Repo, Long> {

    @Query("""
        SELECT r FROM Repo r
        WHERE (:fw IS NULL OR r.fw = :fw)
          AND (:arch IS NULL OR r.arch = :arch)
          AND (:q IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(r.desc) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Repo> findByFilters(@Param("fw") String fw,
                             @Param("arch") String arch,
                             @Param("q") String q,
                             Pageable pageable);
}
