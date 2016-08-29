package com.gft.backend.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by miav on 2016-08-19.
 */
@Repository("userDao")
public interface UsersRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
//    User findById(long id);
}
