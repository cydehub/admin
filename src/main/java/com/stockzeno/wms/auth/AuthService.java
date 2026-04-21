package com.stockzeno.wms.auth;

import com.stockzeno.wms.auth.dto.AuthResponse;
import com.stockzeno.wms.auth.dto.LoginRequest;
import com.stockzeno.wms.auth.dto.RefreshRequest;
import com.stockzeno.wms.auth.dto.RegisterRequest;
import com.stockzeno.wms.auth.dto.RegisterResponse;
import com.stockzeno.wms.auth.dto.VerifyEmailResponse;
import com.stockzeno.wms.config.AppProperties;
import com.stockzeno.wms.identity.RefreshToken;
import com.stockzeno.wms.identity.RefreshTokenRepository;
import com.stockzeno.wms.identity.Role;
import com.stockzeno.wms.identity.RoleName;
import com.stockzeno.wms.identity.RoleRepository;
import com.stockzeno.wms.identity.User;
import com.stockzeno.wms.identity.EmailVerificationToken;
import com.stockzeno.wms.identity.EmailVerificationTokenRepository;
import com.stockzeno.wms.identity.UserRepository;
import com.stockzeno.wms.identity.UserStatus;
import com.stockzeno.wms.notification.EmailNotificationRequest;
import com.stockzeno.wms.notification.NotificationService;
import com.stockzeno.wms.security.JwtService;
import com.stockzeno.wms.security.SecurityProperties;
import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final NotificationService notificationService;
    private final AppProperties appProperties;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       EmailVerificationTokenRepository verificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       SecurityProperties securityProperties,
                       NotificationService notificationService,
                       AppProperties appProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
        this.notificationService = notificationService;
        this.appProperties = appProperties;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        Role defaultRole = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role missing"));

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.DISABLED);
        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);

        return new RegisterResponse("Verification email sent. Please check your inbox.", savedUser.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User existingUser = userRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (existingUser.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(existingUser.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, refreshToken);

        return buildResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        EmailVerificationToken verification = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid verification token"));
        if (verification.getVerifiedAt() != null) {
            return new VerifyEmailResponse("Email already verified.");
        }
        if (verification.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification token expired");
        }
        User user = verification.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        verification.setVerifiedAt(Instant.now());
        verificationTokenRepository.save(verification);
        return new VerifyEmailResponse("Email verified. You can now sign in.");
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        if (!jwtService.isRefreshToken(request.getRefreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token type");
        }

        User user = storedToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        persistRefreshToken(user, refreshToken);

        return buildResponse(user, accessToken, refreshToken);
    }

    private void persistRefreshToken(User user, String refreshToken) {
        Instant expiresAt = Instant.now().plus(securityProperties.getRefreshTokenTtl());
        RefreshToken token = new RefreshToken(refreshToken, user, expiresAt);
        refreshTokenRepository.save(token);
    }

    private void sendVerificationEmail(User user) {
        verificationTokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(securityProperties.getEmailVerificationTtl());
        EmailVerificationToken verification = new EmailVerificationToken(user, token, expiresAt);
        verificationTokenRepository.save(verification);

        String baseUrl = appProperties.getFrontendBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String verifyUrl = baseUrl + "/verify.html?token=" + token;
        String body = String.format(
                "Welcome to Cydestore.ke!%n%nVerify your email to activate your account:%n%s%n%nThis link expires in 24 hours.",
                verifyUrl
        );
        notificationService.sendEmail(new EmailNotificationRequest(user.getEmail(), "Verify your Cydestore account", body));
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        long expiresIn = securityProperties.getAccessTokenTtl().toSeconds();
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                user.getId(),
                user.getEmail(),
                roles
        );
    }
}
