package com.example.SeniorProject.Model;

import jakarta.transaction.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long>
{
    boolean existsByToken(String token);

    @Query("SELECT bt FROM BlacklistedToken bt WHERE bt.expiredAt < :now")
    List<BlacklistedToken> findExpiredBlacklistTokens(LocalDateTime now);
}