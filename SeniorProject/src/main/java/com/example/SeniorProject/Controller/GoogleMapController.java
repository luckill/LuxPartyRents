package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Service.GoogleMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/map")
public class GoogleMapController
{
    @Autowired
    private GoogleMapService googleMapService;

    @PostMapping("/getPlaceId")
    public ResponseEntity<?> getPlaceId(@RequestBody String address)
    {
        String result = googleMapService.getPlaceId(address);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calculateDeliveryFee")
    public ResponseEntity<?> calculateDeliveryFee( @RequestParam String destinationPlaceId)
    {
        double deliveryFee = googleMapService.calculateDeliveryFee(destinationPlaceId);
        return ResponseEntity.status(HttpStatus.OK).body(deliveryFee);
    }
}
