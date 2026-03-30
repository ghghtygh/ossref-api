# ossref-api

아키텍처를 참고할 수 있는 오픈소스 추천 서비스 **ossref-web**의 백엔드 API 서버입니다.

---

## 기술 스택

- Java 21
- Spring Boot 3.4.4
- Spring Batch
- Spring Data JPA
- H2 (개발) / MySQL (운영)
- Flyway (DB 마이그레이션)
- Gradle 8.12 (멀티모듈)

---

## 아키텍처

Clean Architecture + DDD 기반 구조를 채택하고 있습니다.

```
ossref-api/
├── ossref-core/          # 핵심 모듈 (Clean Architecture)
│   └── src/main/java/com/ossref/core/
│       ├── domain/                        # Domain 레이어
│       │   └── repo/
│       │       ├── Repo.java              #   Aggregate Root (엔티티)
│       │       ├── RepoRepository.java    #   Port (인터페이스)
│       │       └── GithubPort.java        #   Output Port (GitHub 연동 추상화)
│       │
│       ├── application/                   # Application 레이어
│       │   └── repo/
│       │       ├── RepoQueryService.java  #   조회 유스케이스
│       │       ├── RepoSyncService.java   #   동기화 유스케이스
│       │       └── dto/                   #   유스케이스 입출력 DTO
│       │
│       └── infrastructure/                # Infrastructure 레이어
│           ├── persistence/
│           │   ├── RepoJpaRepository.java #   JPA Repository (Adapter)
│           │   └── RepoRepositoryImpl.java#   RepoRepository 구현체
│           └── github/
│               ├── GithubApiAdapter.java  #   GithubPort 구현체
│               └── dto/                   #   GitHub API 응답 DTO
│
├── ossref-api/           # REST API 모듈
│   └── src/main/java/com/ossref/api/
│       ├── controller/                    # API 엔드포인트
│       ├── dto/                           # HTTP 응답 DTO
│       └── config/                        # CORS, 예외 처리
│
├── ossref-batch/         # 배치 모듈 (Spring Batch)
│   └── src/main/java/com/ossref/batch/
│       └── job/
│           └── GithubSyncJobConfig.java   # GitHub 동기화 Chunk Job
│
├── Dockerfile
├── .github/workflows/ci-cd.yaml
└── build.gradle
```

### 의존성 방향

```
Controller(api) ──→ Application Service(core) ──→ Domain(core)
                                                       ↑
  Batch Job(batch) ──→ Application Service(core)   Infrastructure(core) 가 구현
```

- **Domain**: 비즈니스 규칙 + Port 인터페이스 (외부 의존 없음)
- **Application**: 유스케이스 오케스트레이션 (Port를 통해 인프라 접근)
- **Infrastructure**: Port 구현체 (JPA, GitHub API)
- **api/batch**: Application 레이어만 의존

ArchUnit 테스트로 이 의존성 방향이 자동 검증됩니다.

---

## API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/repos` | 레포지토리 목록 조회 (fw, arch, q, page, size 파라미터) |
| GET | `/api/repos/{id}` | 레포지토리 상세 조회 |
| GET | `/api/filters` | 프레임워크/아키텍처 필터 옵션 목록 |

### 요청 예시

```bash
# 전체 목록
curl http://localhost:8080/api/repos

# 프레임워크 필터
curl http://localhost:8080/api/repos?fw=spring

# 검색
curl http://localhost:8080/api/repos?q=petclinic

# 상세 조회
curl http://localhost:8080/api/repos/1

# 필터 옵션
curl http://localhost:8080/api/filters
```

---

## 로컬 실행

### API 서버

```bash
./gradlew :ossref-api:bootRun
```

- 서버: `http://localhost:8080`
- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:ossref`)
- 시작 시 Flyway 마이그레이션으로 초기 데이터 6건이 자동 삽입됩니다.

### Batch 서버

```bash
GITHUB_TOKEN=ghp_xxxx ./gradlew :ossref-batch:bootRun
```

- `GITHUB_TOKEN` 환경변수 설정을 권장합니다 (미설정 시 GitHub API rate limit: 60회/시간).
- `spring.batch.job.enabled=false`로 설정되어 있어 앱 시작 시 자동 실행되지 않습니다.

---

## 배포

### 배포 흐름

```
main 브랜치 Push
    ↓
GitHub Actions (ci-cd.yaml)
    ├─ Gradle 빌드 + 테스트
    ├─ Docker 이미지 빌드 (멀티스테이지)
    ├─ GHCR에 이미지 푸시
    │   ├─ ghcr.io/ghghtygh/ossref-api:{commit-sha}
    │   └─ ghcr.io/ghghtygh/ossref-api:latest
    └─ k8s-manifests 레포 이미지 태그 업데이트
        ↓
    Kubernetes 배포 (ArgoCD 등)
```

### Docker 빌드

```bash
# 로컬 빌드
docker build -t ossref-api .

# 실행
docker run -p 8080:8080 ossref-api

# 환경변수 전달
docker run -p 8080:8080 \
  -e GITHUB_TOKEN=ghp_xxxx \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/ossref \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  ossref-api
```

### GitHub Actions 필요 설정

| 구분 | 이름 | 설명 |
|------|------|------|
| Secret | `K8S_MANIFESTS_PAT` | k8s-manifests 레포 접근용 Personal Access Token |

`GITHUB_TOKEN`은 Actions에서 자동 제공됩니다 (GHCR 푸시용).

---

## 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `GITHUB_TOKEN` | (없음) | GitHub API 인증 토큰 |
| `SERVER_PORT` | `8080` | API 서버 포트 |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:ossref` (local) / `jdbc:mysql://...` (prod) | DB 접속 URL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | (빈 값) | DB 비밀번호 |

---

## 프론트엔드 연동

프론트엔드([ossref-web](https://github.com/ghghtygh/ossref-web))에서 이 API를 사용합니다.

Kubernetes 환경에서는 Ingress 레벨에서 `/api` 경로를 백엔드 서비스로 라우팅합니다.

```
ossref.gpglab.site
    ├── /      → ossref-web (nginx, port 80)
    └── /api   → ossref-api (Spring Boot, port 8080)
```
