package com.switchfully.questboard.domain;

import com.switchfully.questboard.enums.AdventurerClass;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "adventurers")
public class Adventurer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "level")
    private int level;

    @Column(name = "xp_point")
    private int xpPoint;

    @Column(name = "recruitment_date")
    private LocalDateTime recruitmentDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "class")
    @Enumerated(EnumType.STRING)
    private AdventurerClass adventurerClass;

    protected Adventurer() {}

    public Adventurer(String name, int level, int xpPoint, User user, AdventurerClass adventurerClass) {
        this.name = name;
        this.level = level;
        this.xpPoint = xpPoint;
        this.user = user;
        this.adventurerClass = adventurerClass;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getXpPoint() {
        return xpPoint;
    }

    public LocalDateTime getRecruitmentDate() {
        return recruitmentDate;
    }

    public User getUser() {
        return user;
    }

    public AdventurerClass getAdventurerClass() {
        return adventurerClass;
    }
}
