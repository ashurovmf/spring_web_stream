package com.gft.backend.entities;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by miav on 2016-08-22.
 */
public class UserTest {
    @Ignore
    @Test
    public void tryToSetGetId() throws Exception {
        User user = new User("testName", "testPass");
        user.setId(1);
        assertEquals(1,user.getId());
    }


}