package com.shimpimilan.service.melava;

import com.shimpimilan.model.melava.Melava;
import com.shimpimilan.model.melava.MelavaStatus;
import com.shimpimilan.repository.melava.MelavaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MelavaService {

    private final MelavaRepository melavaRepository;

    public List<Melava> getAllMelavas() {
        return melavaRepository.findAll();
    }

    public List<Melava> getActiveMelavas() {
        return melavaRepository.findByStatus(MelavaStatus.UPCOMING);
    }

    public Optional<Melava> getMelavaById(Long id) {
        return melavaRepository.findById(id);
    }

    @Transactional
    public Melava createMelava(Melava melava) {
        return melavaRepository.save(melava);
    }

    @Transactional
    public Melava updateMelava(Long id, Melava melavaDetails) {
        return melavaRepository.findById(id).map(melava -> {
            melava.setMelavaName(melavaDetails.getMelavaName());
            melava.setBannerUrl(melavaDetails.getBannerUrl());
            melava.setLogoUrl(melavaDetails.getLogoUrl());
            melava.setDescription(melavaDetails.getDescription());
            melava.setStartDate(melavaDetails.getStartDate());
            melava.setEndDate(melavaDetails.getEndDate());
            melava.setStartTime(melavaDetails.getStartTime());
            melava.setEndTime(melavaDetails.getEndTime());
            melava.setRegistrationStartDate(melavaDetails.getRegistrationStartDate());
            melava.setRegistrationEndDate(melavaDetails.getRegistrationEndDate());
            melava.setVenueName(melavaDetails.getVenueName());
            melava.setAddress(melavaDetails.getAddress());
            melava.setGoogleMapLocation(melavaDetails.getGoogleMapLocation());
            melava.setCity(melavaDetails.getCity());
            melava.setState(melavaDetails.getState());
            melava.setPincode(melavaDetails.getPincode());
            melava.setOrganizerName(melavaDetails.getOrganizerName());
            melava.setContactPerson(melavaDetails.getContactPerson());
            melava.setMobileNumber(melavaDetails.getMobileNumber());
            melava.setEmail(melavaDetails.getEmail());
            melava.setWebsite(melavaDetails.getWebsite());
            melava.setRegistrationFee(melavaDetails.getRegistrationFee());
            melava.setCoupleEntryFee(melavaDetails.getCoupleEntryFee());
            melava.setVisitorFee(melavaDetails.getVisitorFee());
            melava.setMaximumRegistrations(melavaDetails.getMaximumRegistrations());
            melava.setStatus(melavaDetails.getStatus());
            melava.setBrochurePdfUrl(melavaDetails.getBrochurePdfUrl());
            return melavaRepository.save(melava);
        }).orElseThrow(() -> new RuntimeException("Melava not found with id " + id));
    }

    @Transactional
    public void deleteMelava(Long id) {
        melavaRepository.deleteById(id);
    }
}
