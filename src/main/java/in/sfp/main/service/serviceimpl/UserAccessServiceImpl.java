package in.sfp.main.service.serviceimpl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import in.sfp.main.models.UserAccessInfo;
import in.sfp.main.repository.UserAccessRepo;
import in.sfp.main.service.UserAccessService;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccessRepo usersAccessRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Override // saving user access info with status "Pending"
    public UserAccessInfo saveUsersAccessInfo(UserAccessInfo usersAccess) {
        usersAccess.setStatus("PENDING");
        usersAccess.setRole("CLIENT");
        UserAccessInfo saved = usersAccessRepo.save(usersAccess);

        // Send an acknowledgement email to the client confirming request received
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(saved.getEmail());
            msg.setSubject("SFP Billing - Access Request Received");
            StringBuilder body = new StringBuilder();
            body.append("Hello ");
            body.append(saved.getFullName() != null ? saved.getFullName() : "");
            body.append(",\n\n");
            body.append("Thanks for requesting access to SFP Billing. We have received your request and will review it shortly.\n\n");
            body.append("Submitted details:\n");
            body.append("Company Name: ").append(saved.getCompanyName() != null ? saved.getCompanyName() : "-").append("\n");
            body.append("Company Type: ").append(saved.getCompanyType() != null ? saved.getCompanyType() : "-").append("\n");
            body.append("Email: ").append(saved.getEmail()).append("\n");
            body.append("Mobile: ").append(saved.getMobileNumber() != null ? saved.getMobileNumber() : "-").append("\n\n");
            body.append("We'll notify you when your account is approved.\n\nRegards,\nSFP Team");
            msg.setText(body.toString());
            mailSender.send(msg);
        } catch (Exception ex) {
            System.out.println("Failed to send acknowledgement email: " + ex.getMessage());
        }

        return saved;
    }

    @Override
    public UserAccessInfo findByEmail(String email) {
        return usersAccessRepo.findByEmail(email);
    }

    @Override
    public UserAccessInfo findByMobileNumber(String mobileNumber) {
        return usersAccessRepo.findByMobileNumber(mobileNumber);
    }

    @Override
    public UserAccessInfo findByUsername(String username) {
        return usersAccessRepo.findByUsername(username);
    }

    @Override
    public UserAccessInfo updateUsersAccessInfo(UserAccessInfo usersAccess) {
        return usersAccessRepo.save(usersAccess);
    }

    @Override
    public List<UserAccessInfo> getPendingRequests() {
        return usersAccessRepo.findByStatus("PENDING");
    }

    @Override
    public void approveRequest(Long id) {
        UserAccessInfo user = usersAccessRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Generate Random Username
        String randomUsername = "SFP_" + (System.currentTimeMillis() % 10000);
        user.setUsername(randomUsername);

        // 2. Generate Setup Token
        String token = UUID.randomUUID().toString();
        user.setSetupToken(token);

        // 3. Update Status
        user.setStatus("APPROVED");
        usersAccessRepo.save(user);

        // 4. Send Real Email
        sendSetupEmail(user.getEmail(), randomUsername, token);
    }

    @Override
    public void rejectRequest(Long id) {
        UserAccessInfo user = usersAccessRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("REJECTED");
        usersAccessRepo.save(user);
    }

    @Override
    public UserAccessInfo findByToken(String token) {
        return usersAccessRepo.findBySetupToken(token);
    }

    @Override
    public void resetPassword(String email, String secretKey, String newPassword) {
        UserAccessInfo user = usersAccessRepo.findByEmailAndSecretKey(email, secretKey);
        if (user == null) {
            throw new RuntimeException("Invalid Email or Secret Key");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        usersAccessRepo.save(user);
    }

    @Override
    public UserAccessInfo createAdminAccount(UserAccessInfo adminInfo) {
        adminInfo.setRole("ADMIN");
        adminInfo.setStatus("APPROVED"); // Admins are pre-approved
        if (adminInfo.getPassword() != null) {
            adminInfo.setPassword(passwordEncoder.encode(adminInfo.getPassword()));
        }
        return usersAccessRepo.save(adminInfo);
    }

    @Override
    public List<UserAccessInfo> getAllClients() {
        return usersAccessRepo.findByRole("CLIENT");
    }

    private void sendSetupEmail(String email, String username, String token) {
        String setupLink = "http://localhost:8080/billing-app/api/setup-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("SFP Billing Access Approved");
        message.setText("Welcome to SFP Billing!\n\n" +
                "Your request for access has been approved.\n\n" +
                "Your temporary username is: " + username + "\n" +
                "Please set your secret password using this link: " + setupLink + "\n\n" +
                "Note: This link is for one-time use only.\n\n" +
                "Best Regards,\nSFP Team");

        mailSender.send(message);

        System.out.println("Email successfully sent to: " + email);
    }
}
