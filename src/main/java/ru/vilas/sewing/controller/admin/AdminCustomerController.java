package ru.vilas.sewing.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.service.admin.AdminCustomerService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public String showCustomers(Model model) {
        List<Customer> customers = adminCustomerService.getAllCustomers()
                .stream()
                .filter(Customer::isActive) // Фильтрация по активным заказчикам
                .collect(Collectors.toList());
        model.addAttribute("customers", customers);
        return "admin/customersList";
    }

    // Добавление нового заказчика
    @GetMapping("/new")
    public String showNewCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "admin/addCustomer";
    }

    @PostMapping("/new")
    public String addCustomer(@ModelAttribute Customer сustomer) {
        adminCustomerService.saveCustomer(сustomer);
        return "redirect:/admin/customers";
    }

    // Редактирование
    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Long id, Model model) {
        Customer customer = adminCustomerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        return "admin/editCustomer";
    }

    @PostMapping("/edit")
    public String updateCustomer(@ModelAttribute Customer customer) {
        adminCustomerService.saveCustomer(customer);
        return "redirect:/admin/customers";
    }

    // Удаление
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        adminCustomerService.deleteCustomer(id);
        return "redirect:/admin/customers";
    }
}
