package ru.vilas.sewing.service.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.config.UsernameExistsException;
import ru.vilas.sewing.model.Role;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.RoleRepository;
import ru.vilas.sewing.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeamstressServiceImpl implements SeamstressService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public SeamstressServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllSeamstresses() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_USER")))
                .collect(Collectors.toList());
    }

    @Override
    public void saveSeamstress(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException("Пользователь с логином " + user.getUsername() + " уже существует");
        }

        // Кодирование пароля перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Получение объекта роли "user" из базы данных
        Optional<Role> optionalUserRole = roleRepository.findByName("ROLE_USER");

        // Извлечение объекта роли из Optional
        Role userRole = optionalUserRole.orElseThrow(() -> new RuntimeException("Роль 'USER' не найдена в базе данных"));

        // Присвоение роли "user" пользователю
        user.setRoles(Collections.singleton(userRole));

        // Сохранение пользователя в базе данных
        userRepository.save(user);
    }


    @Override
    public void updateSeamstress(User newUser) {
        // Проверяем, существует ли швея с таким логином, исключая текущую швею
        if (userRepository.existsByUsernameAndIdNot(newUser.getUsername(), newUser.getId())) {
            throw new UsernameExistsException("Пользователь с логином " + newUser.getUsername() + " уже существует");
        }
        User oldUser = getSeamstressById(newUser.getId());
        oldUser.setName(newUser.getName());
        oldUser.setUsername(newUser.getUsername());
        oldUser.setHourlyRate(newUser.getHourlyRate());
        oldUser.setSalary(newUser.getSalary());
        if (!Objects.equals(newUser.getPassword(), "")){
            // Шифруем пароль перед сохранением в базу данных
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        // Обновляем данные о швее
        userRepository.save(oldUser);
    }


    @Override
    public void deleteSeamstress(Long id) {
        changeUserRole(id, 4L);
    }


    public void changeUserRole(Long userId, Long roleId) {
        // Шаг 1: Получить пользователя из базы данных
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Шаг 2: Получить новую роль из базы данных или создать новую роль
        Role newRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        // Шаг 3: Обновить коллекцию ролей пользователя
        Set<Role> roles = new HashSet<>();
        roles.add(newRole);
        user.setRoles(roles);

        // Шаг 4: Сохранить обновленного пользователя в базу данных
        userRepository.save(user);
    }

    @Override
    public User getSeamstressById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }



}
