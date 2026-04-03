package com.Gig.Guide.GigGuide.Mapper;

import com.Gig.Guide.GigGuide.DTO.Club.AddressDTO;
import com.Gig.Guide.GigGuide.Models.Club.Address;
import org.springframework.stereotype.Component;

@Component
public class ClubAddressMapper {

    public static Address addressToEntity(AddressDTO addressDTO) {
        Address address = new Address();
        address.setLocation(addressDTO.getLocation());
        address.setProvince(address.getProvince());
        address.setCity(address.getCity());
        address.setPostalCode(address.getPostalCode());
        address.setCountry(address.getCountry());

        return address;
    }

    public static AddressDTO addressDtoToDto(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(address.getCountry());
        addressDTO.setLocation(address.getLocation());
        addressDTO.setProvince(address.getProvince());
        addressDTO.setCity(address.getCity());
        addressDTO.setPostalCode(address.getPostalCode());

        return addressDTO;
    }


}
