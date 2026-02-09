package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ProjectCreateDTO;
import com.example.backend.dto.ProjectQueryDTO;
import com.example.backend.dto.ProjectUpdateDTO;
import com.example.backend.service.ProjectService;
import com.example.backend.vo.PageVO;
import com.example.backend.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public Result<Long> create(@RequestBody @Validated ProjectCreateDTO dto) {
        Long id = projectService.create(dto);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getById(@PathVariable Long id) {
        ProjectVO vo = projectService.getById(id);
        return Result.success(vo);
    }

    @GetMapping
    public Result<PageVO<ProjectVO>> pageList(@ModelAttribute ProjectQueryDTO dto) {
        PageVO<ProjectVO> pageVO = projectService.pageList(dto);
        return Result.success(pageVO);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated ProjectUpdateDTO dto) {
        projectService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }
}
