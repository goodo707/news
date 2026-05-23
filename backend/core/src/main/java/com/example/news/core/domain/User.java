package com.example.news.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "push_type", nullable = false)
    private String pushType;

    @Column(name = "dnd_start")
    private String dndStart;

    @Column(name = "dnd_end")
    private String dndEnd;

    public User(Long id, String name, String deviceId, String pushType,
                String dndStart, String dndEnd) {
        this.id = id;
        this.name = name;
        this.deviceId = deviceId;
        this.pushType = pushType;
        this.dndStart = dndStart;
        this.dndEnd = dndEnd;
    }
}
