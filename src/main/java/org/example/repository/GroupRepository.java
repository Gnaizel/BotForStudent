package org.example.repository;

import org.example.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByChatId(long chatId);
    boolean deleteByChatId(long chatId);
}
