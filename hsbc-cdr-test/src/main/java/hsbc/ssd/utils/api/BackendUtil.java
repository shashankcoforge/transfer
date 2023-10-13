package hsbc.ssd.utils.api;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class BackendUtil {

    public static String validateString(String value) {
        if (Objects.nonNull(value)) {
            return value.trim().toLowerCase();
        }
        return StringUtils.EMPTY;
    }

    public static String getMessage(String token, String hk, String basketID) {
        return token + "miss match for " + hk + "! basketID :" + basketID;
    }

    public static String validateStr(String value) {
        if (Objects.nonNull(value)) {
            return value.trim();
        }
        return StringUtils.EMPTY;
    }
}
