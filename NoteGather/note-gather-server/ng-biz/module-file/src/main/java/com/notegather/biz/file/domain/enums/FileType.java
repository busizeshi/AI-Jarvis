package com.notegather.biz.file.domain.enums;

import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;

import java.util.Locale;

public enum FileType {
    PDF,
    TXT,
    MD;

    public static FileType fromFileName(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 1 || extensionIndex == fileName.length() - 1) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }
        try {
            return valueOf(fileName.substring(extensionIndex + 1).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }
    }
}
