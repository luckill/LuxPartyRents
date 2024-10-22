package com.example.SeniorProject.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.SeniorProject.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.server.*;

import java.time.LocalDateTime;
import java.time.temporal.*;
import java.util.*;

public class JwtTokenBlacklistServiceTest
{

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtTokenBlacklistService jwtTokenBlacklistService;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBlacklistToken_ValidToken() {
        String token = "validToken";
        long remainingValidTime = 3600L; // 1 hour
        when(jwtService.getRemainingValidTime(token)).thenReturn(remainingValidTime);

        LocalDateTime createDate = LocalDateTime.now();
        jwtTokenBlacklistService.blacklistToken(token);

        ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(captor.capture());

        BlacklistedToken savedToken = captor.getValue();
        assertThat(savedToken.getToken()).isEqualTo(token);
        assertThat(savedToken.getCreateAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(createDate.truncatedTo(ChronoUnit.SECONDS)); // Ignore nanoseconds
        assertThat(savedToken.getExpiredAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(createDate.plusSeconds(remainingValidTime).truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    public void testIsTokenBlacklisted_TokenExists()
    {
        String token = "existingToken";
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(true);
        boolean result = jwtTokenBlacklistService.isTokenBlacklisted(token);
        assertThat(result).isTrue();
    }

    @Test
    void testDeleteExpiredTokens_TokensPresent() {
        // Given
        BlacklistedToken token1 = new BlacklistedToken("token1", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        BlacklistedToken token2 = new BlacklistedToken("token2", LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        List<BlacklistedToken> expiredTokens = Arrays.asList(token1, token2);

        // Mock the behavior of the repository
        when(blacklistedTokenRepository.findExpiredBlacklistTokens(any(LocalDateTime.class))).thenReturn(expiredTokens);

        // When
        jwtTokenBlacklistService.deleteExpiredTokens(LocalDateTime.now());

        // Then
        verify(blacklistedTokenRepository).deleteAll(expiredTokens);
    }

    @Test
    void testDeleteExpiredTokens_NoTokensPresent() {
        // Given
        when(blacklistedTokenRepository.findExpiredBlacklistTokens(LocalDateTime.now())).thenReturn(Collections.emptyList());

        // When / Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                jwtTokenBlacklistService.deleteExpiredTokens(LocalDateTime.now())
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No expire tokens found", exception.getReason());
    }
}
