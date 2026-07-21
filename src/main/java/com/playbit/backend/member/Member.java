package com.playbit.backend.member;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    public Member(String memberUuid) {
        this.memberUuid = memberUuid;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true)
    private String memberUuid;

}