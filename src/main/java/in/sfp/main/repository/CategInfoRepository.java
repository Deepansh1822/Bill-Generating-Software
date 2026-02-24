package in.sfp.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.models.CategInfo;

@Repository
public interface CategInfoRepository extends JpaRepository<CategInfo, Long> {

    CategInfo findByCategoryName(String categoryName);

}
