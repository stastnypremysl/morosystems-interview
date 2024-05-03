package cz.morosystems.interview.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import cz.morosystems.interview.domain.User;
import cz.morosystems.interview.service.UserService;


record TrimmedUser (Long id, String name, String username) {
    TrimmedUser(User u){
        this(u.getId(), u.getName(), u.getUsername());
    }
}

record UserBodyRequest (
        Long id,

        @NotBlank(message = "A 'name' must be present in the request and " +
                "must contain at least one non-whitespace char.")
        @Size(min = 6, message = "A name of an user must have at least 6 chars.")
        @Size(max = 50, message = "A name of an user can have at most 50 chars.")
        String name,

        @NotNull(message = "A 'username' must be present in the request.")
        @Size(min = 6, message = "An username must have at least 6 chars.")
        @Size(max = 50, message = "An username can have at most 50 chars.")
        @Pattern(regexp="[a-z][-_.a-z0-9]*",
                message = "An username must begin with a small English letter and" +
                        " can only consist small English alphabet characters, numbers, '.', '_' and '-'.")
        String username,

        @NotNull(message = "A 'password' must be present in the request.")
        @Size(min = 8, message = "An user password must have at least 8 chars.")
        @Size(max = 255, message = "An user password can have at most 255 chars.")
        String password) {}


class NoUserIdGivenException extends RuntimeException {

    NoUserIdGivenException(String msg) {
        super(msg);
    }
}

class UserNotFoundException extends RuntimeException {

    UserNotFoundException(String msg) {
        super(msg);
    }
}

class DuplicateUsernameException extends RuntimeException {

    DuplicateUsernameException(String msg) {
        super(msg);
    }
}


@RestController
final class UsersController {

    private final UserService service;
    private final PasswordEncoder encoder;

    private UsersController(UserService service, PasswordEncoder encoder){
        this.service = service;
        this.encoder = encoder;
    }


    private UserNotFoundException notFoundException(long id){
        return new UserNotFoundException("Could not find the user with id=" + id + ".");   
    }

    private User userFromUserBodyRequest(UserBodyRequest req){
        User u = new User();
        u.setId(req.id());
        u.setName(req.name());
        u.setUsername(req.username());
        u.setEncodedPassword(encoder.encode(req.password()));
        return u;
    }


    @GetMapping("/api/v1/users/{id}")
    private TrimmedUser findById(@PathVariable Long id) throws UserNotFoundException {
        var optUser = service.findById(id);

        if (optUser.isPresent()) {
            return new TrimmedUser(optUser.get());
        } else {
            throw notFoundException(id);
        }

    }


    @PutMapping("/api/v1/users")
    private TrimmedUser update(@RequestBody @Valid UserBodyRequest inputUser)
            throws UserNotFoundException, NoUserIdGivenException {

        if(inputUser.id() == null){
            throw new NoUserIdGivenException("An user id must be given in the update request.");
        }

        var optUser = service.update(userFromUserBodyRequest(inputUser));

        if (optUser.isPresent()) {
            return new TrimmedUser(optUser.get());
        } else {
            throw notFoundException(inputUser.id());
        }

    }


    @DeleteMapping("/api/v1/users/{id}")
    private void deleteById(@PathVariable Long id) throws UserNotFoundException {
        Optional<User> user = service.deleteById(id);

        if (user.isEmpty()) {
            throw notFoundException(id);
        }
    }


    @GetMapping("/api/v1/users")
    private List<TrimmedUser> findAll(){
        var users = service.findAll();

        return users.stream().map(TrimmedUser::new).toList();
    }

    @PostMapping("/api/v1/users")
    private TrimmedUser newUser(@RequestBody @Valid UserBodyRequest inputUser) throws DuplicateUsernameException {

        if (service.findByUsername(inputUser.username()).isPresent()){
            throw new DuplicateUsernameException("Username " + inputUser.username() + " is already used.");
        }

        var user = userFromUserBodyRequest(inputUser);
        return new TrimmedUser(service.newUser(user));
    }


    @ExceptionHandler(value = NoUserIdGivenException.class)
    private ResponseEntity<String> handleNoUserIdGivenException(NoUserIdGivenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error 400: " + ex.getMessage());
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    private ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 404: " + ex.getMessage());
    }

    @ExceptionHandler(value = DuplicateUsernameException.class)
    private ResponseEntity<String> handleDuplicateUsernameException(DuplicateUsernameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Error 409: " + ex.getMessage());
    }

}
