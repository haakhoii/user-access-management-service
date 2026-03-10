package com.r2s.auth.domain.httpclient;

import com.r2s.auth.config.AuthenticationRequestInterceptor;
import com.r2s.core.dto.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    url = "http://localhost:8082/api/v1/user",
    configuration = {AuthenticationRequestInterceptor.class}
)
public interface UserClient {

  @DeleteMapping(value = "/delete-profile/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
  ApiResponse<String> deleteProfile(@PathVariable("username") String username);

}