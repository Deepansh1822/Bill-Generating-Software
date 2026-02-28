package in.sfp.main.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.sfp.main.models.CategInfo;
import in.sfp.main.service.serviceimpl.CategInfoServiceImpl;

@RestController
@RequestMapping("/billing-app/api")
public class CategInfoController {

    @Autowired
    private CategInfoServiceImpl categInfoService;

    @PostMapping("/saveCategory")
    public ResponseEntity<CategInfo> saveCategory(@RequestBody CategInfo category) {
        CategInfo savedCategory = categInfoService.saveCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @GetMapping("/getAllCategories")
    public ResponseEntity<List<CategInfo>> getAllCategories() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("CLIENT");

        List<CategInfo> categories = categInfoService.getAllCategoriesByUser(email, role);
        // Strip image bytes from list response — images are served separately via
        // /getCategoryImage/{id}
        categories.forEach(c -> c.setCategoryImage(null));
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/getCategoryImage/{id}")
    public ResponseEntity<byte[]> getCategoryImage(@PathVariable Long id) {
        CategInfo category = categInfoService.getCategoryById(id);
        if (category == null || category.getCategoryImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .header("Cache-Control", "public, max-age=86400") // cache for 1 day
                .body(category.getCategoryImage());
    }

    @GetMapping("/getCategoryById/{id}")
    public ResponseEntity<CategInfo> getCategoryById(@PathVariable Long id) {
        CategInfo category = categInfoService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    @GetMapping("/getCategoryByName/{categoryName}")
    public ResponseEntity<CategInfo> getCategoryByName(@PathVariable String categoryName) {
        CategInfo category = categInfoService.getCategoryByName(categoryName);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categInfoService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<CategInfo> updateCategory(@PathVariable Long id, @RequestBody CategInfo category) {
        CategInfo updatedCategory = categInfoService.updateCategory(id, category);
        if (updatedCategory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCategory);
    }

}
