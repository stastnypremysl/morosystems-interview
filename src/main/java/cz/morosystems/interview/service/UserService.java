package cz.morosystems.interview.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cz.morosystems.interview.domain.User;
import cz.morosystems.interview.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository repo;

    private UserService(UserRepository repo){
        this.repo = repo;
    }


    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }


    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }


    public Optional<User> deleteById(Long id){
        var optUser = repo.findById(id);
        optUser.ifPresent(repo::delete);

        return optUser;
    }


    public List<User> findAll(){
        return repo.findAll();
    }


    public User newUser(User inputUser){
        var user = new User(inputUser);
        user.setId(null);

        return repo.save(user);
    }


    public Optional<User> update(User user){
        if(!repo.existsById(user.getId())) {
            return Optional.empty();
        }

        return Optional.of(repo.save(user));
    }
}
