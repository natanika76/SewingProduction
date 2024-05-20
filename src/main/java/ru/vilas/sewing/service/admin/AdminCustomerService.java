package ru.vilas.sewing.service.admin;

import ru.vilas.sewing.model.Customer;

import java.util.Collection;
import java.util.Set;

public interface AdminCustomerService {
    Set<Customer> getAllCustomers();

    void saveCustomer(Customer сustomer);

    Customer getCustomerById(Long id);

    void deleteCustomer(Long id);
}
