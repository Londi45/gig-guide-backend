package com.Gig.Guide.GigGuide.Repositories;

import com.Gig.Guide.GigGuide.Models.Club.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepo  extends JpaRepository<Address,Long> {
}
