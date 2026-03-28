package com.ossref.api.controller;

import com.ossref.api.dto.PageResponse;
import com.ossref.api.dto.RepoDetailResponse;
import com.ossref.api.dto.RepoResponse;
import com.ossref.core.domain.Repo;
import com.ossref.core.repository.RepoRepository;
import com.ossref.api.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoRepository repoRepository;

    @GetMapping
    public PageResponse<RepoResponse> getRepos(
            @RequestParam(defaultValue = "all") String fw,
            @RequestParam(defaultValue = "all") String arch,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        String fwFilter = "all".equals(fw) ? null : fw;
        String archFilter = "all".equals(arch) ? null : arch;
        String qFilter = (q == null || q.isBlank()) ? null : q;

        Page<Repo> result = repoRepository.findByFilters(fwFilter, archFilter, qFilter, PageRequest.of(page - 1, size));

        return new PageResponse<>(
                result.getContent().stream().map(RepoResponse::from).toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRepo(@PathVariable Long id) {
        return repoRepository.findById(id)
                .<ResponseEntity<?>>map(repo -> ResponseEntity.ok(RepoDetailResponse.from(repo)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("NOT_FOUND", "해당 레포지토리를 찾을 수 없습니다.")));
    }
}
