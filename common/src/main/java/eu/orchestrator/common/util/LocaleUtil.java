package eu.orchestrator.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public final class LocaleUtil {

    public static String fetchLocale(HttpServletRequest request) {

        String locale = null;
        if (null != request && null != request.getCookies() && request.getCookies().length > 0) {
            for (Cookie cookie : request.getCookies()) {
                //org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE
                if (cookie.getName().contains("CookieLocaleResolver")) {
                    locale = cookie.getValue();
                }
            }
        }
        return locale;
    }

}
