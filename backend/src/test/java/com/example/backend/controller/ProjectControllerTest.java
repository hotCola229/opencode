package com.example.backend.controller;

import com.example.backend.entity.Project;
import com.example.backend.mapper.ProjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @BeforeEach
    public void setUp() {
        projectMapper.delete(null);
        projectMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 1));
    }

    @Test
    public void testCreateProjectSuccess() throws Exception {
        String requestBody = "{\"name\":\"Test Project\",\"owner\":\"John\",\"status\":1}";

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data", instanceOf(Number.class)));
    }

    @Test
    public void testCreateProjectValidationFail() throws Exception {
        String requestBody = "{\"name\":\"Test Project\",\"owner\":\"John\",\"status\":9}";

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value(notNullValue()));
    }

    @Test
    public void testGetNotFoundProject() throws Exception {
        mockMvc.perform(get("/api/projects/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    @Test
    public void testCreateAndGetProject() throws Exception {
        String createRequest = "{\"name\":\"My Project\",\"owner\":\"Jane\",\"status\":0}";
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        Long projectId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.name").value("My Project"))
                .andExpect(jsonPath("$.data.owner").value("Jane"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    public void testListProjects() throws Exception {
        String createRequest = "{\"name\":\"List Test Project\",\"owner\":\"Bob\",\"status\":1}";
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    public void testUpdateProject() throws Exception {
        String createRequest = "{\"name\":\"Update Test\",\"owner\":\"Alice\",\"status\":0}";
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").asLong();

        String updateRequest = "{\"name\":\"Updated Name\",\"owner\":\"Bob\",\"status\":2}";
        mockMvc.perform(put("/api/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @Rollback(false)
    public void testDeleteProject() throws Exception {
        String createRequest = "{\"name\":\"Delete Test\",\"owner\":\"Charlie\",\"status\":1}";
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(delete("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(projectId.intValue()));
    }

    @Test
    @Rollback(false)
    public void testLogicalDeleteAndQuery() throws Exception {
        String createRequest = "{\"name\":\"Logical Delete Test\",\"owner\":\"David\",\"status\":1}";
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andReturn();

        Long projectId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").asLong();

        mockMvc.perform(delete("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(projectId.intValue()));
    }

    @Test
    public void testCreateWithInvalidStatus() throws Exception {
        String requestBody = "{\"name\":\"Test\",\"owner\":\"Test\",\"status\":99}";

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
