package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterJobRecruiterRepository extends JpaRepository<RegisterRecruiter,Long>
{

    RegisterRecruiter findByUsername(String username);
}