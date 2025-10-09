package com.nailora.service;

import com.nailora.dto.AddOnDTO;
import com.nailora.dto.AddOnRequest;
import com.nailora.entity.AddOn;
import com.nailora.exception.BadRequestException;
import com.nailora.exception.NotFoundException;
import com.nailora.repository.AddOnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddOnService {

    private final AddOnRepository addOnRepository;

    // ดึง AddOn ทั้งหมด
    public List<AddOnDTO> findAll() {
        return addOnRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ดึง AddOn ตาม id
    public AddOnDTO findOne(Long id) {
        var entity = addOnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AddOn not found: " + id));
        return toDTO(entity);
    }

    // สร้าง AddOn ใหม่
    @Transactional
    public AddOnDTO create(AddOnRequest req) {
        if (addOnRepository.existsByNameIgnoreCase(req.name())) {
            throw new BadRequestException("AddOn name already exists");
        }
        var entity = new AddOn();
        entity.setName(req.name());
        entity.setExtraMinutes(req.extraMinutes());
        entity.setExtraPrice(req.extraPrice());
        entity.setActive(req.active() == null ? true : req.active());
        addOnRepository.save(entity);
        return toDTO(entity);
    }

    // อัปเดต AddOn เดิม
    @Transactional
    public AddOnDTO update(Long id, AddOnRequest req) {
        var entity = addOnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AddOn not found: " + id));

        // กันชื่อซ้ำกับตัวอื่น
        if (!entity.getName().equalsIgnoreCase(req.name())
                && addOnRepository.existsByNameIgnoreCase(req.name())) {
            throw new BadRequestException("AddOn name already exists");
        }

        entity.setName(req.name());
        entity.setExtraMinutes(req.extraMinutes());
        entity.setExtraPrice(req.extraPrice());
        if (req.active() != null) {
            entity.setActive(req.active());
        }
        return toDTO(entity);
    }

    // ลบ AddOn
    @Transactional
    public void delete(Long id) {
        if (!addOnRepository.existsById(id)) {
            throw new NotFoundException("AddOn not found: " + id);
        }
        addOnRepository.deleteById(id);
    }

    // เปิด/ปิดการใช้งาน AddOn
    @Transactional
    public AddOnDTO setActive(Long id, boolean active) {
        var entity = addOnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AddOn not found: " + id));
        entity.setActive(active);
        return toDTO(entity);
    }

    // แปลง Entity → DTO
    private AddOnDTO toDTO(AddOn a) {
        return new AddOnDTO(
                a.getId(),
                a.getName(),
                a.getExtraMinutes(),
                a.getExtraPrice(),
                a.getActive() != null && a.getActive()
        );
    }
}
