package in.sfp.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.models.UserAccessInfo;

import java.util.List;

@Repository
public interface UserAccessRepo extends JpaRepository<UserAccessInfo, Long> {

    UserAccessInfo findByEmail(String email);

    UserAccessInfo findByMobileNumber(String mobileNumber);

    UserAccessInfo findByUsername(String username);

    List<UserAccessInfo> findByStatus(String status);

    UserAccessInfo findBySetupToken(String token);

    UserAccessInfo findByResetToken(String resetToken);

    List<UserAccessInfo> findByRole(String role);

    long countByRoleAndStatus(String role, String status);

    long countByStatus(String status);
}
