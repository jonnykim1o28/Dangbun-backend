package com.dangbun.domain.duty.entity;

import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "duty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Duty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duty_id")
    private Long dutyId;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DutyIcon icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @OneToMany(mappedBy = "duty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberDuty> memberDuties = new ArrayList<>();

    @Builder
    public Duty(String name, DutyIcon icon, Place place) {
        this.name = name;
        this.icon = icon;
        this.place = place;
    }

    public void update(String name, DutyIcon icon) {
        this.name = name;
        this.icon = icon;
    }

}
