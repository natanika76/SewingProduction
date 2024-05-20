package ru.vilas.sewing.service.admin;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.model.Category;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.repository.CustomerRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final CustomerRepository customerRepository;

    public AdminCustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Set<Customer> getAllCustomers() {
        return customerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .filter(Customer::isActive).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if(customer != null){
            customer.setActive(false);
            customerRepository.save(customer);
        }
    }
}
