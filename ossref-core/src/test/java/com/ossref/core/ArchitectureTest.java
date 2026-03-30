package com.ossref.core;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {

    private static JavaClasses coreClasses;

    @BeforeAll
    static void setup() {
        coreClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ossref.core");
    }

    @Nested
    @DisplayName("레이어 의존성 규칙")
    class LayerDependencyTest {

        @Test
        @DisplayName("레이어드 아키텍처 의존성 방향: infrastructure → application → domain")
        void layered_architecture_is_respected() {
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("com.ossref.core.domain..")
                    .layer("Application").definedBy("com.ossref.core.application..")
                    .layer("Infrastructure").definedBy("com.ossref.core.infrastructure..")
                    .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Domain 레이어는 Application 레이어에 의존하지 않는다")
        void domain_should_not_depend_on_application() {
            noClasses()
                    .that().resideInAPackage("com.ossref.core.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.ossref.core.application..")
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Domain 레이어는 Infrastructure 레이어에 의존하지 않는다")
        void domain_should_not_depend_on_infrastructure() {
            noClasses()
                    .that().resideInAPackage("com.ossref.core.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.ossref.core.infrastructure..")
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Application 레이어는 Infrastructure 레이어에 의존하지 않는다")
        void application_should_not_depend_on_infrastructure() {
            noClasses()
                    .that().resideInAPackage("com.ossref.core.application..")
                    .should().dependOnClassesThat().resideInAPackage("com.ossref.core.infrastructure..")
                    .check(coreClasses);
        }
    }

    @Nested
    @DisplayName("Domain 레이어 규칙")
    class DomainLayerTest {

        @Test
        @DisplayName("Domain의 Repository 인터페이스는 Spring Data에 의존하지 않는다")
        void domain_repository_should_not_depend_on_spring_data() {
            noClasses()
                    .that().resideInAPackage("com.ossref.core.domain..")
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework.data.jpa..")
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Domain의 Port 인터페이스는 인터페이스여야 한다")
        void domain_ports_should_be_interfaces() {
            classes()
                    .that().resideInAPackage("com.ossref.core.domain..")
                    .and().haveSimpleNameEndingWith("Port")
                    .should().beInterfaces()
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Domain의 Repository는 인터페이스여야 한다")
        void domain_repositories_should_be_interfaces() {
            classes()
                    .that().resideInAPackage("com.ossref.core.domain..")
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().beInterfaces()
                    .check(coreClasses);
        }
    }

    @Nested
    @DisplayName("Infrastructure 레이어 규칙")
    class InfrastructureLayerTest {

        @Test
        @DisplayName("JPA Repository는 Infrastructure 레이어에만 존재한다")
        void jpa_repositories_should_reside_in_infrastructure() {
            classes()
                    .that().haveSimpleNameEndingWith("JpaRepository")
                    .should().resideInAPackage("com.ossref.core.infrastructure..")
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Adapter 구현체는 Infrastructure 레이어에만 존재한다")
        void adapters_should_reside_in_infrastructure() {
            classes()
                    .that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("com.ossref.core.infrastructure..")
                    .check(coreClasses);
        }
    }

    @Nested
    @DisplayName("Application 레이어 규칙")
    class ApplicationLayerTest {

        @Test
        @DisplayName("Application Service는 @Service 어노테이션을 가진다")
        void application_services_should_be_annotated_with_service() {
            classes()
                    .that().resideInAPackage("com.ossref.core.application..")
                    .and().haveSimpleNameEndingWith("Service")
                    .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                    .check(coreClasses);
        }

        @Test
        @DisplayName("Application Service는 Domain의 Port/Repository만 의존한다 (Infrastructure 직접 의존 금지)")
        void application_services_should_only_use_domain_ports() {
            noClasses()
                    .that().resideInAPackage("com.ossref.core.application..")
                    .should().dependOnClassesThat().resideInAPackage("com.ossref.core.infrastructure..")
                    .check(coreClasses);
        }
    }
}
