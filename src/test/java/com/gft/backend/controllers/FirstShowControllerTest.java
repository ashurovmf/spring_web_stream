package com.gft.backend.controllers;

import com.gft.backend.configs.SpringWebConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by miav on 2016-08-22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigWebContextLoader.class, classes = {SpringWebConfig.class})
@WebAppConfiguration
public class FirstShowControllerTest {

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    @WithMockUser
    public void toShowHelloPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("title","Home page"));
    }

    @Test
    @WithMockUser
    public void toForbiddenPage() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("403"));
    }

    @Test
    @WithMockUser
    public void toDBAPage() throws Exception {
        mockMvc.perform(get("/dba"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("title","Database Page"));
    }

    @Test
    @WithMockUser
    public void toLogoutPage() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is(302));
    }

    @Test
    @WithMockUser
    public void toLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser
    public void toAdminPage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("title","Spring Security - Admin Page"));
    }

}