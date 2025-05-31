package com.sean.lightrpc.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMessageStatusEnum {
    OK("Ok", 20),
    BAD_REQUEST("BadRequest", 40),
    BAD_RESPONSE("BadResponse", 50);

    private final String text;

    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum anEnum : ProtocolMessageStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
