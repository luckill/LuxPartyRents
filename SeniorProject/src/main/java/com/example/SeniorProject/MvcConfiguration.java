package com.example.SeniorProject;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer
{
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("Login");
        registry.addViewController("/forgetPassword").setViewName("forgetPasswordForm");
        registry.addViewController("/form").setViewName("resetPasswordForm");
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/signUp").setViewName("signup");
        registry.addViewController("/gallery").setViewName("gallery");
        registry.addViewController("/rental").setViewName("rentals");
        registry.addViewController("/order").setViewName("UserOrder_page");
    }
}
