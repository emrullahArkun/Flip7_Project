package com.flavia.domain.enums;

public enum CardType {
    NUMBER,
    FREEZE,
    FLIP_THREE,
    SECOND_CHANCE;

    public boolean isNumber() {
        return this == NUMBER;
    }

    public boolean isAction() {
        return this != NUMBER;
    }
}
