package in.sfp.main.service;

import java.util.List;

import in.sfp.main.models.CategInfo;

public interface CategInfoService {

    CategInfo saveCategory(CategInfo category);

    List<CategInfo> getAllCategories();

    List<CategInfo> getAllCategoriesByUser(String email, String role);

    CategInfo getCategoryById(Long id);

    CategInfo getCategoryByName(String categoryName);

    void deleteCategory(Long id);

    CategInfo updateCategory(Long id, CategInfo category);
}
