package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Club.Clubs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Clubs, Long> {

    Optional<Clubs> findByName(String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    Page<Clubs> findByActiveTrue(Pageable pageable);

    Optional<Clubs> findByIdAndActiveTrue(Long id);
}
