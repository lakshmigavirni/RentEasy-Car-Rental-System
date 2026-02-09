package com.CustomerService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @DeleteMapping("/auth/user/{userId}")
    void deleteUserById(@PathVariable("userId") int id);

}
