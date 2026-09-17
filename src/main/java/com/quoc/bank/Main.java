package com.quoc.bank;

import com.quoc.bank.exception.AccountNotFoundException;
import com.quoc.bank.exception.DuplicateCustomerException;
import com.quoc.bank.exception.InsufficientBalanceException;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.exception.InvalidTransactionException;
import com.quoc.bank.exception.TransactionLimitExceededException;
import com.quoc.bank.model.Account;
import com.quoc.bank.model.Customer;
import com.quoc.bank.model.PaymentAccount;
import com.quoc.bank.model.SavingAccount;
import com.quoc.bank.model.Transaction;
import com.quoc.bank.repository.AccountRepository;
import com.quoc.bank.repository.CustomerRepository;
import com.quoc.bank.repository.TransactionRepository;
import com.quoc.bank.service.AccountService;
import com.quoc.bank.service.CustomerService;
import com.quoc.bank.service.TransactionService;
import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateAccountException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // ==========================================
        // 1. Khởi tạo Repository
        // ==========================================

        CustomerRepository customerRepository =
                new CustomerRepository();

        AccountRepository accountRepository =
                new AccountRepository();

        TransactionRepository transactionRepository =
                new TransactionRepository();


        // ==========================================
        // 2. Khởi tạo Service
        // ==========================================

        CustomerService customerService =
                new CustomerService(customerRepository);

        AccountService accountService =
                new AccountService(accountRepository);

        TransactionService transactionService =
                new TransactionService(
                        accountService,
                        transactionRepository
                );


        // ==========================================
        // 3. Tạo Customer
        // ==========================================

        Customer customer1 = new Customer(
                "QUOC01",
                "Doan Quoc"
        );



        Customer customer2 = new Customer(
                "QUOC02",
                "Iron man"
        );

        customerService.createCustomer(customer1);
        customerService.createCustomer(customer2);

        System.out.println("=== DANH SACH KHACH HANG ===");

        for (Customer customer :
                customerService.getAllCustomers()) {

            System.out.println(customer);
        }


        // ==========================================
        // 4. Tạo Account
        // ==========================================

        Account account1 = new SavingAccount(
                "ABC001",
                customer1,
                new BigDecimal("10000000"),
                new BigDecimal("5000000")
        );

        Account account2 = new PaymentAccount(
                "ABC002",
                customer2,
                new BigDecimal("5000000"),
                new BigDecimal("3000000")
        );

        accountService.createAccount(account1);
        accountService.createAccount(account2);


        // ==========================================
        // 5. In thông tin Account
        // ==========================================

        System.out.println();
        System.out.println("=== DANH SACH TAI KHOAN ===");

        for (Account account :
                accountService.getAllAccounts()) {

            System.out.println(account);
        }


        // ==========================================
        // 6. Nạp tiền
        // ==========================================

        System.out.println();
        System.out.println("=== NAP TIEN ===");

        accountService.deposit(
                "ABC001",
                new BigDecimal("2000000")
        );

        System.out.println(
                "So du ABC001 sau khi nap: "
                        + account1.getBalance()
        );


        // ==========================================
        // 7. Chuyển tiền
        // ==========================================

        System.out.println();
        System.out.println("=== CHUYEN TIEN ===");

        transactionService.transfer(
                "ABC001",
                "ABC002",
                new BigDecimal("3000000")
        );

        System.out.println(
                "Chuyen tien thanh cong!"
        );


        // ==========================================
        // 8. Kiểm tra số dư
        // ==========================================

        System.out.println();
        System.out.println("=== SO DU SAU GIAO DICH ===");

        System.out.println(
                "ABC001: "
                        + account1.getBalance()
        );

        System.out.println(
                "ABC002: "
                        + account2.getBalance()
        );


        // ==========================================
        // 9. In lịch sử giao dịch
        // ==========================================

        System.out.println();
        System.out.println("=== LICH SU GIAO DICH ===");

        for (Transaction transaction :
                transactionService.getAllTransactions()) {

            System.out.println(
                    "Ma GD: "
                            + transaction.getRefNo()
                            + " | Tu: "
                            + transaction.getFromAccount()
                            .getAccountNumber()
                            + " | Den: "
                            + transaction.getToAccount()
                            .getAccountNumber()
                            + " | So tien: "
                            + transaction.getAmount()
                            + " | Trang thai: "
                            + transaction.getStatus()
            );
        }

