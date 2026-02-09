package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ProjectCreateDTO;
import com.example.backend.dto.ProjectQueryDTO;
import com.example.backend.dto.ProjectUpdateDTO;
import com.example.backend.entity.Project;
import com.example.backend.vo.PageVO;
import com.example.backend.vo.ProjectVO;

public interface ProjectService extends IService<Project> {

    Long create(ProjectCreateDTO dto);

    ProjectVO getById(Long id);

    PageVO<ProjectVO> pageList(ProjectQueryDTO dto);

    void update(Long id, ProjectUpdateDTO dto);

    void delete(Long id);
}
