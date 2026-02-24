package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.service.serviceimpl.UserBillingServiceImpl;

@RestController
@RequestMapping("/billing-app/api")
public class UsersBillingController {

    @Autowired
    private UserBillingServiceImpl usersBillingService;

    @PostMapping("/saveUsersBillingInfo")
    public ResponseEntity<BusinessBillingInfo> saveUsersBillingInfo(@RequestBody BusinessBillingInfo usersBilling) {
        BusinessBillingInfo saved = usersBillingService.saveUsersBillingInfo(usersBilling);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<BusinessBillingInfo> findByEmail(@PathVariable String email) {
        BusinessBillingInfo info = usersBillingService.findByBusinessEmail(email);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/findByMobileNumber/{mobileNumber}")
    public ResponseEntity<BusinessBillingInfo> findByMobileNumber(@PathVariable String mobileNumber) {
        BusinessBillingInfo info = usersBillingService.findByBusinessNumber(mobileNumber);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/findByUsername/{username}")
    public ResponseEntity<BusinessBillingInfo> findByUsername(@PathVariable String username) {
        BusinessBillingInfo info = usersBillingService.findByCreatedBy(username);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @PutMapping("/updateUsersBillingInfo/{id}")
    public ResponseEntity<BusinessBillingInfo> updateUsersBillingInfo(@RequestBody BusinessBillingInfo usersBilling,
            @PathVariable Long id) {
        BusinessBillingInfo updated = usersBillingService.updateUsersBillingInfo(usersBilling, id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
