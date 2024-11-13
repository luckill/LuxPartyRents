package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.OrderDTO;
import com.example.SeniorProject.Model.OrderRepository;

import java.util.TimerTask;

public class SchedulerService extends TimerTask {
OrderService orderService;
OrderRepository orderRepository;

  @Override
  public void run() {

    orderService.OrderDueCheck(orderRepository.findConfirmedOrdersOnReturnDay());

  }
}
