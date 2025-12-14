package com.app.easypharma_backend.domain.pharmacy.mapper;

import com.app.easypharma_backend.domain.pharmacy.dto.PharmacyDTO;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.entity.PharmacyStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-14T10:31:58+0100",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class PharmacyMapperImpl implements PharmacyMapper {

    @Override
    public PharmacyDTO toDTO(Pharmacy pharmacy) {
        if ( pharmacy == null ) {
            return null;
        }

        PharmacyDTO pharmacyDTO = new PharmacyDTO();

        pharmacyDTO.setId( pharmacy.getId() );
        pharmacyDTO.setName( pharmacy.getName() );
        pharmacyDTO.setLicenseNumber( pharmacy.getLicenseNumber() );
        pharmacyDTO.setAddress( pharmacy.getAddress() );
        pharmacyDTO.setCity( pharmacy.getCity() );
        pharmacyDTO.setPhone( pharmacy.getPhone() );
        pharmacyDTO.setLatitude( pharmacy.getLatitude() );
        pharmacyDTO.setLongitude( pharmacy.getLongitude() );
        pharmacyDTO.setDescription( pharmacy.getDescription() );
        pharmacyDTO.setOpeningHours( pharmacy.getOpeningHours() );
        pharmacyDTO.setStatus( pharmacy.getStatus() );
        pharmacyDTO.setLicenseDocumentUrl( pharmacy.getLicenseDocumentUrl() );
        pharmacyDTO.setValidatedAt( pharmacy.getValidatedAt() );
        pharmacyDTO.setCreatedAt( pharmacy.getCreatedAt() );
        pharmacyDTO.setUpdatedAt( pharmacy.getUpdatedAt() );

        pharmacyDTO.setUserId( pharmacy.getUser() != null ? pharmacy.getUser().getId() : null );

        return pharmacyDTO;
    }

    @Override
    public List<PharmacyDTO> toDTOList(List<Pharmacy> pharmacies) {
        if ( pharmacies == null ) {
            return null;
        }

        List<PharmacyDTO> list = new ArrayList<PharmacyDTO>( pharmacies.size() );
        for ( Pharmacy pharmacy : pharmacies ) {
            list.add( toDTO( pharmacy ) );
        }

        return list;
    }

    @Override
    public Pharmacy toEntity(PharmacyDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Pharmacy.PharmacyBuilder pharmacy = Pharmacy.builder();

        pharmacy.name( dto.getName() );
        pharmacy.licenseNumber( dto.getLicenseNumber() );
        pharmacy.address( dto.getAddress() );
        pharmacy.city( dto.getCity() );
        pharmacy.phone( dto.getPhone() );
        pharmacy.latitude( dto.getLatitude() );
        pharmacy.longitude( dto.getLongitude() );
        pharmacy.description( dto.getDescription() );
        pharmacy.openingHours( dto.getOpeningHours() );
        pharmacy.licenseDocumentUrl( dto.getLicenseDocumentUrl() );

        pharmacy.status( PharmacyStatus.PENDING );

        return pharmacy.build();
    }

    @Override
    public void updateEntityFromDTO(PharmacyDTO dto, Pharmacy pharmacy) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getName() != null ) {
            pharmacy.setName( dto.getName() );
        }
        if ( dto.getLicenseNumber() != null ) {
            pharmacy.setLicenseNumber( dto.getLicenseNumber() );
        }
        if ( dto.getAddress() != null ) {
            pharmacy.setAddress( dto.getAddress() );
        }
        if ( dto.getCity() != null ) {
            pharmacy.setCity( dto.getCity() );
        }
        if ( dto.getPhone() != null ) {
            pharmacy.setPhone( dto.getPhone() );
        }
        if ( dto.getLatitude() != null ) {
            pharmacy.setLatitude( dto.getLatitude() );
        }
        if ( dto.getLongitude() != null ) {
            pharmacy.setLongitude( dto.getLongitude() );
        }
        if ( dto.getDescription() != null ) {
            pharmacy.setDescription( dto.getDescription() );
        }
        if ( dto.getOpeningHours() != null ) {
            pharmacy.setOpeningHours( dto.getOpeningHours() );
        }
        if ( dto.getLicenseDocumentUrl() != null ) {
            pharmacy.setLicenseDocumentUrl( dto.getLicenseDocumentUrl() );
        }
    }
}
