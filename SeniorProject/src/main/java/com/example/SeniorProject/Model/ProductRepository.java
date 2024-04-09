package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

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
}
