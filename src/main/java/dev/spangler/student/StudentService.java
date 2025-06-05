package dev.spangler.student;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentMapper studentMapper;

    public Student findStudentById(Long id) {
        Student student = studentRepository.findById(id);

        if (student == null) {
            throw new NotFoundException("Student not found with ID - " + id);
        }

        return student;
    }

    @Transactional
    public Student createStudent(StudentDto studentDto) {
        Student student = studentMapper.toEntity(studentDto);
        studentRepository.persist(student);

        return student;
    }

    @Transactional
    public Student updateStudent(Long id, StudentDto studentDto) {
        Student student = findStudentById(id);
        studentMapper.updateEntityFromDto(studentDto, student);

        return student;
    }

    @Transactional
    public void deleteStudent(Long id) {
        boolean deleted = studentRepository.deleteById(id);

        if (!deleted) {
            throw new NotFoundException("Student not found with ID - " + id);
        }
    }
}
