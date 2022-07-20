CREATE TABLE `myosotis_authority`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `username`    varchar(32) COLLATE utf8mb4_unicode_ci                       NOT NULL,
    `namespace`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `create_time` datetime                                                     NOT NULL,
    `update_time` datetime                                                     NOT NULL,
    PRIMARY KEY (`id`),
    KEY           `idx_namespace` (`namespace`),
    KEY           `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `myosotis_config`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
    `namespace`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `config_key`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description`  varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `config_value` varchar(1024) COLLATE utf8mb4_unicode_ci                     NOT NULL,
    `version`      int unsigned NOT NULL DEFAULT '1',
    `create_time`  datetime                                                     NOT NULL,
    `update_time`  datetime                                                     NOT NULL,
    PRIMARY KEY (`id`),
    KEY            `udx_namespace_configkey` (`namespace`,`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `myosotis_namespace`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `namespace`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `create_time` datetime                                                     NOT NULL,
    `update_time` datetime                                                     NOT NULL,
    PRIMARY KEY (`id`),
    KEY           `udx_namespace` (`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `myosotis_namespace`
    (`namespace`, `description`, `create_time`, `update_time`)
VALUES ('default', 'default namespace', '1970-01-01 08:00:00', '1970-01-01 08:00:00');

CREATE TABLE `myosotis_session`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `session_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    `username`    varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
    `private_key` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `expire_time` datetime                               NOT NULL,
    `create_time` datetime                               NOT NULL,
    `update_time` datetime                               NOT NULL,
    PRIMARY KEY (`id`),
    KEY           `idx_sessionkey` (`session_key`),
    KEY           `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `myosotis_user`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `username`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `password`    varchar(32) COLLATE utf8mb4_unicode_ci                       NOT NULL,
    `salt`        varchar(8) COLLATE utf8mb4_unicode_ci                        NOT NULL,
    `user_role`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `create_time` datetime                                                     NOT NULL,
    `update_time` datetime                                                     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `udx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `myosotis_user`
(`username`, `password`, `salt`, `user_role`, `create_time`, `update_time`)
VALUES ('myosotis', '2da2ef29d7037902ba0ce9f36346ab55', 'abcd1234', 'superuser', '1970-01-01 08:00:00',
        '1970-01-01 08:00:00');