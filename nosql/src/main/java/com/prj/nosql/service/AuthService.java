package com.prj.nosql.service;

import com.prj.nosql.config.JwtTokenUtil;
import com.prj.nosql.dto.*;
import com.prj.nosql.model.User;
import com.prj.nosql.model.UserRole;
import com.prj.nosql.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    @Value("${app.encryption.key}")
    private String encryptionKey;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private SecretKeySpec getSecretKey() throws Exception {
        byte[] key = encryptionKey.getBytes("UTF-8");
        // Si la clé est trop longue, on la tronque à 16, 24 ou 32 octets
        int keyLength = 16; // 128 bits
        byte[] finalKey = new byte[keyLength];
        System.arraycopy(key, 0, finalKey, 0, Math.min(key.length, keyLength));
        return new SecretKeySpec(finalKey, "AES");
    }

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Autowired
    public AuthService(AuthenticationConfiguration authenticationConfiguration,
                       PasswordEncoder passwordEncoder,
                       JwtTokenUtil jwtTokenUtil,
                       UserRepository userRepository) throws Exception {
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            // Create initial admin account with fixed credentials
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin")); // Encoded password
            admin.setRole(UserRole.ADMIN);
            admin.setNom("Administrateur");
            admin.setPrenom("Principale");
            admin.setEmail("admin@university.edu");
            admin.setPasswordChanged(false);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());

            userRepository.save(admin);
            System.out.println("Compte admin initial créé avec les identifiants:");
            System.out.println("Username: admin");
            System.out.println("Password: admin");
        }
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setFullName(user.getFullName());
        response.setRequiresPasswordChange(!user.isPasswordChanged());

        return response;
    }

    public UserResponse createUser(CreateUserRequest request) {
        String password = generateRandomPassword();
        String encryptedPassword = encryptPassword(password);

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setPlainPassword(encryptedPassword);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(generateUniqueUsername(request.getRole()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        UserResponse response = convertToUserResponse(savedUser);
        response.setGeneratedPassword(password); // Retourné en clair une fois

        return response;
    }

    private String encryptPassword(String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(password.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chiffrement", e);
        }
    }
    private String generateUniqueUsername(UserRole role) {
        String prefix = getPrefixForRole(role);
        String username;
        int suffix = 0;

        do {
            suffix++;
            username = prefix + String.format("%06d", suffix);
        } while (userRepository.existsByUsername(username));

        return username;
    }
    private String getPrefixForRole(UserRole role) {
        switch (role) {
            case ADMIN: return "ADM";
            case PROFESSOR: return "PROF";
            case STUDENT: return "ETU";
            default: return "USR";
        }
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserResponse(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        user.setUpdatedAt(new Date());

        userRepository.save(user);
    }

    public void adminChangePassword(String userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(false); // Force user to change password on next login
        user.setUpdatedAt(new Date());

        userRepository.save(user);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());
        response.setPasswordChanged(user.isPasswordChanged());
        return response;
    }

    private String generateRandomUsername() {
        Random random = new Random();
        long number = (long) (random.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(number);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    public List<UserResponse> getAllUsersWithPasswords() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserResponse res = convertToUserResponse(user);
                    // Vérifier si plainPassword n'est pas null avant de déchiffrer
                    if (user.getPlainPassword() != null && !user.getPlainPassword().isEmpty()) {
                        try {
                            res.setDecryptedPassword(decryptPassword(user.getPlainPassword()));
                        } catch (Exception e) {
                            res.setDecryptedPassword("[Error decrypting]");
                            logger.error("Error decrypting password for user " + user.getUsername(), e);
                        }
                    } else {
                        res.setDecryptedPassword("");
                    }
                    return res;
                })
                .collect(Collectors.toList());
    }
    public String decryptPassword(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Decryption error for string: " + encrypted, e);
            return "[decryption error]";
        }
    }

}