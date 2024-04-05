package com.example.SeniorProject.Email;

import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails
{
    private String recipient;
    private String messageBody;
    private String subject;
    private String attachment;
}


