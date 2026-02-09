package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.converter.ProjectConverter;
import com.example.backend.dto.ProjectCreateDTO;
import com.example.backend.dto.ProjectQueryDTO;
import com.example.backend.dto.ProjectUpdateDTO;
import com.example.backend.entity.Project;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ProjectNotFoundException;
import com.example.backend.mapper.ProjectMapper;
import com.example.backend.service.ProjectService;
import com.example.backend.vo.PageVO;
import com.example.backend.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectConverter projectConverter;

    public ProjectServiceImpl(ProjectConverter projectConverter) {
        this.projectConverter = projectConverter;
    }

    @Override
    public Long create(ProjectCreateDTO dto) {
        Project project = projectConverter.toEntity(dto);
        project.setStatus(dto.getStatus());
        save(project);
        return project.getId();
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = getOneNotDeleted(id);
        return projectConverter.toVO(project);
    }

    @Override
    public PageVO<ProjectVO> pageList(ProjectQueryDTO dto) {
        Page<Project> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Project> wrapper = buildQueryWrapper(dto);
        IPage<Project> resultPage = page(page, wrapper);

        List<ProjectVO> voList = resultPage.getRecords().stream()
                .map(projectConverter::toVO)
                .collect(Collectors.toList());

        return PageVO.of(voList, resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
    }

    @Override
    public void update(Long id, ProjectUpdateDTO dto) {
        Project project = getOneNotDeleted(id);
        projectConverter.updateEntity(project, dto);
        updateById(project);
    }

    @Override
    public void delete(Long id) {
        Project project = getOneNotDeleted(id);
        project.setDeleted(1);
        project.setUpdatedAt(LocalDateTime.now());
        updateById(project);
    }

    private LambdaQueryWrapper<Project> buildQueryWrapper(ProjectQueryDTO dto) {
        LambdaQueryWrapper<Project> wrapper = Wrappers.<Project>lambdaQuery()
                .eq(Project::getDeleted, 0);
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Project::getName, dto.getKeyword())
                    .or()
                    .like(Project::getOwner, dto.getKeyword()));
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        return wrapper;
    }

    private Project getOneNotDeleted(Long id) {
        Project project = getOne(Wrappers.<Project>lambdaQuery()
                .eq(Project::getId, id)
                .eq(Project::getDeleted, 0), false);
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }
}
