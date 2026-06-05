package com.auth;

import com.auth.dto.in.LoginRequest;
import com.auth.dto.in.UserRequest;
import com.auth.entity.User;
import com.auth.repo.RepoUser;
import com.auth.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthServiceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepoUser repoUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String testSecret;

    @BeforeEach
    void cleanDatabase() {
        repoUser.findAll().forEach(user -> {
            if (!user.getUsername().equals("admin")) {
                repoUser.delete(user);
            }
        });
    }

    // @spec AUTH-REG-001
    @Test
    void testRegisterHashesPassword() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("testuser_reg1");
        request.setEmail("reg1@example.com");
        request.setPassword("Password123!");
        request.setName("Test");
        request.setLastName("User");
        request.setPhoneNumber("12345678901");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Optional<User> userOpt = repoUser.findByUsername("testuser_reg1");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertNotEquals("Password123!", user.getPassword());
        assertTrue(passwordEncoder.matches("Password123!", user.getPassword()));
    }

    // @spec AUTH-REG-002
    @Test
    void testRegisterAssignsUserRole() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("testuser_reg2");
        request.setEmail("reg2@example.com");
        request.setPassword("Password123!");
        request.setName("Test");
        request.setLastName("User");
        request.setPhoneNumber("12345678902");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Optional<User> userOpt = repoUser.findByUsername("testuser_reg2");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(Set.of("User"), user.getRoles());
    }

    // @spec AUTH-REG-003
    @Test
    void testRegisterPersistsUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("testuser_reg3");
        request.setEmail("reg3@example.com");
        request.setPassword("Password123!");
        request.setName("Test");
        request.setLastName("User");
        request.setPhoneNumber("12345678903");

        long countBefore = repoUser.count();

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(countBefore + 1, repoUser.count());
        Optional<User> userOpt = repoUser.findByUsername("testuser_reg3");
        assertTrue(userOpt.isPresent());
    }

    // @spec AUTH-REG-004
    @Test
    void testRegisterInvalidPayload() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername(""); // Invalid
        request.setEmail("invalid-email");
        request.setPassword("123"); // Weak password
        request.setName("Test");
        request.setLastName("User");
        request.setPhoneNumber("123"); // Invalid

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // @spec AUTH-REG-005
    @Test
    void testRegisterDuplicateConflict() throws Exception {
        User user = new User();
        user.setUsername("dup_user");
        user.setEmail("dup@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setName("Dup");
        user.setLastName("User");
        user.setPhoneNumber("9999999999");
        user.setRoles(Set.of("User"));
        repoUser.save(user);

        UserRequest requestDupUsername = new UserRequest();
        requestDupUsername.setUsername("dup_user");
        requestDupUsername.setEmail("other@example.com");
        requestDupUsername.setPassword("Password123!");
        requestDupUsername.setName("Other");
        requestDupUsername.setLastName("User");
        requestDupUsername.setPhoneNumber("1111111111");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDupUsername)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", notNullValue()));

        UserRequest requestDupEmail = new UserRequest();
        requestDupEmail.setUsername("other_user");
        requestDupEmail.setEmail("dup@example.com");
        requestDupEmail.setPassword("Password123!");
        requestDupEmail.setName("Other");
        requestDupEmail.setLastName("User");
        requestDupEmail.setPhoneNumber("2222222222");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDupEmail)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", notNullValue()));

        UserRequest requestDupPhone = new UserRequest();
        requestDupPhone.setUsername("third_user");
        requestDupPhone.setEmail("third@example.com");
        requestDupPhone.setPassword("Password123!");
        requestDupPhone.setName("Other");
        requestDupPhone.setLastName("User");
        requestDupPhone.setPhoneNumber("9999999999");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDupPhone)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // @spec AUTH-LOG-001, AUTH-LOG-002, AUTH-LOG-003
    @Test
    void testLoginSuccessAndJwtValidation() throws Exception {
        User user = new User();
        user.setUsername("login_user");
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setName("Login");
        user.setLastName("User");
        user.setPhoneNumber("12345678904");
        user.setRoles(Set.of("User"));
        user = repoUser.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("login_user");
        loginRequest.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(responseStr, Map.class);
        String token = responseMap.get("token");

        Key key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("login_user", claims.getSubject());
        assertEquals(user.getId().intValue(), claims.get("id", Integer.class));
        List<String> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertTrue(roles.contains("User"));

        long diffMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(Math.abs(diffMs - 3600000) < 5000);
    }

    // @spec AUTH-LOG-004
    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("WrongPass!");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // @spec AUTH-LST-001
    @Test
    void testUserListingAuthorized() throws Exception {
        User admin = repoUser.findByUsername("temp_admin").orElseGet(() -> {
            User u = new User();
            u.setUsername("temp_admin");
            u.setEmail("temp_admin@example.com");
            u.setPassword(passwordEncoder.encode("Password123!"));
            u.setName("Temp");
            u.setLastName("Admin");
            u.setPhoneNumber("9999999998");
            u.setRoles(Set.of("Administrator"));
            return repoUser.save(u);
        });

        String token = jwtUtil.generateToken(admin);

        mockMvc.perform(get("/user")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].username", notNullValue()))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // @spec AUTH-LST-002
    @Test
    void testUserListingForbiddenForNormalUser() throws Exception {
        User user = new User();
        user.setUsername("normal_user");
        user.setEmail("normal@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setName("Normal");
        user.setLastName("User");
        user.setPhoneNumber("12345678905");
        user.setRoles(Set.of("User"));
        repoUser.save(user);

        String token = jwtUtil.generateToken(user);

        mockMvc.perform(get("/user")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // @spec AUTH-LST-003
    @Test
    void testUserListingUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    // @spec AUTH-INT-003
    @Test
    void testAdminUserSeededOnStartup() {
        Optional<User> adminOpt = repoUser.findByUsername("admin");
        assertTrue(adminOpt.isPresent(), "Expected default admin user to be initialized on startup");
        User admin = adminOpt.get();
        assertTrue(admin.getRoles().contains("Administrator"));
    }
}
