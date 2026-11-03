package com.ssm.controller;

import com.ssm.common.BusinessException;
import com.ssm.common.Result;
import com.ssm.entity.Student;
import com.ssm.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图形界面（webapp/index.html）使用的 RESTful 接口，挂在 ssm 子容器下：
 *
 *   GET    /ssm/api/students?name=&major=&page=&size=   多条件模糊 + 分页
 *   GET    /ssm/api/students/{id}                       单条查询
 *   POST   /ssm/api/students                            新增
 *   PUT    /ssm/api/students                            修改
 *   DELETE /ssm/api/students/{id}                       单条删除
 *   DELETE /ssm/api/students         body: [1,2,3]     批量删除（一个事务）
 *   POST   /ssm/api/students/tx-demo                    事务回滚演示
 *
 * 原有的 StudentController（JSP 版）保持不变，两者共用同一个 StudentService。
 */
@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudentService studentService;

    @Autowired
    public StudentApiController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public Result<Map<String, Object>> page(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String major,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "5") int size) {
        if (page < 1) {
            throw new BusinessException(400, "页码必须从 1 开始");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(400, "每页条数必须在 1~" + MAX_PAGE_SIZE + " 之间");
        }
        long total = studentService.count(name, major);
        Map<String, Object> data = new HashMap<>();
        data.put("records", studentService.search(name, major, page, size));
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("pages", (total + size - 1) / size);
        return Result.ok(data);
    }

    @GetMapping("/{id}")
    public Result<Student> detail(@PathVariable Integer id) {
        return Result.ok(requireExisting(id));
    }

    @PostMapping
    public Result<Student> add(@RequestBody Student student) {
        student.setId(null);
        validate(student);
        studentService.addStudent(student);
        return Result.ok(student);
    }

    @PutMapping
    public Result<Student> update(@RequestBody Student student) {
        if (student.getId() == null) {
            throw new BusinessException(400, "修改时 id 不能为空");
        }
        validate(student);
        if (!studentService.updateStudent(student)) {
            throw new BusinessException(404, "学生不存在：id=" + student.getId());
        }
        return Result.ok(student);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        requireExisting(id);
        studentService.deleteById(id);
        return Result.ok(null);
    }

    @DeleteMapping
    public Result<Integer> deleteBatch(@RequestBody(required = false) List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请至少选择一条记录");
        }
        return Result.ok(studentService.deleteByIds(ids));
    }

    /** 调用 addThenFail（插入后抛异常），返回调用前后的总数，用于确认事务已回滚 */
    @PostMapping("/tx-demo")
    public Result<Map<String, Object>> txDemo() {
        long before = studentService.count(null, null);
        String error = null;
        try {
            Student s = new Student();
            s.setName("事务测试");
            s.setAge(20);
            s.setMajor("TX");
            s.setScore(0.0);
            studentService.addThenFail(s);
        } catch (RuntimeException e) {
            error = e.getMessage();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("before", before);
        data.put("after", studentService.count(null, null));
        data.put("exception", error);
        return Result.ok(data);
    }

    private Student requireExisting(Integer id) {
        Student student = studentService.findById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在：id=" + id);
        }
        return student;
    }

    /** 服务端校验（与表结构 student.sql 的约束保持一致） */
    private void validate(Student s) {
        if (s.getName() == null || s.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (s.getName().length() > 50) {
            throw new BusinessException(400, "姓名不能超过 50 个字符");
        }
        if (s.getAge() != null && (s.getAge() < 1 || s.getAge() > 150)) {
            throw new BusinessException(400, "年龄必须在 1~150 之间");
        }
        if (s.getScore() != null && (s.getScore() < 0 || s.getScore() > 100)) {
            throw new BusinessException(400, "成绩必须在 0~100 之间");
        }
        if (s.getEmail() != null && !s.getEmail().isEmpty() && !s.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (s.getMajor() != null && s.getMajor().length() > 50) {
            throw new BusinessException(400, "专业不能超过 50 个字符");
        }
    }
}
