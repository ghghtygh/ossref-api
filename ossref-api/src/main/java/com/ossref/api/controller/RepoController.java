package com.ossref.api.controller;

import com.ossref.api.dto.ErrorResponse;
import com.ossref.api.dto.PageResponse;
import com.ossref.core.application.repo.RepoQueryService;
import com.ossref.core.application.repo.dto.RepoDetailResult;
import com.ossref.core.application.repo.dto.RepoSearchQuery;
import com.ossref.core.application.repo.dto.RepoSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoQueryService repoQueryService;

    @GetMapping
    public PageResponse<RepoSummaryResult> getRepos(
            @RequestParam(defaultValue = "all") String fw,
            @RequestParam(defaultValue = "all") String arch,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<RepoSummaryResult> result = repoQueryService.search(new RepoSearchQuery(fw, arch, q, page, size));

        return new PageResponse<>(
                result.getContent(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRepo(@PathVariable Long id) {
        return repoQueryService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("NOT_FOUND", "해당 레포지토리를 찾을 수 없습니다.")));
    }
}
