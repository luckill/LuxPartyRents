package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>
{
        @Query(" SELECT p FROM Order p WHERE p.id=?1")
        List<Product> getOrderById(int id);

        @Transactional
        @Modifying
        @Query(" DELETE FROM Order p WHERE p.id=?1")
        void deleteById(int id);
}
