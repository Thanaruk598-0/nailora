package com.nailora.service;

import com.nailora.entity.ServiceItem;
import com.nailora.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemRepository repository;

    public List<ServiceItem> getAllServices() {
        return repository.findAll();
    }

    public ServiceItem getServiceById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id " + id));
    }

    public ServiceItem createService(ServiceItem serviceItem) {
        return repository.save(serviceItem);
    }

    public ServiceItem updateService(Long id, ServiceItem newService) {
        var existing = getServiceById(id);

        existing.setName(newService.getName());
        existing.setDurationMin(newService.getDurationMin());
        existing.setPrice(newService.getPrice());
        existing.setDepositMin(newService.getDepositMin());
        existing.setActive(newService.getActive());

        return repository.save(existing);
    }

    public void deleteService(Long id) {
        repository.deleteById(id);
    }
}
