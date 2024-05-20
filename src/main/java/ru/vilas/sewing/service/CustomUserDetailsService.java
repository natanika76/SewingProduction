package ru.vilas.sewing.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                usernameOrEmail,
                user.getPassword(),
                authorities
        );
    }

    public Long getUserIdByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        // Проверяем, найден ли пользователь с заданным именем
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            // Обработка случая, когда пользователь с заданным именем не найден
            throw new RuntimeException("User not found with username: " + username);
        }
    }
    public String getNameByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        // Проверяем, найден ли пользователь с заданным именем
        if (userOptional.isPresent()) {
            return userOptional.get().getName();
        } else {
            // Обработка случая, когда пользователь с заданным именем не найден
            throw new RuntimeException("User not found with username: " + username);
        }
    }



    public User getCurrentUser() {
        // Получение информации о текущем пользователе
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Доступ к имени пользователя
        String username = authentication.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);

        // Проверяем, найден ли пользователь с заданным именем
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            // Обработка случая, когда пользователь с заданным именем не найден
            throw new RuntimeException("User not found with username: " + username);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String findNameById(Long id){
        return userRepository.findNameById(id);
    }
}