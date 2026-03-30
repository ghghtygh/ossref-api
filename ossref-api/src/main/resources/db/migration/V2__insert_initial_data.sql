INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, readme, topics, license, contributors, forks, arch_description) VALUES
('spring-petclinic', 'spring-projects',
 'Spring 공식 레퍼런스 앱. 전형적인 Layered 구조로 컨트롤러-서비스-레포지토리 패턴을 명확하게 보여줌.',
 '7.2k', 'https://github.com/spring-projects/spring-petclinic',
 'spring', 'layered', 'Java', '2주 전',
 'src/main/java/org/springframework/samples/|src/main/java/org/springframework/samples/owner/|src/main/java/org/springframework/samples/owner/Owner.java|src/main/java/org/springframework/samples/owner/OwnerController.java|src/main/java/org/springframework/samples/owner/OwnerRepository.java|src/main/java/org/springframework/samples/pet/|src/main/java/org/springframework/samples/pet/Pet.java|src/main/java/org/springframework/samples/pet/PetController.java|src/main/java/org/springframework/samples/vet/|src/main/java/org/springframework/samples/vet/VetController.java',
 '# Spring PetClinic Sample Application', 'spring-boot,java,sample', 'Apache-2.0', 156, '1.8k',
 '전형적인 3-tier Layered Architecture로 Controller → Service → Repository 계층 구조를 따릅니다.');

INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, topics, license, contributors, forks, arch_description) VALUES
('realworld-springboot', 'gothinkster',
 'Medium 클론 풀스택 앱. JWT 인증, 페이징, 관계형 데이터 처리가 포함된 실전형 Spring 프로젝트.',
 '3.1k', 'https://github.com/gothinkster/spring-boot-realworld-example-app',
 'spring', 'layered', 'Java', '1개월 전',
 'src/main/java/io/spring/|src/main/java/io/spring/api/|src/main/java/io/spring/api/exception/|src/main/java/io/spring/application/|src/main/java/io/spring/application/ArticleService.java|src/main/java/io/spring/application/UserService.java|src/main/java/io/spring/infrastructure/|src/main/java/io/spring/infrastructure/mybatis/',
 'spring-boot,realworld,jwt', 'MIT', 89, '1.2k',
 'API, Application, Infrastructure 레이어로 분리된 Layered Architecture입니다.');

INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, topics, license, contributors, forks, arch_description) VALUES
('fastapi-realworld', 'nsidnev',
 'FastAPI + PostgreSQL로 구현한 RealWorld 앱. Dependency Injection과 레이어 분리가 잘 된 Clean 구조.',
 '2.8k', 'https://github.com/nsidnev/fastapi-realworld-example-app',
 'fastapi', 'clean', 'Python', '3개월 전',
 'app/|app/api/|app/api/routes/|app/core/|app/core/settings.py|app/db/|app/db/queries/|app/db/repositories/|app/models/',
 'fastapi,python,realworld', 'MIT', 45, '800',
 'Use Case 중심의 Clean Architecture로 외부 의존성이 내부 도메인에 영향을 주지 않는 구조입니다.');

INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, topics, license, contributors, forks, arch_description) VALUES
('nestjs-realworld', 'lujakob',
 'NestJS + TypeORM 기반 RealWorld 구현. NestJS의 모듈 시스템과 데코레이터 패턴을 실전에서 활용한 사례.',
 '2.4k', 'https://github.com/lujakob/nestjs-realworld-example-app',
 'nestjs', 'mvc', 'TypeScript', '2개월 전',
 'src/|src/article/|src/article/article.controller.ts|src/article/article.service.ts|src/article/article.entity.ts|src/user/|src/user/user.module.ts|src/shared/',
 'nestjs,typeorm,typescript', 'MIT', 67, '950',
 'NestJS의 Module-Controller-Service 패턴을 따르는 MVC Architecture입니다.');

INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, topics, license, contributors, forks, arch_description) VALUES
('hexagonal-spring', 'thombergs',
 '《만들면서 배우는 클린 아키텍처》 예제 코드. Hexagonal 아키텍처를 Spring으로 구현한 교과서적 사례.',
 '5.1k', 'https://github.com/thombergs/buckpal',
 'spring', 'hexagonal', 'Java', '1개월 전',
 'src/main/java/|src/main/java/account/|src/main/java/account/adapter/|src/main/java/account/adapter/in/|src/main/java/account/adapter/in/web/|src/main/java/account/adapter/out/|src/main/java/account/adapter/out/persistence/|src/main/java/account/application/|src/main/java/account/application/port/|src/main/java/account/application/service/|src/main/java/account/domain/',
 'hexagonal-architecture,spring-boot,clean-architecture', 'MIT', 34, '2.1k',
 'Port & Adapter 패턴 기반 Hexagonal Architecture로 도메인이 외부 기술에 의존하지 않는 구조입니다.');

INSERT INTO repo (name, owner, description, stars, url, fw, arch, lang, last_commit, tree, topics, license, contributors, forks, arch_description) VALUES
('express-api-starter', 'w3tecch',
 'Express + TypeScript 기반 REST API. 미들웨어 구성, 에러 핸들링, DI 패턴이 잘 구성된 실전형 스타터.',
 '1.9k', 'https://github.com/w3tecch/express-typescript-boilerplate',
 'express', 'layered', 'TypeScript', '5개월 전',
 'src/|src/api/|src/api/controllers/|src/api/middlewares/|src/api/services/|src/config/|src/database/',
 'express,typescript,boilerplate', 'MIT', 78, '650',
 '미들웨어 기반의 Layered Architecture로 Controller → Service → Model 구조를 따릅니다.');
