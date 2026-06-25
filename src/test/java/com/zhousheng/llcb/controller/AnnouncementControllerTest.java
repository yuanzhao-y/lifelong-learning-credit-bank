package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhousheng.llcb.TestSecurity;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.entity.SysAnnouncement;
import com.zhousheng.llcb.mapper.SysAnnouncementMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {

    @Mock
    private SysAnnouncementMapper mapper;

    private AnnouncementController controller;

    @BeforeEach
    void setUp() {
        controller = new AnnouncementController(mapper);
        TestSecurity.loginAs(1L, "qa_admin", "admin");
    }

    @AfterEach
    void clear() {
        TestSecurity.clear();
    }

    @Test
    void sanitizesHistoricalHtmlOnPublicList() {
        SysAnnouncement announcement = announcement("enabled",
                "<p onclick=\"bad()\">safe</p><script>alert(1)</script>");
        Page<SysAnnouncement> page = new Page<>(1, 10);
        page.setRecords(List.of(announcement));
        when(mapper.selectPage(any(), any())).thenReturn(page);

        SysAnnouncement result = controller.publicList(1, 10, null).getData().getRecords().get(0);

        assertThat(result.getContent()).contains("<p>safe</p>").doesNotContain("script", "onclick");
    }

    @Test
    void rejectsDisabledPublicDetail() {
        when(mapper.selectById(9L)).thenReturn(announcement("disabled", "hidden"));

        assertThatThrownBy(() -> controller.publicDetail(9L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    private SysAnnouncement announcement(String status, String content) {
        SysAnnouncement announcement = new SysAnnouncement();
        announcement.setId(9L);
        announcement.setTitle("QA announcement");
        announcement.setStatus(status);
        announcement.setContent(content);
        return announcement;
    }
}
