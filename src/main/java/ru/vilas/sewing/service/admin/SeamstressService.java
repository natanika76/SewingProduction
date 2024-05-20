package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.model.User;

import java.util.List;

public interface SeamstressService {
    List<User> getAllSeamstresses();

    void saveSeamstress(User user);

    void deleteSeamstress(Long id);

    User getSeamstressById(Long id);

    void updateSeamstress(User user);
}
