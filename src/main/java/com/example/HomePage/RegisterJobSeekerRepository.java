package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterJobSeekerRepository extends JpaRepository<RegisterJobSeeker, Long>{
    boolean existsByUsername(String username);
    RegisterJobSeeker findByUsername(String username);// Correct method signature
}
