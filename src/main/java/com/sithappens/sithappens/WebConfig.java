package com.sithappens.sithappens;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// configuration for serving uploaded files (like pet images)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // maps /uploads URL to the folder where images are stored
        // this allows Spring Boot to serve images from the file system instead of classpath

        // example: /uploads/dog.jpg → src/main/resources/static/uploads/dog.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/static/uploads/");

        // note: images are successfully saved and paths are stored in the database
        // however, due to redirect behavior after form submission,
        // the updated image path may not immediately appear on the dashboard

        // the image displays correctly after a new request (such as editing the pet)
        // because the data is reloaded from the database at that point
    }
}

// did it take longer for it to upload or there could be something wrong with the ad 