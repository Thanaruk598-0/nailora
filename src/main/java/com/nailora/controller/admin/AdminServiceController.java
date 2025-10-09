package com.nailora.controller.admin;

import com.nailora.entity.ServiceItem;
import com.nailora.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ServiceItemService service;

    @GetMapping
    public List<ServiceItem> getAllServices() {
        return service.getAllServices();
    }

    @GetMapping("/{id}")
    public ServiceItem getService(@PathVariable Long id) {
        return service.getServiceById(id);
    }

    @PostMapping
    public ServiceItem createService(@RequestBody ServiceItem serviceItem) {
        return service.createService(serviceItem);
    }

    @PutMapping("/{id}")
    public ServiceItem updateService(@PathVariable Long id, @RequestBody ServiceItem serviceItem) {
        return service.updateService(id, serviceItem);
    }

    @DeleteMapping("/{id}")
    public void deleteService(@PathVariable Long id) {
        service.deleteService(id);
    }
}
