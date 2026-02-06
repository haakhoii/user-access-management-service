package com.r2s.auth.service;

import com.r2s.auth.entity.User;

import java.util.UUID;

public interface UserQueryService {
    User getById(UUID userId);
}