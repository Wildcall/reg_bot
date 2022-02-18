package ru.wildmazubot.model.entity.core;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "_phone")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false)
    private String number;

    @OneToOne(mappedBy = "phone")
    private Person person;
}
