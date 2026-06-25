package com.zhousheng.llcb.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvSecurityTest {

    @Test
    void neutralizesSpreadsheetFormulaPrefixes() {
        assertThat(CsvSecurity.cell("=SUM(A1:A2)")).isEqualTo("\"'=SUM(A1:A2)\"");
        assertThat(CsvSecurity.cell("+cmd")).isEqualTo("\"'+cmd\"");
        assertThat(CsvSecurity.cell("@lookup")).isEqualTo("\"'@lookup\"");
    }

    @Test
    void escapesQuotesCommasAndLineBreaks() {
        assertThat(CsvSecurity.cell("a,\"b\"\nnext")).isEqualTo("\"a,\"\"b\"\"\nnext\"");
    }

    @Test
    void handlesNullAndOrdinaryValues() {
        assertThat(CsvSecurity.cell(null)).isEmpty();
        assertThat(CsvSecurity.cell(7L)).isEqualTo("\"7\"");
    }
}
