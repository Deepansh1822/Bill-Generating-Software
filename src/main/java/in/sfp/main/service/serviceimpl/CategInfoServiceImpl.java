package in.sfp.main.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
        return categInfoRepository.save(category);
    }

    @Override
    public List<CategInfo> getAllCategories() {
        return categInfoRepository.findAll();
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
            return categInfoRepository.save(existingCategory);
        }
        return null;
    }

}
