package com.zhousheng.llcb.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSecurityTest {

    @Test
    void removesScriptsAndEventHandlers() {
        String cleaned = HtmlSecurity.announcement("<p onclick=\"steal()\">hello</p><script>alert(1)</script>");

        assertThat(cleaned).contains("<p>hello</p>");
        assertThat(cleaned).doesNotContain("onclick", "script", "alert");
    }

    @Test
    void removesDangerousProtocols() {
        String cleaned = HtmlSecurity.announcement("<a href=\"javascript:alert(1)\">open</a>");

        assertThat(cleaned).isEqualTo("<a>open</a>");
    }

    @Test
    void keepsSafeRichText() {
        String cleaned = HtmlSecurity.announcement("<h2>Title</h2><p><strong>Safe</strong></p>");

        assertThat(cleaned).contains("<h2>Title</h2>", "<strong>Safe</strong>");
    }
}
