package ru.wildmazubot.model.entity.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import ru.wildmazubot.model.entity.UserRole;
import ru.wildmazubot.model.entity.UserStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "_user")
public class User {

    @Id
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private User referrer;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private User operator;

    @ToString.Exclude
    @OneToMany(
            mappedBy = "referrer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<User> referralUsers;

    @ToString.Exclude
    @OneToMany(
            mappedBy = "operator",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<User> processedUsers;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "email_id")
    private Email email;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passport_id")
    private Passport passport;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "phone_id")
    private Phone phone;

    public User(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
