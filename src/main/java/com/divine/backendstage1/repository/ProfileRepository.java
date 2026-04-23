package com.divine.backendstage1.repository;

import com.divine.backendstage1.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID>,
        JpaSpecificationExecutor<Profile> {

    Optional<Profile> findByNameIgnoreCase(String name);

    List<Profile> findByNameIgnoreCaseIn(Collection<String> names);

    List<Profile> findByGenderIgnoreCase(String gender);

    List<Profile> findByCountryIdIgnoreCase(String countryId);

    List<Profile> findByAgeGroupIgnoreCase(String ageGroup);

    List<Profile> findByGenderIgnoreCaseAndCountryIdIgnoreCase(String gender, String countryId);

    List<Profile> findByGenderIgnoreCaseAndAgeGroupIgnoreCase(String gender, String ageGroup);

    List<Profile> findByCountryIdIgnoreCaseAndAgeGroupIgnoreCase(String countryId, String ageGroup);

    List<Profile> findByGenderIgnoreCaseAndCountryIdIgnoreCaseAndAgeGroupIgnoreCase(
            String gender, String countryId, String ageGroup);

//    // Advanced filtering + pagination
//    Page<Profile> findAllByGenderIgnoreCaseAndAgeGroupIgnoreCaseAndCountryIdIgnoreCaseAndAgeBetweenAndGenderProbabilityGreaterThanEqualAndCountryProbabilityGreaterThanEqual(
//            String gender, String ageGroup, String countryId, Integer minAge, Integer maxAge,
//            Double minGenderProb, Double minCountryProb, Pageable pageable);

    // You can add more specific methods if needed, but one powerful method is better
}











