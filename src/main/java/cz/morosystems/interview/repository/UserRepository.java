package cz.morosystems.interview.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.morosystems.interview.domain.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
