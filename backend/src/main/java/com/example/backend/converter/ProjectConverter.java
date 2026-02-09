package com.example.backend.converter;

import com.example.backend.dto.ProjectCreateDTO;
import com.example.backend.dto.ProjectUpdateDTO;
import com.example.backend.entity.Project;
import com.example.backend.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProjectConverter {

    public Project toEntity(ProjectCreateDTO dto) {
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setDeleted(0);
        return project;
    }

    public Project toEntity(ProjectUpdateDTO dto, Project entity) {
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public ProjectVO toVO(Project entity) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public void updateEntity(Project entity, ProjectUpdateDTO dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getOwner() != null) {
            entity.setOwner(dto.getOwner());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
