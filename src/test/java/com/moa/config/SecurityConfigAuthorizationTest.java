package com.moa.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.mybatis.spring.SqlSessionTemplate;
import org.apache.ibatis.session.Configuration;

import com.moa.auth.filter.JwtAuthenticationFilter;
import com.moa.auth.handler.JwtAccessDeniedHandler;
import com.moa.auth.handler.JwtAuthenticationEntryPoint;
import com.moa.auth.provider.JwtProvider;

@WebMvcTest(controllers = SecurityConfigAuthorizationTest.TestEndpoints.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class, JwtAccessDeniedHandler.class,
		JwtAuthenticationEntryPoint.class, SecurityConfigAuthorizationTest.MyBatisTestConfig.class,
		SecurityConfigAuthorizationTest.TestEndpoints.class })
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost",
        "mybatis.lazy-initialization=true"
})
class SecurityConfigAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void publicNoticeReadRemainsPublic() throws Exception {
        mockMvc.perform(get("/api/community/notice")).andExpect(status().isOk());
    }

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void normalUserCannotWriteNoticeProductOrPush() throws Exception {
        mockMvc.perform(post("/api/community/notice")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/product")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/push")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanWriteNoticeProductAndPush() throws Exception {
        mockMvc.perform(post("/api/community/notice")).andExpect(status().isOk());
        mockMvc.perform(post("/api/product")).andExpect(status().isOk());
        mockMvc.perform(post("/api/push")).andExpect(status().isOk());
    }

    @Test
    void inquiryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/community/inquiry/my")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void authenticatedUserCanReadOwnInquiryRoute() throws Exception {
        mockMvc.perform(get("/api/community/inquiry/my")).andExpect(status().isOk());
    }

    @RestController
    static class TestEndpoints {
        @GetMapping("/api/community/notice")
        void notice() {}

        @GetMapping("/actuator/health")
        void health() {}

        @PostMapping("/api/community/notice")
        void createNotice() {}

        @PostMapping("/api/product")
        void createProduct() {}

        @PostMapping("/api/push")
        void createPush() {}

        @GetMapping("/api/community/inquiry/my")
        void ownInquiry() {}
    }

	@TestConfiguration
	static class MyBatisTestConfig {
		@Bean
		SqlSessionTemplate sqlSessionTemplate() {
			SqlSessionTemplate template = mock(SqlSessionTemplate.class);
			Configuration configuration = new Configuration();
			when(template.getConfiguration()).thenReturn(configuration);
			when(template.getMapper(any())).thenAnswer(invocation -> {
				Class<?> mapperType = invocation.getArgument(0, Class.class);
				return mock(mapperType);
			});
			return template;
		}
	}
}
