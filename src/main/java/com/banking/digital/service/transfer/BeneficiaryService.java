package com.banking.digital.service.transfer;

import com.banking.digital.model.Beneficiary;
import com.banking.digital.model.User;
import com.banking.digital.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public List<Beneficiary> getBeneficiariesByUser(Long userId) {
        return beneficiaryRepository.findByUserId(userId);
    }

    @Transactional
    public Beneficiary addBeneficiary(User user, Beneficiary beneficiary) {
        beneficiary.setUser(user);
        return beneficiaryRepository.save(beneficiary);
    }

    @Transactional
    public void deleteBeneficiary(Long id, Long userId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        if (!beneficiary.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access");
        }
        beneficiaryRepository.delete(beneficiary);
    }
}
