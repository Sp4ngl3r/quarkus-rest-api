package dev.spangler.student;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentService studentService;

    @Inject
    StudentMapper studentMapper;

    @GET
    public List<StudentResponseDto> fetchAllStudents(
            @Context UriInfo uriInfo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        return studentService.fetchAllStudents(page, size)
                .stream()
                .map(student -> studentMapper.toResponseDto(student, uriInfo))
                .collect(Collectors.toList());
    }


    @GET
    @Path("/{id}")
    public Response findStudentById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        Student student = studentService.findStudentById(id);
        StudentResponseDto studentResponse = studentMapper.toResponseDto(student, uriInfo);

        return Response.ok(studentResponse).build();
    }

    @POST
    @Transactional
    public Response createStudent(@Valid StudentDto studentDto, @Context UriInfo uriInfo) {
        Student student = studentService.createStudent(studentDto);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(student.id))
                .build();

        StudentResponseDto studentResponseDto = studentMapper.toResponseDto(student, uriInfo);

        return Response.created(location)
                .entity(studentResponseDto)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateStudent(@PathParam("id") Long id, @Valid StudentDto studentDto) {
        Student student = studentService.updateStudent(id, studentDto);

        return Response.ok(student).build();
    }


    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteStudent(@PathParam("id") Long id) {
        studentService.deleteStudent(id);

        return Response.ok().build();
    }
}
