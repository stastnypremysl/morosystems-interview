package cz.morosystems.interview.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import cz.morosystems.interview.service.UserService;
import cz.morosystems.interview.exceptions.UnexpectedErrorException;


record ChangePasswordRequestBody(
        @NotNull(message = "A 'password' must be present in the request.")
        @Size(min = 8, message = "An user password must have at least 8 chars.")
        @Size(max = 255, message = "An user password can have at most 255 chars.")
        String password)
{}


class PasswordSameException extends RuntimeException {

    PasswordSameException() {
        super("The new password is the same as the current one.");
    }
}


@RestController
final class UserController {

    private final UserService service;
    private final PasswordEncoder encoder;

    private UserController(UserService service, PasswordEncoder encoder) {
        this.service = service;
        this.encoder = encoder;

    }

    @PutMapping(value = "/api/v1/user/password")
    private void changePassword(Authentication auth, @RequestBody @Valid ChangePasswordRequestBody body)
            throws PasswordSameException {

        var username = auth.getName();
        var optUser = service.findByUsername(username);

        if(optUser.isEmpty()){
            throw new UnexpectedErrorException("The logged-in user can't be found in the database.");
        }

        var user = optUser.get();
        if(encoder.matches(body.password(), user.getEncodedPassword())){
            throw new PasswordSameException();
        }

        user.setEncodedPassword(encoder.encode(body.password()));
        service.update(user);
    }

    @ExceptionHandler(value = PasswordSameException.class)
    private ResponseEntity<String> handlePasswordSameException(PasswordSameException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error 422: " + ex.getMessage());
    }
}
