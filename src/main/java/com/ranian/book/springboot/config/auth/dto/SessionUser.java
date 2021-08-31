package com.ranian.book.springboot.config.auth.dto;

import com.ranian.book.springboot.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

/**
 * 인증된 사용자 정보를 위한 DTO 객체
 * - 세션에 저장용
 * - 기존의 User 객체는 직렬화 기능이 없어서, 직렬화 기능을 가진 세션 DTO 객체를 만듬.
 * - 기존의 User 객체에 직렬화를 추가하면, 엔티티이기 때문에 OneToMany 등 자식 엔티티가 있으면 직렬화 시, 성능 이슈, 부수 효과 등이 발생할 수 있음.
 */
@Getter
public class SessionUser implements Serializable {

    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
