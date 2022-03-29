package com.app.OnboardingSystem.repository;

import com.app.OnboardingSystem.model.EducationalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<EducationalDetail, Integer> {
}
