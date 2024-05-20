package ru.vilas.sewing.service.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.config.UsernameExistsException;
import ru.vilas.sewing.model.Role;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.repository.RoleRepository;
import ru.vilas.sewing.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminsServiceImpl implements AdminsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AdminsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllAdmins() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());
    }

    @Override
    public void saveAdmin(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameExistsException("Пользователь с логином " + user.getUsername() + " уже существует");
        }

        // Кодирование пароля перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Получение объекта роли "admin" из базы данных
        Optional<Role> optionalUserRole = roleRepository.findByName("ROLE_ADMIN");

        // Извлечение объекта роли из Optional
        Role userRole = optionalUserRole.orElseThrow(() -> new RuntimeException("Роль 'ADMIN' не найдена в базе данных"));

        // Присвоение роли "user" пользователю
        user.setRoles(Collections.singleton(userRole));

        // Сохранение админа в базе данных
        userRepository.save(user);
    }

    @Override
    public void deleteAdmin(Long id) {
        changeUserRole(id, 4L);
    }

    @Override
    public User getAdminById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Не найден пользователь с id: " + id));
    }

    @Override
    public void updateAdmin(User newAdmin) {
        // Проверяем, существует ли швея с таким логином, исключая текущую швею
        if (userRepository.existsByUsernameAndIdNot(newAdmin.getUsername(), newAdmin.getId())) {
            throw new UsernameExistsException("Пользователь с логином " + newAdmin.getUsername() + " уже существует");
        }
        User oldAdmin = getAdminById(newAdmin.getId());
        oldAdmin.setName(newAdmin.getName());
        oldAdmin.setUsername(newAdmin.getUsername());
        if (!Objects.equals(newAdmin.getPassword(), "")){
            // Шифруем пароль перед сохранением в базу данных
            oldAdmin.setPassword(passwordEncoder.encode(newAdmin.getPassword()));
        }

        // Обновляем данные об админе
        userRepository.save(oldAdmin);
    }

    @Override
    public boolean checkPass(String enteredOldPassword) {
        // Получить текущий хэшированный пароль из базы данных
        String hashedPasswordFromDatabase = userRepository.findPasswordByUsername("demigod")
                .orElseThrow(() -> new RuntimeException("Пароль не найден в базе данных"));

        // Проверка соответствия введенного старого пароля хэшированному паролю в базе данных
        return BCrypt.checkpw(enteredOldPassword, hashedPasswordFromDatabase);
    }

    public void changeUserRole(Long userId, Long roleId) {
        // Шаг 1: Получить пользователя из базы данных
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Не найден пользователь с id: " + userId));

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
    public void changeSuperAdminPassword(String newPassword) {
        User user = userRepository.findByUsername("demigod")
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Хешируем новый пароль
        String hashedNewPassword = passwordEncoder.encode(newPassword);

        // Устанавливаем новый пароль и сохраняем пользователя
        user.setPassword(hashedNewPassword);
        userRepository.save(user);
    }

}
