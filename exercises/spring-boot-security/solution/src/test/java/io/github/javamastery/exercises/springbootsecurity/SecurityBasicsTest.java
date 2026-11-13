package io.github.javamastery.exercises.springbootsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "learning.security.student-password=student-test-password",
        "learning.security.admin-password=admin-test-password"
})
@AutoConfigureMockMvc
class SecurityBasicsTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserDetailsService users;

    @Test
    void permitsPublicResources() throws Exception {
        mvc.perform(get("/courses")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void redirectsAnonymousUserToLogin() throws Exception {
        mvc.perform(get("/profile")).andExpect(status().isFound()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void authenticatesRealPasswordAndReusesSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("student").password("student-test-password"))
                .andExpect(authenticated().withUsername("student").withRoles("STUDENT"))
                .andExpect(redirectedUrl("/profile")).andReturn().getRequest().getSession(false);

        mvc.perform(get("/profile").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student"));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mvc.perform(formLogin().user("student").password("wrong-password"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void storesEncodedPasswords() {
        String stored = users.loadUserByUsername("student").getPassword();
        assertNotEquals("student-test-password", stored);
        assertTrue(stored.startsWith("{bcrypt}"));
        assertTrue(encoder.matches("student-test-password", stored));
    }

    @Test
    void deniesStudentAccessToAdminEndpoint() throws Exception {
        mvc.perform(get("/admin/summary").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void permitsAdminEndpoint() throws Exception {
        mvc.perform(get("/admin/summary").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.courseCount").value(1));
    }

    @Test
    void writesOnlyAuthenticatedUsersOwnProgressWithCsrf() throws Exception {
        mvc.perform(post("/progress").with(user("writer").roles("STUDENT")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completedLessons\":4,\"username\":\"victim\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("writer"))
                .andExpect(jsonPath("$.completedLessons").value(4));
        mvc.perform(get("/progress").with(user("writer").roles("STUDENT")))
                .andExpect(jsonPath("$.completedLessons").value(4));
        mvc.perform(get("/progress").with(user("victim").roles("STUDENT")))
                .andExpect(jsonPath("$.completedLessons").value(0));
    }

    @Test
    void rejectsMissingOrInvalidCsrfWithoutWriting() throws Exception {
        mvc.perform(post("/progress").with(user("csrf-check").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completedLessons\":5}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/progress").with(user("csrf-check").roles("STUDENT")).with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completedLessons\":5}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/progress").with(user("csrf-check").roles("STUDENT")))
                .andExpect(jsonPath("$.completedLessons").value(0));
    }

    @Test
    void logsOutAndInvalidatesAuthenticatedSession() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("student").password("student-test-password"))
                .andExpect(authenticated()).andReturn().getRequest().getSession(false);

        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/courses"));
        assertTrue(session.isInvalid());
        mvc.perform(get("/profile")).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void deniesUnlistedRoutesEvenForAdmin() throws Exception {
        mvc.perform(get("/unlisted").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidProgressWithoutWriting() throws Exception {
        mvc.perform(post("/progress").with(user("invalid-input").roles("STUDENT")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completedLessons\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/progress").with(user("invalid-input").roles("STUDENT")))
                .andExpect(jsonPath("$.completedLessons").value(0));
    }
}
