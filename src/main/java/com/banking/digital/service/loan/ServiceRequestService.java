package com.banking.digital.service.loan;

import com.banking.digital.model.Account;
import com.banking.digital.model.ServiceRequest;
import com.banking.digital.model.User;
import com.banking.digital.model.enums.ServiceRequestType;
import com.banking.digital.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Transactional
    public ServiceRequest createRequest(User user, Account account,
                                        ServiceRequestType type, String remarks) {
        ServiceRequest request = new ServiceRequest();
        request.setUser(user);
        request.setAccount(account);
        request.setRequestType(type);
        request.setRemarks(remarks);
        return serviceRequestRepository.save(request);
    }

    public List<ServiceRequest> getRequestsByUser(Long userId) {
        return serviceRequestRepository.findByUserId(userId);
    }
}
