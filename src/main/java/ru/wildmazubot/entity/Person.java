package ru.wildmazubot.entity;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Data
public class Person {

    private Long id;
    private String firstName;
    private String secondName;
    private String middleName;
    private String birthDay;
    private String passport;
    private Long userId;

    @Getter
    private static Map<Integer, String> fieldName = new HashMap<>(){{
        put(0, "Фамилия ( Ivanov ):");
        put(1, "Имя ( Ivan ):");
        put(2, "Отчество ( Ivanovich ):");
        put(3, "Дата рождения ( dd.mm.yyyy ):");
        put(4, "Паспорт ( серия и номер ):");

    }};

    public int addProperty(String text) {
        if (firstName == null) {
            firstName = text;
            return 1;
        }
        if (secondName == null) {
            secondName = text;
            return 2;
        }
        if (middleName == null) {
            middleName = text;
            return 3;
        }
        if (birthDay == null) {
            birthDay = text;
            return 4;
        }
        if (passport == null) {
            passport = text;
        }
        return 5;
    }

    @Override
    public String toString() {
        return firstName + "\n"
                + secondName + "\n"
                + middleName + "\n"
                + birthDay + "\n"
                + passport;
    }

    public String toShortString() {
        return firstName + " " + secondName + "" + middleName;
    }
}
