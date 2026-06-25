package com.zhousheng.llcb.service;

import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.mapper.SysFileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TosStorageServiceTest {

    @Mock
    private SysFileMapper fileMapper;

    private TosStorageService service;

    @BeforeEach
    void setUp() {
        TosProperties tos = new TosProperties("cn-beijing", "https://tos.example",
                "bucket", "https://bucket.example", "llcb/", "missing-access-key.txt");
        UploadProperties upload = new UploadProperties(8,
                List.of("png", "pdf"), List.of("image/png", "application/pdf"));
        service = new TosStorageService(tos, upload, fileMapper);
    }

    @Test
    void rejectsEmptyFileBeforeReadingCredentials() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.upload("avatar", 1L, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能");
    }

    @Test
    void rejectsOversizedFileBeforeReadingCredentials() {
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", new byte[9]);

        assertThatThrownBy(() -> service.upload("avatar", 1L, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("大小");
    }

    @Test
    void rejectsDisallowedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "../payload.exe",
                "application/octet-stream", new byte[]{1});

        assertThatThrownBy(() -> service.upload("avatar", 1L, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("扩展");
    }

    @Test
    void rejectsDisallowedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png",
                "text/html", new byte[]{1});

        assertThatThrownBy(() -> service.upload("avatar", 1L, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("类型");
    }
}
