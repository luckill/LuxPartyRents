package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.*;

public interface OrderRepository extends JpaRepository<Order, Integer>
{

}
