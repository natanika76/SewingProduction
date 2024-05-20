package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.model.User;

import java.util.List;

public interface AdminsService {
    List<User> getAllAdmins();

    void saveAdmin(User user);

    void deleteAdmin(Long id);

    User getAdminById(Long id);

    void updateAdmin(User user);

    boolean checkPass(String oldPass);

    void changeSuperAdminPassword(String newPass);
}
