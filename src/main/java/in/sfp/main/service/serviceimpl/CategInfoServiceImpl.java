package in.sfp.main.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import in.sfp.main.models.CategInfo;
import in.sfp.main.repository.CategInfoRepository;
import in.sfp.main.service.CategInfoService;

@Service
public class CategInfoServiceImpl implements CategInfoService {

    @Autowired
    private CategInfoRepository categInfoRepository;

    @Override
    public CategInfo saveCategory(CategInfo category) {
        if (category.getCategoryCreatedBy() == null) {
            category.setCategoryCreatedBy(getCurrentUser());
        }
        return categInfoRepository.save(category);
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "System";
    }

    @Override
    public List<CategInfo> getAllCategories() {
        return categInfoRepository.findAll();
    }

    @Override
    public List<CategInfo> getAllCategoriesByUser(String email, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return categInfoRepository.findAll();
        } else {
            return categInfoRepository.findByCategoryCreatedBy(email);
        }
    }

    @Override
    public CategInfo getCategoryById(Long id) {
        return categInfoRepository.findById(id).orElse(null);
    }

    @Override
    public CategInfo getCategoryByName(String categoryName) {
        return categInfoRepository.findByCategoryName(categoryName);
    }

    @Override
    public void deleteCategory(Long id) {
        categInfoRepository.deleteById(id);
    }

    @Override
    public CategInfo updateCategory(Long id, CategInfo category) {
        CategInfo existingCategory = categInfoRepository.findById(id).orElse(null);
        if (existingCategory != null) {
            existingCategory.setCategoryName(category.getCategoryName());
            existingCategory.setCategoryDescription(category.getCategoryDescription());
            if (category.getCategoryImage() != null) {
                existingCategory.setCategoryImage(category.getCategoryImage());
            }
            existingCategory.setCategoryUpdatedBy(getCurrentUser());
            // categoryUpdatedAt will be handled by @PreUpdate
            return categInfoRepository.save(existingCategory);
        }
        return null;
    }

}
