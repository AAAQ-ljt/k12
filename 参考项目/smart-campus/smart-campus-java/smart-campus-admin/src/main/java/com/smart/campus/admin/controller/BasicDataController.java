package com.smart.campus.admin.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.enums.EducationalSystemTypeEnum;
import com.smart.campus.entity.enums.GenderEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.enums.TeacherTitleEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.DepartmentInfoQuery;
import com.smart.campus.entity.query.MajorInfoQuery;
import com.smart.campus.entity.vo.BasicDataOptionsVO;
import com.smart.campus.entity.vo.OptionVO;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.DepartmentInfoService;
import com.smart.campus.service.MajorInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("basicDataController")
@RequestMapping("/basicData")
public class BasicDataController extends ABaseController {

    @Resource
    private DepartmentInfoService departmentInfoService;

    @Resource
    private MajorInfoService majorInfoService;

    @Resource
    private ClassInfoService classInfoService;

    @RequestMapping("/getOptions")
    public ResponseVO getOptions() {
        BasicDataOptionsVO result = new BasicDataOptionsVO();
        result.setStatusTextMap(StatusEnum.toMap());
        result.setGenderTextMap(GenderEnum.toMap());
        result.setDepartmentOptions(buildDepartmentOptions());
        result.setMajorOptions(buildMajorOptions());
        result.setClassOptions(buildClassOptions());
        result.setGradeOptions(buildGradeOptions());
        result.setTeacherTitleOptions(buildTeacherTitleOptions());
        return getSuccessResponseVO(result);
    }

    private List<OptionVO<Integer>> buildDepartmentOptions() {
        DepartmentInfoQuery query = new DepartmentInfoQuery();
        query.setOrderBy("d.sort_order asc,d.department_id desc");
        List<DepartmentInfo> list = departmentInfoService.findListByParam(query);
        List<OptionVO<Integer>> options = new ArrayList<>();
        for (DepartmentInfo item : list) {
            options.add(createOption(item.getDepartmentId(), item.getDepartmentName()));
        }
        return options;
    }

    private List<OptionVO<Integer>> buildMajorOptions() {
        MajorInfoQuery query = new MajorInfoQuery();
        query.setOrderBy("m.sort_order asc,m.major_id desc");
        List<MajorInfo> list = majorInfoService.findListByParam(query);
        List<OptionVO<Integer>> options = new ArrayList<>();
        for (MajorInfo item : list) {
            options.add(createOption(item.getMajorId(), item.getMajorName()));
        }
        return options;
    }

    private List<OptionVO<Integer>> buildClassOptions() {
        ClassInfoQuery query = new ClassInfoQuery();
        query.setOrderBy("c.sort_order asc,c.class_id desc");
        List<ClassInfo> list = classInfoService.findListByParam(query);
        List<OptionVO<Integer>> options = new ArrayList<>();
        for (ClassInfo item : list) {
            options.add(createOption(item.getClassId(), item.getClassName()));
        }
        return options;
    }

    private List<OptionVO<Integer>> buildGradeOptions() {
        List<OptionVO<Integer>> options = new ArrayList<>();
        for (EducationalSystemTypeEnum item : EducationalSystemTypeEnum.getAll()) {
            options.add(createOption(item.getCode(), item.getDesc()));
        }
        return options;
    }

    private List<OptionVO<String>> buildTeacherTitleOptions() {
        List<OptionVO<String>> options = new ArrayList<>();
        for (TeacherTitleEnum item : TeacherTitleEnum.getAll()) {
            options.add(createOption(item.getCode(), item.getDesc()));
        }
        return options;
    }

    private <T> OptionVO<T> createOption(T value, String label) {
        return new OptionVO<>(value, label);
    }
}
