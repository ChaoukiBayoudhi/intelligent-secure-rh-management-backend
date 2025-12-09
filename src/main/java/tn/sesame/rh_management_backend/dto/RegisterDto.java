package tn.sesame.rh_management_backend.dto;

import tn.sesame.rh_management_backend.Enumerations.UserRole;

public record RegisterDto(String email,
                          String password,
                          UserRole role
                        ) {}
