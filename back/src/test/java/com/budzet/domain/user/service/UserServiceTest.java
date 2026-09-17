package com.budzet.domain.user.service;

import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.repository.UserRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 가입 성공")
    void join_success() {

        String email = "user1@test.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String name = "user1";

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(passwordEncoder.encode(password))
                .thenReturn(encodedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result =
                userService.join(email, password, name);

        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("회원 가입, 이미 존재하는 회원")
    void join_userAlreadyExists() {

        String email = "user1@test.com";
        String password = "password123";
        String name = "user1";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.join(email, password, name)
        );

        assertEquals(
                ErrorCode.USER_CONFLICT,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("비밀번호 일치")
    void checkPassword_success() {

        String inputPassword = "password123";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.matches(inputPassword, encodedPassword))
                .thenReturn(true);

        userService.checkPassword(
                inputPassword,
                encodedPassword
        );

        verify(passwordEncoder)
                .matches(inputPassword, encodedPassword);
    }

    @Test
    @DisplayName("비밀번호 불일치")
    void checkPassword_mismatch() {

        String inputPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.matches(inputPassword, encodedPassword))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkPassword(
                        inputPassword,
                        encodedPassword
                )
        );

        assertEquals(
                ErrorCode.PASSWORD_MISMATCH,
                exception.getErrorCode()
        );
    }
}