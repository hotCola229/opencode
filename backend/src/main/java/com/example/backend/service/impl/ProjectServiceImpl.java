package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.ProjectCreateDTO;
import com.example.backend.dto.ProjectQueryDTO;
import com.example.backend.dto.ProjectUpdateDTO;
import com.example.backend.entity.Project;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ProjectNotFoundException;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.service.ProjectService;
import com.example.backend.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public Long create(ProjectCreateDTO dto) {
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setStatus(dto.getStatus());
        project.setDeleted(0);
        save(project);
        return project.getId();
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = getOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, id)
                .eq(Project::getDeleted, 0));
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        return vo;
    }

    @Override
    public IPage<ProjectVO> pageList(ProjectQueryDTO dto) {
        Page<Project> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Project> wrapper = Wrappers.<Project>lambdaQuery()
                .eq(Project::getDeleted, 0);
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Project::getName, dto.getKeyword())
                    .or()
                    .like(Project::getOwner, dto.getKeyword()));
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        IPage<Project> resultPage = page(page, wrapper);
        return resultPage.convert(project -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(project, vo);
            return vo;
        });
    }

    @Override
    public void update(Long id, ProjectUpdateDTO dto) {
        Project project = getOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, id)
                .eq(Project::getDeleted, 0));
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        BeanUtils.copyProperties(dto, project);
        project.setUpdatedAt(LocalDateTime.now());
        updateById(project);
    }

    @Override
    public void delete(Long id) {
        Project project = getOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, id)
                .eq(Project::getDeleted, 0));
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        project.setDeleted(1);
        project.setUpdatedAt(LocalDateTime.now());
        updateById(project);
    }
}
