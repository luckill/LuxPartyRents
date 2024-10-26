package com.example.SeniorProject.Model;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

public interface AccountRepository extends JpaRepository<Account, Integer>
{
    @Query(" SELECT a FROM Account a WHERE a.email=?1")
    Account findAccountByEmail(String Email);

    @Query(" SELECT a FROM Account a WHERE a.id=?1")
    List<Account> getAccountById(int id);
    
    @Transactional
    @Modifying
    @Query(" DELETE FROM Customer c WHERE c.id=?1")
    void deleteById(int id);

    @Query("SELECT a FROM Account a WHERE a.verified = false")
    List<Account> findUnverifiedAccounts();

    @Transactional
    @Modifying
    @Query("DELETE FROM Account a WHERE a.verified = false")
    void deleteAllUnverifiedAccounts();


}
