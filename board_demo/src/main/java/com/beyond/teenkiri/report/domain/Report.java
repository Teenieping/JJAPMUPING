package com.beyond.teenkiri.report.domain;

import com.beyond.teenkiri.common.domain.BaseTimeEntity;
import com.beyond.teenkiri.common.domain.DelYN;
import com.beyond.teenkiri.user.domain.User;

import javax.persistence.*;

public class Report extends BaseTimeEntity {
    private Long id;

    @Column(length = 3000)
    private String report_log;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('N', 'Y') DEFAULT 'N'")
    private DelYN delYN;

//    '욕설', '도배', '광고', '기타' 순
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('abusive', 'wallpapers', 'advertising', 'more') DEFAULT 'N'", nullable = false)
    private Reason reason;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}