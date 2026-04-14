package com.stockzeno.wms.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockzeno.wms.auth.dto.LoginRequest;
import com.stockzeno.wms.auth.dto.RegisterRequest;
import com.stockzeno.wms.identity.EmailVerificationTokenRepository;
import com.stockzeno.wms.identity.Role;
import com.stockzeno.wms.identity.RoleName;
import com.stockzeno.wms.identity.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailVerificationTokenRepository verificationTokenRepository;

    @Test
    void registerAndLogin() throws Exception {
        roleRepository.save(new Role(RoleName.STAFF));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("tester@stockzeno.local");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        String token = verificationTokenRepository.findAll().stream()
                .findFirst()
                .orElseThrow()
                .getToken();

        mockMvc.perform(get("/auth/verify")
                        .param("token", token))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("tester@stockzeno.local");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
