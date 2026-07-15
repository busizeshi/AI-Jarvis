package com.notegather.common.api.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileNoteDTO implements Serializable {

    private Long noteId;
    private String noteTitle;
}
