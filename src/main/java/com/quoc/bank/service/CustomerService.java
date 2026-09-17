package com.quoc.bank.service;

import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateCustomerException;
import com.quoc.bank.model.Customer;
import com.quoc.bank.repository.CustomerRepository;

import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void createCustomer(Customer customer) {

        // 1. Kiểm tra customer không được null
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer không được null"
            );
        }

        // 2. Kiểm tra Customer ID
        if (customer.getCustomerId() == null ||
                customer.getCustomerId().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer ID không được để trống"
            );
        }

        // 3. Kiểm tra họ tên
        if (customer.getFullName() == null ||
                customer.getFullName().isBlank()) {

            throw new IllegalArgumentException(
                    "Họ tên khách hàng không được để trống"
            );
        }

        // 4. Kiểm tra Customer ID bị trùng
        if (customerRepository.existsById(
                customer.getCustomerId())) {

            throw new DuplicateCustomerException(
                    "Customer ID đã tồn tại: "
                            + customer.getCustomerId()
            );
        }

        // 5. Lưu customer
        customerRepository.save(customer);
    }

    public Customer getCustomer(String customerId) {

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Không tìm thấy khách hàng: "
                                        + customerId
                        )
                );
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public void deleteCustomer(String customerId) {

        // Kiểm tra customer có tồn tại hay không
        getCustomer(customerId);

        // Nếu tồn tại thì xóa
        customerRepository.deleteById(customerId);
    }
}