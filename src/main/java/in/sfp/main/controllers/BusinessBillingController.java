package in.sfp.main.controllers;

import in.sfp.main.models.BusinessBillingInfo;
import in.sfp.main.service.serviceimpl.BusinessBillingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/billing-app/api/business")
public class BusinessBillingController {

    @Autowired
    private BusinessBillingServiceImpl businessService;

    @PostMapping("/save")
    public ResponseEntity<BusinessBillingInfo> saveBusinessBillingInfo(@RequestBody BusinessBillingInfo info) {
        BusinessBillingInfo saved = businessService.saveBusinessBillingInfo(info);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<BusinessBillingInfo> findByEmail(@PathVariable String email) {
        BusinessBillingInfo info = businessService.findByEmail(email);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BusinessBillingInfo>> getAll() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<BusinessBillingInfo> update(@RequestBody BusinessBillingInfo info, @PathVariable Long id) {
        BusinessBillingInfo updated = businessService.updateBusinessBillingInfo(info, id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
