package tn.sesame.rh_management_backend.Web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tn.sesame.rh_management_backend.Services.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final UserService userService;
}
