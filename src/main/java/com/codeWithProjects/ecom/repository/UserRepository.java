package com.codeWithProjects.ecom.repository;

import com.codeWithProjects.ecom.enums.UserRole;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codeWithProjects.ecom.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirstByEmail(String email);
    User findByRole(UserRole userRole);
    Optional<User> findById(Long UserId);

}
