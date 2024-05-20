package ru.vilas.sewing.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.config.UsernameExistsException;
import ru.vilas.sewing.model.User;
import ru.vilas.sewing.service.admin.AdminsService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Controller
@RequestMapping("/demigod")
public class SuperAdminController {

    private final AdminsService adminsService;

    public SuperAdminController(AdminsService adminsService) {
        this.adminsService = adminsService;
    }


    @GetMapping
    public String getAdmins(Model model,
                            @RequestParam(name = "passChanged", required = false) Boolean passChanged) {
        List<User> admins = adminsService.getAllAdmins();
        model.addAttribute("admins", admins);
        if (passChanged != null && passChanged) {
            model.addAttribute("success", "Пароль успешно сохранен");
        }
        return "admin/demigod";
    }

    @GetMapping("/new")
    public String showAddAdminForm(Model model) {
        model.addAttribute("admin", new User());
        return "admin/addAdmin";
    }

    @PostMapping("/new")
    public String addAdmin(@ModelAttribute User user, Model model) {
        try {
            adminsService.saveAdmin(user);
            return "redirect:/demigod";
        } catch (UsernameExistsException e) {
            // Если пользователь с таким логином уже существует
            model.addAttribute("user", user);
            model.addAttribute("error", "Админ с таким логином уже существует");
            return "admin/addAdmin"; // Возвращает на страницу добавления с сообщением об ошибке
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        adminsService.deleteAdmin(id);
        return "redirect:/demigod";
    }

    @GetMapping("/edit/{id}")
    public String editAdmin(@PathVariable Long id, Model model) {
        User admin = adminsService.getAdminById(id);
        model.addAttribute("admin", admin);
        return "admin/editAdmin";
    }

    @PostMapping("/edit")
    public String updateAdmin(@ModelAttribute User user, Model model) {

        try {
            adminsService.updateAdmin(user);
            return "redirect:/demigod";
        } catch (UsernameExistsException e) {
            // Если пользователь с таким логином уже существует
            model.addAttribute("admin", user);
            model.addAttribute("error", "Админ с таким логином уже существует");
            return "admin/editAdmin"; // Возвращает на страницу добавления с сообщением об ошибке
        }
    }

    @GetMapping("/chpas")
    public String changePassword() {
        return "admin/changePassword";
    }

    @PostMapping("/chpas")
    public String updatePass(@RequestParam("old_pass") String oldPass,
                             @RequestParam("new_pass") String newPass,
                             @RequestParam("new_pass_conf") String newPassConf,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        System.out.println(oldPass);

        if (!adminsService.checkPass(oldPass)) {
            model.addAttribute("error", "Неверный старый пароль");
            model.addAttribute("requestURI", "/demigod/chpas");
            return "admin/changePassword";
        }
        if (!newPass.equals(newPassConf)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("requestURI", "/demigod/chpas");
            return "admin/changePassword";
        }

        try {
            adminsService.changeSuperAdminPassword(newPass);
        } catch (Exception e){
            model.addAttribute("error", "Что-то пошло не так. Попробуйте еще раз.");
            model.addAttribute("requestURI", "/demigod/chpas");
            return "admin/changePassword";
        }

        // Добавляем параметр к объекту RedirectAttributes
        redirectAttributes.addAttribute("passChanged", true);

        // Делаем перенаправление на другую страницу
        return "redirect:/demigod";

    }
}
