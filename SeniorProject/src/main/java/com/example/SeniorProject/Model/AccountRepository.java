package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.*;

public interface AccountRepository extends JpaRepository<Account, Integer>
{
    @Query(" SELECT a FROM Account a WHERE a.email=?1")
    Account findAccountByToken(String token);
}
