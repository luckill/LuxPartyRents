package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.OrderDTO;

import java.util.TimerTask;

public class SchedulerService extends TimerTask {
OrderService orderService;

  @Override
  public void run() {

    orderService.OrderDueCheck(OrderDTO Orders);
   
  }
}
