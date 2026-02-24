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
        List<CategInfo> categories = categInfoService.getAllCategories();
        return ResponseEntity.ok(categories);
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
