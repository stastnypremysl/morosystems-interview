package cz.morosystems.interview.security;

import java.util.Optional;

import org.springframework.stereotype.Service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cz.morosystems.interview.domain.User;
import cz.morosystems.interview.service.UserService;


@Service
final class CustomUserDetailsService implements UserDetailsService {

    private final UserService service;

    private CustomUserDetailsService(UserService service){
        this.service = service;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = service.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }

        return new CustomUserDetails(user.get());
    }
}
