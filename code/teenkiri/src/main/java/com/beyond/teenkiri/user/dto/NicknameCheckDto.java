package com.beyond.teenkiri.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NicknameCheckDto {
    private String nickname;

    public NicknameCheckDto(String nickname) {
        this.nickname = nickname;
    }
}
