package projects.dnetsova.taskmanager.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import projects.dnetsova.taskmanager.entities.User;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = :newName WHERE u.name = :name")
    int updateUserName(String name, String newName);

    @Query("SELECT u.name FROM User u")
    Page<String> findAllUserNames(Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.name = :name")
    int delete(String name);
}
