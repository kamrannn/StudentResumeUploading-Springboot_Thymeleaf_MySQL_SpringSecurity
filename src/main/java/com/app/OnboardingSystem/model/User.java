package com.app.OnboardingSystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Address address;
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String dob;
    private String gender;
    @Column(unique = true)
    @NotBlank
    private String phoneNumber;
    @Column(unique = true)
    @NotBlank(message = "email is mandatory")
    private String email;

    @OneToMany(targetEntity = WorkExperience.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<WorkExperience> workExperienceList = new ArrayList<>();

    @OneToMany(targetEntity = EducationalDetail.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<EducationalDetail> educationalDetails = new ArrayList<>();

    @OneToMany(targetEntity = File.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<File> fileList = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
