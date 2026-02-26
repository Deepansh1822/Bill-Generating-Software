package in.sfp.main.repository;

import in.sfp.main.models.SystemAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Long> {
    Optional<SystemAnnouncement> findFirstByActiveTrueOrderByCreatedAtDesc();
}
