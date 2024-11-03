package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;

import java.util.List;

import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Service
public class RentalService
{
    @Autowired
    private RentalRepository rentalRepository;

    public Page<Product> getAllProducts(String keyword, String type, Pageable pageable)
    {
        if (keyword != null && !keyword.isEmpty() && type != null && !type.isEmpty())
        {
            // Apply both keyword and type filters
            return rentalRepository.findByNameContainingIgnoreCaseAndTypeIgnoreCase(keyword, type, pageable);
        }
        else if (keyword != null && !keyword.isEmpty())
        {
            // Apply keyword filter only
            return rentalRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        else if (type != null && !type.isEmpty())
        {
            // Apply type filter only
            return rentalRepository.findByTypeIgnoreCase(type, pageable);
        }
        return rentalRepository.findAll(pageable);
    }

    public Page<Product> searchProducts(String keyword, String type, Pageable pageable)
    {
        if (keyword != null && !keyword.isEmpty())
        {
            return rentalRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        else if (type != null && !type.isEmpty())
        {
            return rentalRepository.findByTypeIgnoreCase(type, pageable);
        }
        else
        {
            return rentalRepository.findAll(pageable);
        }
    }
    
    public List<String> getDistinctProductTypes() {
        return rentalRepository.findDistinctProductTypes();
    }
}
