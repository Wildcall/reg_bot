package ru.wildmazubot.entity.core;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "_passport")
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "second_name", nullable = false)
    private String secondName;

    @Column(name = "middle_name", nullable = false)
    private String middleName;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthDay;

    @Column(name = "number_series", nullable = false, unique = true)
    private Long numberSeries;

    @OneToOne(mappedBy = "passport")
    private Person person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Passport passport = (Passport) o;
        return id != null
                && Objects.equals(id, passport.id)
                && Objects.equals(numberSeries, passport.numberSeries);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
