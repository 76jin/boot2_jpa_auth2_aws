package com.ranian.book.springboot.config.auth;

import com.ranian.book.springboot.config.auth.dto.SessionUser;
import com.ranian.book.springboot.config.dto.OAuthAttributes;
import com.ranian.book.springboot.domain.user.User;
import com.ranian.book.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

/**
 * 구글 로그인 이후 사용자 정보 기반으로 가입 및 정보수정, 세션 저장 등 기능 지원
 * - 사용자 정보: name, email, picture)
 * - 연동 SNS: 구글
 */
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    /**
     * OAuth2User: 인증된 사용자 객체
     * registrationId: 서비스 구분 코드
     * userNameAttributeName: OAuth2 로그인 진행 시, 키가 되는 필드값 (private key와 같은 의미)
     *   - 구글의 기본코드: sub
     *   - 네이버, 카카오 기본 코드: 없음
     * OAuthAttributes: 사용자 인증 객체의 속성 저장용 DTO 객체
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entry -> entry.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntry());

        return userRepository.save(user);
    }
}
