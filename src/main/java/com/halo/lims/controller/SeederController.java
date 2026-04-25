package com.halo.lims.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/api/internal/seed")
public class SeederController {

    @Autowired
    private DataSource dataSource;

    @PostMapping
    public String seed() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/seed.sql"));
            populator.execute(dataSource);
            return "Seeding successful!";
        } catch (Exception e) {
            return "Seeding failed: " + e.getMessage();
        }
    }
}
