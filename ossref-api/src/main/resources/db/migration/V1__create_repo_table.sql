CREATE TABLE repo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    owner           VARCHAR(255)  NOT NULL,
    description     VARCHAR(1000),
    stars           VARCHAR(255),
    url             VARCHAR(255)  NOT NULL,
    fw              VARCHAR(255)  NOT NULL,
    arch            VARCHAR(255)  NOT NULL,
    lang            VARCHAR(255)  NOT NULL,
    last_commit     VARCHAR(255),
    tree            VARCHAR(2000),
    readme          TEXT,
    topics          VARCHAR(255),
    license         VARCHAR(255),
    contributors    INT,
    forks           VARCHAR(255),
    arch_description VARCHAR(1000)
);