// ==========================================
// MENU CHÍNH
// ==========================================

        while (true) {



                System.out.println();
                System.out.println("========================================");
                System.out.println("     QUAN LY GIAO DICH NGAN HANG");
                System.out.println("========================================");
                System.out.println("1. Them khach hang");
                System.out.println("2. Xem danh sach khach hang");
                System.out.println("3. Tao tai khoan");
                System.out.println("4. Xem danh sach tai khoan");
                System.out.println("5. Nap tien");
                System.out.println("6. Chuyen tien");
                System.out.println("7. Xem lich su giao dich");
                System.out.println("8. Xem giao dich SUCCESS");
                System.out.println("9. Xem giao dich FAILED");
                System.out.println("10. Tinh tong tien giao dich SUCCESS");
                System.out.println("11. Tim giao dich SUCCESS lon nhat");
                System.out.println("12. Sap xep giao dich theo so tien giam dan");
                System.out.println("13. Nhom giao dich theo tai khoan nguon");
                System.out.println("14. Tim giao dich theo ma refNo");
                System.out.println("0. Thoat");
                System.out.println("========================================");

            System.out.print("Chon: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Lua chon phai la so!");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

                switch (choice) {

                    case 1:

                        System.out.println();
                        System.out.println("=== THEM KHACH HANG ===");

                        System.out.print("Nhap Customer ID: ");
                        String customerId = scanner.nextLine();

                        System.out.print("Nhap ho ten: ");
                        String fullName = scanner.nextLine();

                        Customer customer = new Customer(
                                customerId,
                                fullName
                        );

                        try {

                            customerService.createCustomer(customer);

                            System.out.println(
                                    "Them khach hang thanh cong!"
                            );

                        } catch (DuplicateCustomerException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;

                    case 2:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("       DANH SACH KHACH HANG");
                        System.out.println("========================================");

                        for (Customer c :
                                customerService.getAllCustomers()) {

                            System.out.println(
                                    "Customer ID: "
                                            + c.getCustomerId()
                            );

                            System.out.println(
                                    "Ho ten     : "
                                            + c.getFullName()
                            );

                            System.out.println("----------------------------------------");
                        }

                        break;

                    case 3:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("           TAO TAI KHOAN");
                        System.out.println("========================================");

                        System.out.print("Nhap so tai khoan: ");
                        String accountNumberInput = scanner.nextLine();

                        System.out.print("Nhap Customer ID: ");
                        String ownerCustomerId = scanner.nextLine();

                        System.out.println();
                        System.out.println("Chon loai tai khoan:");
                        System.out.println("1. SAVING");
                        System.out.println("2. PAYMENT");
                        System.out.print("Lua chon: ");

                        int accountTypeChoice = scanner.nextInt();

                        System.out.print("Nhap so du ban dau: ");
                        BigDecimal initialBalance =
                                scanner.nextBigDecimal();

                        System.out.print("Nhap han muc giao dich trong ngay: ");
                        BigDecimal dailyLimit =
                                scanner.nextBigDecimal();

                        scanner.nextLine();

                        try {

                            // 1. Tìm Customer
                            Customer ownerCustomer =
                                    customerService.getCustomer(ownerCustomerId);

                            // 2. Tạo Account theo loại
                            Account newAccount;

                            if (accountTypeChoice == 1) {

                                newAccount = new SavingAccount(
                                        accountNumberInput,
                                        ownerCustomer,
                                        initialBalance,
                                        dailyLimit
                                );

                            } else if (accountTypeChoice == 2) {

                                newAccount = new PaymentAccount(
                                        accountNumberInput,
                                        ownerCustomer,
                                        initialBalance,
                                        dailyLimit
                                );

                            } else {

                                System.out.println(
                                        "Loai tai khoan khong hop le!"
                                );

                                break;
                            }

                            // 3. Lưu Account
                            accountService.createAccount(newAccount);

                            System.out.println();
                            System.out.println(
                                    "Tao tai khoan thanh cong!"
                            );

                            System.out.println(newAccount);

                        } catch (CustomerNotFoundException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (DuplicateAccountException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (InvalidAmountException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (IllegalArgumentException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;

                    case 4:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("          DANH SACH TAI KHOAN");
                        System.out.println("========================================");

                        for (Account account :
                                accountService.getAllAccounts()) {

                            System.out.println(
                                    "So tai khoan : "
                                            + account.getAccountNumber()
                            );

                            System.out.println(
                                    "Chu tai khoan: "
                                            + account.getOwner().getFullName()
                            );

                            System.out.println(
                                    "Loai         : "
                                            + account.getType()
                            );

                            System.out.println(
                                    "So du        : "
                                            + account.getBalance()
                            );

                            System.out.println(
                                    "Han muc/ngay  : "
                                            + account.getDailyLimit()
                            );

                            System.out.println(
                                    "Mo ta        : "
                                            + account.getDescription()
                            );

                            System.out.println(
                                    "Ngay tao     : "
                                            + account.getCreatedAt()
                            );

                            System.out.println("----------------------------------------");
                        }

                        break;

                    case 5:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("              NAP TIEN");
                        System.out.println("========================================");

                        System.out.print("Nhap so tai khoan: ");
                        String depositAccountNumber = scanner.nextLine();

                        System.out.print("Nhap so tien can nap: ");
                        BigDecimal depositAmount = scanner.nextBigDecimal();

                        scanner.nextLine();

                        try {

                            accountService.deposit(
                                    depositAccountNumber,
                                    depositAmount
                            );

                            Account account =
                                    accountService.getAccount(
                                            depositAccountNumber
                                    );

                            System.out.println();
                            System.out.println("Nap tien thanh cong!");
                            System.out.println(
                                    "So tai khoan: "
                                            + account.getAccountNumber()
                            );
                            System.out.println(
                                    "So tien nap : "
                                            + depositAmount
                            );
                            System.out.println(
                                    "So du moi   : "
                                            + account.getBalance()
                            );

                        } catch (AccountNotFoundException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (InvalidAmountException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;

                    case 6:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("            CHUYEN TIEN");
                        System.out.println("========================================");

                        System.out.print("Nhap tai khoan nguon: ");
                        String fromAccountNumber = scanner.nextLine();

                        System.out.print("Nhap tai khoan dich: ");
                        String toAccountNumber = scanner.nextLine();

                        System.out.print("Nhap so tien can chuyen: ");
                        BigDecimal transferAmount = scanner.nextBigDecimal();

                        scanner.nextLine();

                        try {

                            transactionService.transfer(
                                    fromAccountNumber,
                                    toAccountNumber,
                                    transferAmount
                            );

                            Account fromAccount =
                                    accountService.getAccount(
                                            fromAccountNumber
                                    );

                            Account toAccount =
                                    accountService.getAccount(
                                            toAccountNumber
                                    );

                            System.out.println();
                            System.out.println("Chuyen tien thanh cong!");
                            System.out.println("----------------------------------------");

                            System.out.println(
                                    "Tai khoan nguon: "
                                            + fromAccount.getAccountNumber()
                            );

                            System.out.println(
                                    "So du con lai  : "
                                            + fromAccount.getBalance()
                            );

                            System.out.println();

                            System.out.println(
                                    "Tai khoan dich : "
                                            + toAccount.getAccountNumber()
                            );

                            System.out.println(
                                    "So du moi      : "
                                            + toAccount.getBalance()
                            );

                            System.out.println("----------------------------------------");

                        } catch (AccountNotFoundException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (InvalidAmountException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (InsufficientBalanceException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (TransactionLimitExceededException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );

                        } catch (InvalidTransactionException e) {

                            System.out.println();
                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;




                    case 7:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("        LICH SU GIAO DICH");
                        System.out.println("========================================");

                        if (transactionService.getAllTransactions().isEmpty()) {

                            System.out.println(
                                    "Chua co giao dich nao."
                            );

                        } else {

                            for (Transaction tx :
                                    transactionService.getAllTransactions()) {

                                System.out.println(
                                        "Ma giao dich : "
                                                + tx.getRefNo()
                                );

                                System.out.println(
                                        "Tai khoan nguon: "
                                                + tx
                                                .getFromAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "Tai khoan dich : "
                                                + tx
                                                .getToAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "So tien       : "
                                                + tx.getAmount()
                                );

                                System.out.println(
                                        "Trang thai    : "
                                                + tx.getStatus()
                                );

                                System.out.println(
                                        "Thoi gian     : "
                                                + tx.getCreatedAt()
                                );

                                if (tx.getFailureReason() != null) {

                                    System.out.println(
                                            "Ly do that bai: "
                                                    + tx.getFailureReason()
                                    );
                                }

                                System.out.println("----------------------------------------");
                            }
                        }

                        break;

                    case 8:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("       GIAO DICH SUCCESS");
                        System.out.println("========================================");

                        if (transactionService
                                .getSuccessfulTransactions()
                                .isEmpty()) {

                            System.out.println(
                                    "Chua co giao dich SUCCESS."
                            );

                        } else {

                            for (Transaction tx :
                                    transactionService
                                            .getSuccessfulTransactions()) {

                                System.out.println(
                                        "Ma giao dich   : "
                                                + tx.getRefNo()
                                );

                                System.out.println(
                                        "Tai khoan nguon: "
                                                + tx.getFromAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "Tai khoan dich : "
                                                + tx.getToAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "So tien        : "
                                                + tx.getAmount()
                                );

                                System.out.println(
                                        "Trang thai     : "
                                                + tx.getStatus()
                                );

                                System.out.println(
                                        "Thoi gian      : "
                                                + tx.getCreatedAt()
                                );

                                System.out.println("----------------------------------------");
                            }
                        }

                        break;
                    case 9:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("        GIAO DICH FAILED");
                        System.out.println("========================================");

                        if (transactionService
                                .getFailedTransactions()
                                .isEmpty()) {

                            System.out.println(
                                    "Chua co giao dich FAILED."
                            );

                        } else {

                            for (Transaction tx :
                                    transactionService
                                            .getFailedTransactions()) {

                                System.out.println(
                                        "Ma giao dich   : "
                                                + tx.getRefNo()
                                );

                                System.out.println(
                                        "Tai khoan nguon: "
                                                + tx.getFromAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "Tai khoan dich : "
                                                + tx.getToAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "So tien        : "
                                                + tx.getAmount()
                                );

                                System.out.println(
                                        "Trang thai     : "
                                                + tx.getStatus()
                                );

                                System.out.println(
                                        "Thoi gian      : "
                                                + tx.getCreatedAt()
                                );

                                if (tx.getFailureReason() != null) {

                                    System.out.println(
                                            "Ly do that bai: "
                                                    + tx.getFailureReason()
                                    );
                                }

                                System.out.println("----------------------------------------");
                            }
                        }

                        break;

                    case 10:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("     TONG TIEN GIAO DICH SUCCESS");
                        System.out.println("========================================");

                        BigDecimal totalSuccessfulAmount =
                                transactionService.getTotalSuccessfulAmount();

                        System.out.println(
                                "Tong tien giao dich SUCCESS: "
                                        + totalSuccessfulAmount
                        );

                        break;

                    case 11:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("   GIAO DICH SUCCESS LON NHAT");
                        System.out.println("========================================");

                        try {

                            Transaction tx =
                                    transactionService
                                            .getLargestSuccessfulTransaction();

                            System.out.println(
                                    "Ma giao dich   : "
                                            + tx.getRefNo()
                            );

                            System.out.println(
                                    "Tai khoan nguon: "
                                            + tx.getFromAccount()
                                            .getAccountNumber()
                            );

                            System.out.println(
                                    "Tai khoan dich : "
                                            + tx.getToAccount()
                                            .getAccountNumber()
                            );

                            System.out.println(
                                    "So tien        : "
                                            + tx.getAmount()
                            );

                            System.out.println(
                                    "Trang thai     : "
                                            + tx.getStatus()
                            );

                            System.out.println(
                                    "Thoi gian      : "
                                            + tx.getCreatedAt()
                            );

                        } catch (InvalidTransactionException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;

                    case 12:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("   GIAO DICH GIAM DAN THEO SO TIEN");
                        System.out.println("========================================");

                        List<Transaction> sortedTransactions =
                                transactionService
                                        .getTransactionsSortedByAmountDesc();

                        if (sortedTransactions.isEmpty()) {

                            System.out.println(
                                    "Chua co giao dich."
                            );

                        } else {

                            for (Transaction tx : sortedTransactions) {

                                System.out.println(
                                        "Ma giao dich   : "
                                                + tx.getRefNo()
                                );

                                System.out.println(
                                        "Tai khoan nguon: "
                                                + tx.getFromAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "Tai khoan dich : "
                                                + tx.getToAccount()
                                                .getAccountNumber()
                                );

                                System.out.println(
                                        "So tien        : "
                                                + tx.getAmount()
                                );

                                System.out.println(
                                        "Trang thai     : "
                                                + tx.getStatus()
                                );

                                System.out.println(
                                        "Thoi gian      : "
                                                + tx.getCreatedAt()
                                );

                                System.out.println("----------------------------------------");
                            }
                        }

                        break;

                    case 13:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("   NHOM GIAO DICH THEO TAI KHOAN NGUON");
                        System.out.println("========================================");

                        Map<String, List<Transaction>> groupedTransactions =
                                transactionService
                                        .groupTransactionsByFromAccount();

                        if (groupedTransactions.isEmpty()) {

                            System.out.println(
                                    "Chua co giao dich."
                            );

                        } else {

                            for (Map.Entry<String, List<Transaction>> entry :
                                    groupedTransactions.entrySet()) {

                                System.out.println();
                                System.out.println(
                                        "Tai khoan nguon: "
                                                + entry.getKey()
                                );

                                System.out.println(
                                        "So giao dich: "
                                                + entry.getValue().size()
                                );

                                for (Transaction tx : entry.getValue()) {

                                    System.out.println(
                                            "  - "
                                                    + tx.getRefNo()
                                                    + " | "
                                                    + tx.getFromAccount()
                                                    .getAccountNumber()
                                                    + " -> "
                                                    + tx.getToAccount()
                                                    .getAccountNumber()
                                                    + " | "
                                                    + tx.getAmount()
                                                    + " | "
                                                    + tx.getStatus()
                                    );
                                }
                            }
                        }

                        break;

                    case 14:

                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("       TIM GIAO DICH THEO REFNO");
                        System.out.println("========================================");

                        System.out.print("Nhap ma giao dich refNo: ");
                        String refNo = scanner.nextLine();

                        try {

                            Transaction tx =
                                    transactionService
                                            .getTransactionByRefNo(refNo);

                            System.out.println();
                            System.out.println(
                                    "Ma giao dich   : "
                                            + tx.getRefNo()
                            );

                            System.out.println(
                                    "Tai khoan nguon: "
                                            + tx.getFromAccount()
                                            .getAccountNumber()
                            );

                            System.out.println(
                                    "Tai khoan dich : "
                                            + tx.getToAccount()
                                            .getAccountNumber()
                            );

                            System.out.println(
                                    "So tien        : "
                                            + tx.getAmount()
                            );

                            System.out.println(
                                    "Trang thai     : "
                                            + tx.getStatus()
                            );

                            System.out.println(
                                    "Thoi gian      : "
                                            + tx.getCreatedAt()
                            );

                            if (tx.getFailureReason() != null) {

                                System.out.println(
                                        "Ly do that bai: "
                                                + tx.getFailureReason()
                                );
                            }

                        } catch (InvalidTransactionException e) {

                            System.out.println(
                                    "Loi: " + e.getMessage()
                            );
                        }

                        break;



                    case 0:
                        System.out.println("Cam on ban da su dung!");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Lua chon khong hop le!");
                }
            }
        }
    }
