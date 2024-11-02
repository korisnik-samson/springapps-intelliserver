package com.samson.springappsintelliserver.repositories;

import com.samson.springappsintelliserver.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Integer> {

    Users findByUsername(String username);
    Users findByEmail(String email);
    Users findByUsernameAndPassword(String username, String password);

}
