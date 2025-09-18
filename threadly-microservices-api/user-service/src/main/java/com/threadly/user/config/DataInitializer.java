package com.threadly.user.config;

import com.threadly.user.constant.RoleConstants;
import com.threadly.user.entity.Role;
import com.threadly.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(RoleConstants.USER);
            userRole.setDescription("Default user role");

            Role adminRole = new Role();
            adminRole.setName(RoleConstants.ADMIN);
            adminRole.setDescription("Administrator role");

            roleRepository.save(userRole);
            roleRepository.save(adminRole);
        }
    }
}
