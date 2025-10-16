package com.nailora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nailora.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
