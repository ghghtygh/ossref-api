# ossref-api

아키텍처를 참고할 수 있는 오픈소스 추천 서비스 **ossref-web**의 백엔드 API 서버입니다.

---

## 기술 스택

- Java 21
- Spring Boot 3.4.4
- Spring Batch
- Spring Data JPA
- H2 (개발) / PostgreSQL (운영 전환 가능)
- Gradle 8.12 (멀티모듈)

---

## 프로젝트 구조

```
ossref-api/
├── ossref-core/          # 공통 모듈 (엔티티, 리포지토리, GitHub API 클라이언트)
│   └── src/main/java/com/ossref/core/
│       ├── domain/
│       │   └── Repo.java                  # 레포지토리 엔티티
│       ├── repository/
│       │   └── RepoRepository.java        # JPA Repository (필터 검색 쿼리)
│       └── github/
│           ├── GithubApiClient.java       # GitHub REST API 클라이언트
│           └── dto/                       # GitHub API 응답 DTO
│
├── ossref-api/           # REST API 모듈 (Spring Boot Application)
│   └── src/main/java/com/ossref/api/
│       ├── OssrefApiApplication.java
│       ├── controller/
│       │   ├── RepoController.java        # GET /api/repos, GET /api/repos/{id}
│       │   └── FilterController.java      # GET /api/filters
│       ├── dto/                           # 응답 DTO
│       └── config/
│           ├── WebConfig.java             # CORS 설정
│           └── GlobalExceptionHandler.java
│
├── ossref-batch/         # 배치 모듈 (Spring Batch)
│   └── src/main/java/com/ossref/batch/
│       ├── OssrefBatchApplication.java
│       └── job/
│           └── GithubSyncJobConfig.java   # GitHub 데이터 동기화 Job
│
├── Dockerfile
├── .github/workflows/ci-cd.yaml
└── build.gradle          # 루트 빌드 스크립트
```

### 모듈 의존 관계

```
ossref-api  ──→  ossref-core
ossref-batch ──→  ossref-core
```

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
- 시작 시 `data.sql`의 초기 데이터 6건이 자동 삽입됩니다.

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
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ossref \
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
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:ossref` | DB 접속 URL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | (빈 값) | DB 비밀번호 |

---

## 프론트엔드 연동

프론트엔드([ossref-web](https://github.com/ghghtygh/ossref-web))에서 이 API를 사용합니다.

Kubernetes 환경에서는 nginx에 프록시 설정을 추가하거나, Ingress 레벨에서 `/api` 경로를 백엔드 서비스로 라우팅합니다.

```nginx
# ossref-web nginx.conf에 추가
location /api/ {
    proxy_pass http://ossref-api-service:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```
