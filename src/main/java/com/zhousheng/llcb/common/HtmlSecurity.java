package com.zhousheng.llcb.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.util.StringUtils;

public final class HtmlSecurity {

    private static final Safelist ANNOUNCEMENT_SAFELIST = Safelist.relaxed()
            .addAttributes("a", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    private HtmlSecurity() {
    }

    public static String announcement(String html) {
        if (!StringUtils.hasText(html)) {
            return html;
        }
        return Jsoup.clean(html, ANNOUNCEMENT_SAFELIST);
    }
}
