package com.notegather.biz.ai.infrastructure.persistence.citation.repository;

import com.notegather.biz.ai.domain.model.ChatCitation;
import com.notegather.biz.ai.domain.repository.ChatCitationRepository;
import com.notegather.biz.ai.infrastructure.persistence.citation.mapper.ChatCitationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisChatCitationRepository implements ChatCitationRepository {

    private final ChatCitationMapper chatCitationMapper;

    @Override
    public void save(ChatCitation citation) {
        chatCitationMapper.insert(citation);
    }
}
