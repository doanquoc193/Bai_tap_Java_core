package com.quoc.bank.repository;

import com.quoc.bank.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    private final List<Customer> customers = new ArrayList<>();

    // Thêm customer
    public void save(Customer customer) {
        customers.add(customer);
    }

    // Tìm customer theo ID
    public Optional<Customer> findById(String customerId) {

        return customers.stream()
                .filter(customer ->
                        customer.getCustomerId()
                                .equals(customerId))
                .findFirst();
    }

    // Kiểm tra customer có tồn tại hay không
    public boolean existsById(String customerId) {

        return customers.stream()
                .anyMatch(customer ->
                        customer.getCustomerId()
                                .equals(customerId));
    }

    // Lấy tất cả customer
    public List<Customer> findAll() {
        return customers;
    }

    // Xóa customer theo ID
    public void deleteById(String customerId) {

        customers.removeIf(customer ->
                customer.getCustomerId()
                        .equals(customerId));
    }
}