package dev.spangler.student;

import dev.spangler.cache.RedisService;
import io.quarkus.panache.common.Page;
import io.vertx.core.json.Json;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentMapper studentMapper;

    @Inject
    RedisService redisService;

    private static final String STUDENT_KEY_PREFIX = "student:";
    private static final long DURATION_IN_SECONDS = 600;

    public Student findStudentById(Long id) {
        String key = STUDENT_KEY_PREFIX + id;
        String cached = redisService.getValue(key);

        if (cached != null) {
            return Json.decodeValue(cached, Student.class);
        }

        Student student = studentRepository.findById(id);

        if (student == null) {
            throw new NotFoundException("Student not found with ID - " + id);
        }

        redisService.setValueWithTTL(key, Json.encode(student), DURATION_IN_SECONDS);

        return student;
    }

    public List<Student> fetchAllStudents(int page, int size) {
        String key = "students:page:" + page + ":size:" + size;
        List<Student> cachedList = redisService.getListValue(key, Student.class);

        if (cachedList != null) {
            return cachedList;
        }

        List<Student> students = studentRepository.find("ORDER BY id")
                .page(Page.of(page, size))
                .list();

        redisService.setListValueWithTTL(key, students, DURATION_IN_SECONDS);

        return students;
    }


    @Transactional
    public Student createStudent(StudentDto studentDto) {
        Student student = studentMapper.toEntity(studentDto);
        studentRepository.persist(student);

        String key = STUDENT_KEY_PREFIX + student.id;
        redisService.setValueWithTTL(key, Json.encode(student), DURATION_IN_SECONDS);
        redisService.invalidatePaginatedStudentCache();

        return student;
    }

    @Transactional
    public Student updateStudent(Long id, StudentDto studentDto) {
        Student student = findStudentById(id);
        studentMapper.updateEntityFromDto(studentDto, student);

        String key = STUDENT_KEY_PREFIX + id;
        redisService.setValueWithTTL(key, Json.encode(student), DURATION_IN_SECONDS);
        redisService.invalidatePaginatedStudentCache();

        return student;
    }

    @Transactional
    public void deleteStudent(Long id) {
        boolean deleted = studentRepository.deleteById(id);

        if (!deleted) {
            throw new NotFoundException("Student not found with ID - " + id);
        }

        String key = STUDENT_KEY_PREFIX + id;
        redisService.deleteKey(key);
        redisService.invalidatePaginatedStudentCache();
    }
}
