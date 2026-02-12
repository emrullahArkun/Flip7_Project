package com.flavia.domain.model;


import com.flavia.domain.enums.CardType;

public record Card(int value, CardType type) {

    public boolean isNumber() {
        return type == CardType.NUMBER;
    }

    public boolean isAction() {
        return type != CardType.NUMBER;
    }

    @Override
    public String toString() {
        return isNumber() ? String.valueOf(value) : type.name();
    }
}