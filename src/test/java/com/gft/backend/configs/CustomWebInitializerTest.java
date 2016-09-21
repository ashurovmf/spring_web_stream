package com.gft.backend.configs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by miav on 2016-09-08.
 */
public class CustomWebInitializerTest {
    @Test
    public void tryToInit() throws Exception {
        CustomWebInitializer initializer = new CustomWebInitializer();
        assertEquals(SpringRootConfig.class,initializer.getRootConfigClasses()[0]);
        assertEquals(SpringWebConfig.class,initializer.getServletConfigClasses()[0]);
        assertEquals("/",initializer.getServletMappings()[0]);
    }

    @Test
    public void tryToInitWebSession() throws Exception {
        SpringSessionInitializer initializer = new SpringSessionInitializer();
        assertTrue("Session initializer is created", initializer != null);
    }

    @Test
    public void tryToInitWeb() throws Exception {
        CustomWebInitializer initializer = new CustomWebInitializer();
        assertTrue("Custom web initializer is created", initializer != null);
    }
}
