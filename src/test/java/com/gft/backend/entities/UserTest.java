package com.gft.backend.entities;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by miav on 2016-08-22.
 */
public class UserTest {
    //@Ignore
    @Test
    public void tryToSetGetId() throws Exception {
        User user = new User("testName", "testPass");
        user.setId(1);
        assertEquals(1,user.getId());
    }

    @Test
    public void tryToCompareUsers() throws Exception {
        User user1 = new User("testName", "testPass");
        User user2 = new User();
        user2.setId(1);
        user2.setDeleted((short) 1);
        user2.setLogin("testLogin");
        user2.setPassword("testPass");
        user2.setName("Name");
        user2.setSurname("Surname");
        user2.setUsername("testName");
        assertEquals(1,user2.getId());
        assertEquals(user1.getUsername(),user2.getUsername());
        assertEquals(user1.getPassword(),user2.getPassword());
        assertNotEquals(user1.getDeleted(), user2.getDeleted());
        assertNotEquals(user1.getName(), user2.getName());
        assertNotEquals(user1.getSurname(), user2.getSurname());
        assertNotEquals(user1.getLogin(), user2.getLogin());
    }


}