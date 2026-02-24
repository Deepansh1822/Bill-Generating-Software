package in.sfp.main.service;

import in.sfp.main.models.UserAccessInfo;
import java.util.List;

public interface UserAccessService {

    public UserAccessInfo saveUsersAccessInfo(UserAccessInfo usersAccess);

    public UserAccessInfo findByEmail(String email);

    public UserAccessInfo findByMobileNumber(String mobileNumber);

    public UserAccessInfo findByUsername(String username);

    public UserAccessInfo updateUsersAccessInfo(UserAccessInfo usersAccess);

    public List<UserAccessInfo> getPendingRequests();

    public void approveRequest(Long id);

    public void rejectRequest(Long id);

    public UserAccessInfo findByToken(String token);

    public void resetPassword(String email, String secretKey, String newPassword);

    public UserAccessInfo createAdminAccount(UserAccessInfo adminInfo);
}
