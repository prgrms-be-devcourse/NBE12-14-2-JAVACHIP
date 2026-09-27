package com.budzet.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Budzet API 서버",
        version = "beta",
        description = "Budzet API 서버 문서입니다."))
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SpringDoc {

    @Bean
    public GroupedOpenApi allApi(){
        return GroupedOpenApi.builder()
                .group("00. 전체 API")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi(){
        return GroupedOpenApi.builder()
                .group("01. 회원 & 인증")
                .pathsToMatch("/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roomApi(){
        return GroupedOpenApi.builder()
                .group("02. 모임방 & 맴버")
                .pathsToMatch(
                        "/rooms", "/rooms/{roomId}",
                        "/rooms/{roomId}/members/**"
                        )
                .build();
    }

    @Bean
    public GroupedOpenApi budgetApi(){
        return GroupedOpenApi.builder()
                .group("03. 예산관리 & 예산 신청 & 정산")
                .pathsToMatch("/rooms/{roomId}/budget/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inviteApi(){
        return GroupedOpenApi.builder()
                .group("04. 초대")
                .pathsToMatch(
                        "/invites/**",
                        "/rooms/{roomId}/invites/**",
                        "/rooms/join/**")
                .build();
    }
}
