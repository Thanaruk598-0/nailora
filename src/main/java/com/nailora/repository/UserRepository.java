package com.nailora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nailora.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);
}
