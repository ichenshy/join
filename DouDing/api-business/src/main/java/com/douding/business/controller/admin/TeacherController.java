package com.douding.business.controller.admin;


import com.douding.server.domain.Teacher;
import com.douding.server.dto.MemberCourseDto;
import com.douding.server.dto.TeacherDto;
import com.douding.server.dto.PageDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.exception.ValidatorException;
import com.douding.server.service.TeacherService;
import com.douding.server.util.ValidatorUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/admin/teacher")
public class TeacherController {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherController.class);
    //给了日志用的
    public static final String BUSINESS_NAME = "讲师";

    @Resource
    private TeacherService teacherService;

    @RequestMapping("/list")
    public ResponseDto list(PageDto pageDto) {
        ResponseDto<PageDto> responseDto = new ResponseDto<>();
        teacherService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/save")
    public ResponseDto save(@RequestBody TeacherDto teacherDto) {
        String name = teacherDto.getName();
        if (StringUtils.isEmpty(name)) {
            throw new ValidatorException("name不能为空");
        }
        String nickname = teacherDto.getNickname();
        if (StringUtils.isEmpty(nickname)) {
            throw new ValidatorException("nickname不能为空");
        }
        String image = teacherDto.getImage();
        if (StringUtils.isEmpty(image)) {
            throw new ValidatorException("image不能为空");
        }
        String position = teacherDto.getPosition();
        if (StringUtils.isEmpty(position)) {
            throw new ValidatorException("position不能为空");
        }
        String motto = teacherDto.getMotto();
        if (StringUtils.isEmpty(motto)) {
            throw new ValidatorException("motto不能为空");
        }
        String intro = teacherDto.getIntro();
        if (StringUtils.isEmpty(intro)) {
            throw new ValidatorException("intro不能为空");
        }
        ResponseDto<TeacherDto> responseDto = new ResponseDto<>();
        teacherService.save(teacherDto);
        responseDto.setContent(teacherDto);
        return responseDto;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto<MemberCourseDto> responseDto = new ResponseDto<>();
        teacherService.delete(id);
        return responseDto;
    }

    @RequestMapping("/all")
    public ResponseDto all() {
        ResponseDto<List<TeacherDto>> responseDto = new ResponseDto<>();
        List<TeacherDto> list = teacherService.all();
        responseDto.setContent(list);
        return responseDto;
    }

}//end class