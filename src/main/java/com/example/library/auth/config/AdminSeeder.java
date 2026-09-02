package com.example.library.auth.config;

import com.example.library.auth.entity.Role;
import com.example.library.auth.entity.User;
import com.example.library.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminUsername;
    private final String defaultAdminPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.default-username:admin}") String defaultAdminUsername,
                       @Value("${app.admin.default-password:admin123}") String defaultAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultAdminUsername = defaultAdminUsername;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User admin = User.builder()
                .username(defaultAdminUsername)
                .password(passwordEncoder.encode(defaultAdminPassword))
                .role(Role.ADMIN)
                .enabled(true)
                .memberId(null)
                .build();

        userRepository.save(admin);
        log.warn("No ADMIN account existed, so a default one was created (username: '{}'). " +
                "Please log in and change its password immediately.", defaultAdminUsername);
    }
}
