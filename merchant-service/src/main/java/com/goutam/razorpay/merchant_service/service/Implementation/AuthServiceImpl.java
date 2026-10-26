package com.goutam.razorpay.merchant_service.service.Implementation;


import com.goutam.razorpay.common_lib.enums.MerchantStatus;
import com.goutam.razorpay.common_lib.enums.UserRole;
import com.goutam.razorpay.common_lib.exception.BusinessRuleViolationException;
import com.goutam.razorpay.common_lib.exception.DuplicateResourceException;
import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.merchant_service.dto.request.LoginRequest;
import com.goutam.razorpay.merchant_service.dto.request.MerchantSignupRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.LoginResponse;
import com.goutam.razorpay.merchant_service.dto.response.MerchantResponseDto;
import com.goutam.razorpay.merchant_service.entity.AppUser;
import com.goutam.razorpay.merchant_service.entity.Merchant;
import com.goutam.razorpay.merchant_service.mapper.MerchantMapper;
import com.goutam.razorpay.merchant_service.repository.AppUserRepository;
import com.goutam.razorpay.merchant_service.repository.MerchantRepository;
import com.goutam.razorpay.merchant_service.security.JwtUtil;
import com.goutam.razorpay.merchant_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Override
    public MerchantResponseDto signup(MerchantSignupRequestDto request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: " + request.email());
        }

        Merchant merchant = merchantMapper.toEntityFromSignupRequestDto(request);
       merchant.setStatus(MerchantStatus.PENDINGKYC);
       merchant = merchantRepository.save(merchant);

       AppUser appUser = AppUser.builder()
               .email(request.email())
               .passwordHash(passwordEncoder.encode(request.password()))
               .merchant(merchant)
               .role(UserRole.OWNER)
               .build();
       appUserRepository.save(appUser);
        return merchantMapper.toResponseDto(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {


        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));
        if (!passwordEncoder.matches(request.password(), appUser.getPasswordHash())) {
            throw new BusinessRuleViolationException("INVALID_CREDENTIALS", "Invalid email or password");
        }
        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().toString());

        return new LoginResponse(token);
    }
}
