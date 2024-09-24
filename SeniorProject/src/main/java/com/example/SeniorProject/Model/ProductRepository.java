package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

import java.util.List;

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
    List<Product> getProductByName(String name);
    @Query(" SELECT p FROM Product p WHERE p.type=?1")
    List<Product> getProductByType(String type);

    @Query(" SELECT p FROM Product p WHERE p.id=?1")
    Product getProductById(int id);

    @Transactional
    @Modifying
    @Query(" DELETE FROM Product p WHERE p.id=?1")
    void deleteById(int id);
}
