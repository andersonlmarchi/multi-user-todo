package br.com.jtech.tasklist.api;

import br.com.jtech.tasklist.StartTasklist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StartTasklist.class)
@AutoConfigureMockMvc
@Testcontainers
class TaskBoardApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> true);
        r.add("app.jwt.secret", () -> "integration-test-jwt-secret-32-bytes!");
        r.add("app.jwt.access-token-minutes", () -> 15);
        r.add("app.jwt.refresh-token-days", () -> 7);
        r.add("app.jwt.issuer", () -> "test-issuer");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registerLoginCreateListAndTask_flow() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@ex.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterBody("Alice", email, "password12"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, "password12"))))
                .andExpect(status().isOk())
                .andReturn();

        String access = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();

        MvcResult listRes = mockMvc.perform(post("/api/v1/task-lists")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Work\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"))
                .andReturn();

        String listId = objectMapper.readTree(listRes.getResponse().getContentAsString()).get("id").asText();

        MvcResult taskRes = mockMvc.perform(post("/api/v1/task-lists/{id}/tasks", listId)
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Fix bug\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andReturn();

        String taskId = objectMapper.readTree(taskRes.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/task-lists/{id}/tasks", listId)
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/task-lists/{id}/tasks/{tid}", listId, taskId)
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"done\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));

        mockMvc.perform(post("/api/v1/task-lists/{id}/archive", listId)
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        JsonNode lists = objectMapper.readTree(mockMvc.perform(get("/api/v1/task-lists")
                        .param("archived", "true")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(lists).hasSize(1);
    }

    private record RegisterBody(String name, String email, String password) {
    }

    private record LoginBody(String email, String password) {
    }
}
