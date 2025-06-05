package dev.spangler.student;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@ApplicationScoped
public class StudentMapper {

    public Student toEntity(StudentDto studentDto) {
        Student student = new Student();
        student.name = studentDto.name;
        student.email = studentDto.email;
        student.mobile = studentDto.mobile;

        return student;
    }

    public void updateEntityFromDto(StudentDto studentDto, Student student) {
        student.name = studentDto.name;
        student.email = studentDto.email;
        student.mobile = studentDto.mobile;
    }

    public StudentResponseDto toResponseDto(Student student) {
        return new StudentResponseDto(
                student.id,
                student.name,
                student.email,
                student.mobile
        );
    }

    public StudentResponseDto toResponseDto(Student student, UriInfo uriInfo) {
        String baseUri = uriInfo.getBaseUriBuilder()
                .path("api/v1/students")
                .path(String.valueOf(student.id))
                .build()
                .toString();

        List<StudentResponseDto.Link> links = List.of(
                new StudentResponseDto.Link("self", baseUri, "GET"),
                new StudentResponseDto.Link("update", baseUri, "PUT"),
                new StudentResponseDto.Link("delete", baseUri, "DELETE")
        );

        return new StudentResponseDto(
                student.id,
                student.name,
                student.email,
                student.mobile,
                links
        );
    }

}
