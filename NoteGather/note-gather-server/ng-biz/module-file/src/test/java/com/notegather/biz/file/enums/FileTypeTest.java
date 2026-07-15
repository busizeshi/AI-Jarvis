package com.notegather.biz.file.domain.enums;

import com.notegather.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileTypeTest {

    @Test
    void shouldResolveSupportedExtensionsIgnoringCase() {
        assertThat(FileType.fromFileName("paper.PDF")).isEqualTo(FileType.PDF);
        assertThat(FileType.fromFileName("memo.txt")).isEqualTo(FileType.TXT);
        assertThat(FileType.fromFileName("README.md")).isEqualTo(FileType.MD);
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        assertThatThrownBy(() -> FileType.fromFileName("archive.zip"))
                .isInstanceOf(BusinessException.class);
    }
}
