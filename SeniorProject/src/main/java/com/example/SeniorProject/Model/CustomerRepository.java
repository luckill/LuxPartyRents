package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;

public interface CustomerRepository extends JpaRepository<Customer, Integer>
{
    @Query(" SELECT c FROM Customer c WHERE c.email=?1")
    Customer findCustomersByEmail(String email);
}
