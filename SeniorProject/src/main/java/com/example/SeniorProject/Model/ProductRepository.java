package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.*;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer>
{
    @Transactional
    @Modifying
    @Query(" UPDATE Product p SET p.price = ?1 WHERE p.id=?2")
    void updateProductPriceById(int price, int id);
    

    @Transactional
    @Modifying
    @Query(" UPDATE Product p SET p.quantity = ?1 WHERE p.id=?2")
    void updateProductQuantityById(int quantity, int id);

    @Query(" SELECT p FROM Product p WHERE p.name = ?1")
    Product getProductByName(String name);
    
    @Query(" SELECT p FROM Product p WHERE p.type=?1")
    List<Product> getProductByType(String type);

    @Query(" SELECT p FROM Product p WHERE p.id=?1")
    List<Product> getProductById(int id);
    // For searching all together: Search name
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> findAllByNameContaining(@Param("searchTerm") String searchTerm);
    // For searching all together: Search type
    @Query("SELECT p FROM Product p WHERE LOWER(p.type) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> findAllByTypeContaining(@Param("searchTerm") String searchTerm);
    // For searching all together: Search id      
    @Query("SELECT p FROM Product p WHERE p.id = :searchTerm")
    List<Product> findAllByIdContaining(@Param("searchTerm") Integer searchTerm);

    @Query("SELECT p FROM Product p WHERE p.featureProduct = :featureProduct")
    List<Product> findAllByFeatureProduct(@Param("featureProduct") boolean featureProduct);
    
    @Transactional
    @Modifying
    @Query(" DELETE FROM Product p WHERE p.id=?1")
    void deleteById(int id);

}
