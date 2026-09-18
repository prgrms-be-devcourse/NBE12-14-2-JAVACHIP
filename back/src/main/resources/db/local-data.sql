-- 로컬 개발용 초기 데이터
-- 공통 비밀번호: 12341234
-- 로그인 계정: user1@example.com ~ user21@example.com

SET @should_seed = (
    SELECT (SELECT COUNT(*) FROM `user`) = 0
        AND (SELECT COUNT(*) FROM room) = 0
        AND (SELECT COUNT(*) FROM user_room_connection) = 0
        AND (SELECT COUNT(*) FROM budget_request) = 0
        AND (SELECT COUNT(*) FROM budget_change) = 0
        AND (SELECT COUNT(*) FROM invite) = 0
);

INSERT INTO `user` (id, email, password, name, created_at, modified_at)
SELECT seed.id, seed.email, seed.password, seed.name, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM (
    SELECT 1 AS id, 'user1@example.com' AS email, '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu' AS password, '테스트 사용자 1' AS name
    UNION ALL SELECT 2, 'user2@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 2'
    UNION ALL SELECT 3, 'user3@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 3'
    UNION ALL SELECT 4, 'user4@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 4'
    UNION ALL SELECT 5, 'user5@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 5'
    UNION ALL SELECT 6, 'user6@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 6'
    UNION ALL SELECT 7, 'user7@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 7'
    UNION ALL SELECT 8, 'user8@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 8'
    UNION ALL SELECT 9, 'user9@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 9'
    UNION ALL SELECT 10, 'user10@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 10'
    UNION ALL SELECT 11, 'user11@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 11'
    UNION ALL SELECT 12, 'user12@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 12'
    UNION ALL SELECT 13, 'user13@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 13'
    UNION ALL SELECT 14, 'user14@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 14'
    UNION ALL SELECT 15, 'user15@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 15'
    UNION ALL SELECT 16, 'user16@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 16'
    UNION ALL SELECT 17, 'user17@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 17'
    UNION ALL SELECT 18, 'user18@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 18'
    UNION ALL SELECT 19, 'user19@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 19'
    UNION ALL SELECT 20, 'user20@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 20'
    UNION ALL SELECT 21, 'user21@example.com', '$2y$10$Ke7jb5Y2zd2UXfOPtabEZeu48u002Mvv9fubZJZeSzDGsf.6jFqWu', '테스트 사용자 21'
) AS seed
WHERE @should_seed = 1;

INSERT INTO room (id, name, total_budget, available_budget, currency, created_at)
SELECT seed.id, seed.name, seed.total_budget, seed.available_budget, seed.currency, CURRENT_TIMESTAMP(6)
FROM (
    SELECT 1 AS id, '프로젝트 회식' AS name, 500000 AS total_budget, 500000 AS available_budget, 'KRW' AS currency
    UNION ALL SELECT 2, '일본 여행', 100000, 100000, 'JPY'
    UNION ALL SELECT 3, '미국 여행', 10000, 10000, 'USD'
) AS seed
WHERE @should_seed = 1;

INSERT INTO user_room_connection (user_id, room_id, authority, joined, created_at)
SELECT seed.user_id, seed.room_id, seed.authority, b'1', CURRENT_TIMESTAMP(6)
FROM (
    SELECT 1 AS user_id, 1 AS room_id, 'OWNER' AS authority
    UNION ALL SELECT 2, 1, 'MEMBER'
    UNION ALL SELECT 3, 1, 'MEMBER'
    UNION ALL SELECT 4, 1, 'MEMBER'
    UNION ALL SELECT 5, 1, 'MEMBER'
    UNION ALL SELECT 6, 1, 'MEMBER'
    UNION ALL SELECT 7, 1, 'MEMBER'
    UNION ALL SELECT 8, 1, 'MEMBER'
    UNION ALL SELECT 9, 1, 'MEMBER'
    UNION ALL SELECT 10, 1, 'MEMBER'
    UNION ALL SELECT 11, 2, 'OWNER'
    UNION ALL SELECT 12, 2, 'MEMBER'
    UNION ALL SELECT 13, 2, 'MEMBER'
    UNION ALL SELECT 14, 2, 'MEMBER'
    UNION ALL SELECT 15, 2, 'MEMBER'
    UNION ALL SELECT 16, 3, 'OWNER'
    UNION ALL SELECT 17, 3, 'MEMBER'
    UNION ALL SELECT 18, 3, 'MEMBER'
) AS seed
WHERE @should_seed = 1;
