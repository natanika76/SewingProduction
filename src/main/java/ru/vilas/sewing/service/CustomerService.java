package ru.vilas.sewing.service;

import ru.vilas.sewing.model.Customer;

import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Task;

import java.util.List;

public interface CustomerService {
    List<Customer> getAllCustomers();

    Customer getCustomerById(Long customerId);

    void saveCustomer(Customer  customer);

    void deleteCustomer(Long id);
}


