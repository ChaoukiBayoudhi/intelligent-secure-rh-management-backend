package tn.sesame.rh_management_backend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private Instant timestamp;
}
