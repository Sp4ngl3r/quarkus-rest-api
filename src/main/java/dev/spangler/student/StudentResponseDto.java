package dev.spangler.student;

import java.util.List;

public class StudentResponseDto {

    public Long id;
    public String name;
    public String email;
    public String mobile;
    public List<Link> links;

    public static class Link {
        public String rel;
        public String href;
        public String method;

        public Link(String rel, String href, String method) {
            this.rel = rel;
            this.href = href;
            this.method = method;
        }
    }

    public StudentResponseDto() {
    }

    public StudentResponseDto(Long id, String name, String email, String mobile) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public StudentResponseDto(Long id, String name, String email, String mobile, List<Link> links) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.links = links;
    }
}
