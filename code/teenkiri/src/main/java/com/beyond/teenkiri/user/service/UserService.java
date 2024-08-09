package com.beyond.teenkiri.user.service;

import com.beyond.teenkiri.chatting.service.ChatService;
import com.beyond.teenkiri.common.domain.DelYN;
import com.beyond.teenkiri.qna.domain.QnA;
import com.beyond.teenkiri.qna.dto.QnAListResDto;
import com.beyond.teenkiri.qna.repository.QnARepository;

import com.beyond.teenkiri.user.config.JwtTokenprovider;
import com.beyond.teenkiri.user.domain.User;
import com.beyond.teenkiri.user.dto.*;
import com.beyond.teenkiri.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service("userService")
public class UserService {

    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Autowired
    //private JwtTokenProvider jwtTokenProvider;
    private JwtTokenprovider jwtTokenprovider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private QnARepository qnARepository;

    public String login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("잘못된 이메일/비밀번호 입니다."));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("잘못된 이메일/비밀번호 입니다.");
        }

        return jwtTokenprovider.createToken(user.getEmail(), user.getRole().name());
    }

    public String getEmailFromToken(String token) {
        return jwtTokenprovider.getEmailFromToken(token);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public String findId(UserFindIdDto findIdDto) {
        User user = userRepository.findByNameAndPhone(findIdDto.getName(), findIdDto.getPhone())
                .orElseThrow(() -> new RuntimeException("없는 사용자 입니다."));
        return maskEmail(user.getEmail());
    }


    private String maskEmail(String email) {
        return email.substring(0, 4) + "******" + email.substring(email.indexOf("@"));
    }

    public void sendPasswordResetLink(UserFindPasswordDto findPasswordDto) {
        User user = userRepository.findByNameAndPhoneAndEmail(findPasswordDto.getName(), findPasswordDto.getPhone(), findPasswordDto.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 확인해주세요."));

        String resetToken = jwtTokenprovider.createToken(user.getEmail(), user.getRole().name());
        redisService.saveVerificationCode(findPasswordDto.getEmail(), resetToken);

        String resetLink = "http://localhost:8088/user/reset-password?token=" + resetToken;
        emailService.sendSimpleMessage(findPasswordDto.getEmail(), "비밀번호 재설정", "비밀번호 재설정 링크: " + resetLink);
    }

    public void resetPassword(PasswordResetDto passwordResetDto) {
        String email = jwtTokenprovider.getEmailFromToken(passwordResetDto.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 확인해주세요."));

        validatePassword(passwordResetDto.getNewPassword(), passwordResetDto.getConfirmPassword(), user.getPassword());

        user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        userRepository.save(user);
    }

    private void validatePassword(String newPassword, String confirmPassword, String currentPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("동일하지 않은 비밀번호 입니다.");
        }

        if (newPassword.length() <= 7) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (passwordEncoder.matches(newPassword, currentPassword)) {
            throw new RuntimeException("이전과 동일한 비밀번호로 설정할 수 없습니다.");
        }
    }

    public void register(UserSaveReqDto saveReqDto) {
        validateRegistration(saveReqDto);

        User user = saveReqDto.toEntity(passwordEncoder.encode(saveReqDto.getPassword()));
        userRepository.save(user);
    }

    private void validateRegistration(UserSaveReqDto saveReqDto) {
        if (saveReqDto.getPassword().length() <= 7) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (userRepository.existsByEmail(saveReqDto.getEmail())) {
            throw new RuntimeException("이미 사용중인 이메일 입니다.");
        }

        String filteredNickname = chatService.filterMessage(saveReqDto.getNickname());
        if (!filteredNickname.equals(saveReqDto.getNickname())) {
            throw new RuntimeException("비속어는 닉네임으로 설정할 수 없습니다.");
        }
    }

    public void sendVerificationEmail(String email) {
        String code = generateVerificationCode();
        redisService.saveVerificationCode(email, code);
        emailService.sendSimpleMessage(email, "이메일 인증 코드", "인증 코드: " + code);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9999 - 1000 + 1) + 1000;
        return String.valueOf(code);
    }

    public boolean verifyEmail(String email, String code) {
        if (redisService.verifyCode(email, code)) {
            return true;
        } else {
            throw new RuntimeException("인증 코드가 유효하지 않습니다.");
        }
    }

    public boolean checkNickname(String nickname) {
        String filteredNickname = chatService.filterMessage(nickname);
        if (!filteredNickname.equals(nickname)) {
            throw new RuntimeException("비속어는 닉네임으로 설정할 수 없습니다.");
        }

        return !userRepository.existsByNickname(nickname);
    }

    public void deleteAccount(String token) {
        String email = jwtTokenprovider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 확인할 수 없습니다: " + email));

        user.setDelYN(DelYN.Y);
        user.setNickname("<알수없음>" + System.currentTimeMillis());
        userRepository.save(user);
    }

    public User getUserFromToken(String token) {
        String email = jwtTokenprovider.getEmailFromToken(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }


    private void updateUserDetails(User user, UserEditReqDto editReqDto) {
        if (editReqDto.getPassword() != null && !editReqDto.getPassword().isEmpty()) {
            validatePassword(editReqDto.getPassword(), editReqDto.getConfirmPassword(), user.getPassword());
            user.setPassword(passwordEncoder.encode(editReqDto.getPassword()));
        }

        if (editReqDto.getNickname() != null && !editReqDto.getNickname().isEmpty()) {
            String filteredNickname = chatService.filterMessage(editReqDto.getNickname());
            if (!filteredNickname.equals(editReqDto.getNickname())) {
                throw new RuntimeException("비속어는 닉네임으로 설정할 수 없습니다.");
            }
            if (userRepository.existsByNickname(editReqDto.getNickname())) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(editReqDto.getNickname());
        }

        if (editReqDto.getAddress() != null) {
            user.setAddress(editReqDto.getAddress());
        }
    }

    public List<QnAListResDto> getUserQnAList(String userEmail) {
        User user = findByEmail(userEmail);
        List<QnA> qnaList = qnARepository.findByUser(user);
        List<QnAListResDto> qnaListResDtos = new ArrayList<>();

        for (QnA qna : qnaList) {
            qnaListResDtos.add(qna.listFromEntity());
        }

        return qnaListResDtos;
    }


    public void updateUserInfo(String username, UserEditReqDto editReqDto) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        updateUserDetails(user, editReqDto);
        userRepository.save(user);
    }

}
