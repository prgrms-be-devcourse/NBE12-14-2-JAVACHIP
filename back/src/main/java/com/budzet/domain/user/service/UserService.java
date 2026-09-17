package com.budzet.domain.user.service;

import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.repository.UserRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthTokenService authTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User join(String email, String password, String name) {
        if(userRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.USER_CONFLICT);
        }

        User user = new User(email, passwordEncoder.encode(password), name);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public void checkPassword(String inputPassword, String encodedPassword) {
        if(!passwordEncoder.matches(inputPassword, encodedPassword)){
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    public String genAccessToken(User user){
        return authTokenService.genAccessToken(user);
    }

    public Map<String, Object> payloadOrNull(String accessToken) {
        return authTokenService.payloadOrNull(accessToken);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
