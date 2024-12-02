package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.time.*;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

        // Fetch current (active) orders for a specific customer
        @Query("SELECT o FROM Order o WHERE o.customer.id = ?1 AND (o.status != 'completed' AND o.status != 'cancelled' AND o.status != 'refunded')")
        List<Order> findCurrentOrdersByCustomerId(int customerId);

        // Fetch past (completed) orders for a specific customer
        @Query("SELECT o FROM Order o WHERE o.customer.id = ?1 AND (o.status = 'completed' OR o.status = 'cancelled' OR o.status = 'refunded')")
        List<Order> findPastOrdersByCustomerId(int customerId);
        
        // Custom query to fetch an order by its ID
        @Query("SELECT o FROM Order o WHERE o.id = ?1")
        Order getOrderById(int id);

        // Optional: Custom delete method using a query (though JpaRepository has deleteById already)
        @Transactional
        @Modifying
        @Query("DELETE FROM Order o WHERE o.id = ?1")
        void deleteById(int id);

        @Query("SELECT o FROM Order o WHERE o.customer.id = ?1")
        List<Order> findOrderByCustomerId(int customerId);

        @Query("SELECT o FROM Order o WHERE o.status = 'PICK_UP' AND o.returnDate = :tomorrow")
        List<Order> findReturnOrders(@Param("tomorrow")LocalDate tomorrow);

        @Query(value = "SELECT * FROM orders WHERE order_status = 'RECEIVED' AND order_date < CURDATE()", nativeQuery = true)
        List<Order> findReceivedOrdersBeforeToday();

        @Modifying
        @Transactional
        @Query("DELETE FROM Order o WHERE o.status = 'RECEIVED' AND o.creationDate < CURRENT_DATE")
        void deleteReceivedOrdersBeforeToday();
}


