package dev.spangler.student;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

@Entity
@Table(name = "student", uniqueConstraints = {
        @UniqueConstraint(name = "uc_student_email", columnNames = "email"),
        @UniqueConstraint(name = "uc_student_mobile", columnNames = "mobile")
})
public class Student extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Email
    @Column(unique = true, nullable = false)
    public String email;

    @Column(unique = true)
    public String mobile;
}
