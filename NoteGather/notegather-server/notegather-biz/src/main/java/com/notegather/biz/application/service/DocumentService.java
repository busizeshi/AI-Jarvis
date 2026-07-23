package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.CreateDocumentCommand;
import com.notegather.biz.application.command.MoveDocumentCommand;
import com.notegather.biz.application.command.UpdateDocumentCommand;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.domain.knowledge.repository.DocumentRepository;
import com.notegather.biz.domain.knowledge.repository.KnowledgeBaseRepository;
import com.notegather.biz.domain.knowledge.service.DocumentTreeService;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.DocumentType;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档应用服务
 */
@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final DocumentTreeService documentTreeService;
    
    /**
     * 创建文档
     */
    @Transactional(rollbackFor = Exception.class)
    public Document createDocument(CreateDocumentCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBaseId kbId = KnowledgeBaseId.of(command.getKnowledgeBaseId());
        
        // 校验知识库权限
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        if (!kb.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该知识库");
        }
        
        // 计算深度
        DocumentId parentId = command.getParentId() != null 
            ? DocumentId.of(command.getParentId()) 
            : null;
        int depth = documentTreeService.validateAndCalculateDepth(parentId);
        
        // 创建文档
        DocumentType type = DocumentType.fromCode(command.getType());
        Document document;
        
        if (type == DocumentType.FOLDER) {
            document = Document.createFolder(kbId, userId, parentId, command.getTitle(), depth);
        } else {
            document = Document.createDocument(kbId, userId, parentId, command.getTitle(), command.getContent(), depth);
        }
        
        // 保存
        document = documentRepository.save(document);
        
        // 更新知识库文档计数
        kb.incrementDocCount();
        knowledgeBaseRepository.save(kb);
        
        return document;
    }
    
    /**
     * 更新文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDocument(UpdateDocumentCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        Document document = documentRepository.findById(DocumentId.of(command.getId()))
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        // 权限检查
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该文档");
        }
        
        document.updateContent(command.getTitle(), command.getContent());
        documentRepository.save(document);
    }
    
    /**
     * 移动文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveDocument(MoveDocumentCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        DocumentId documentId = DocumentId.of(command.getId());
        DocumentId newParentId = command.getNewParentId() != null 
            ? DocumentId.of(command.getNewParentId()) 
            : null;
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        // 权限检查
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该文档");
        }
        
        // 校验移动操作合法性
        documentTreeService.validateMove(documentId, newParentId);
        
        // 计算新深度
        int newDepth = documentTreeService.validateAndCalculateDepth(newParentId);
        
        // 移动文档
        document.moveTo(newParentId, newDepth);
        documentRepository.save(document);
        
        // 递归更新子文档深度
        if (document.isFolder()) {
            documentTreeService.updateChildrenDepth(documentId, newDepth);
        }
    }
    
    /**
     * 删除文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        Document document = documentRepository.findById(DocumentId.of(id))
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        // 权限检查
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该文档");
        }
        
        // 使用 MyBatis Plus 的逻辑删除
        documentRepository.deleteById(DocumentId.of(id));
        
        // 更新知识库文档计数
        KnowledgeBase kb = knowledgeBaseRepository.findById(document.getKnowledgeBaseId())
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        kb.decrementDocCount();
        knowledgeBaseRepository.save(kb);
    }
    
    /**
     * 查询文档详情
     */
    public Document getDocument(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        Document document = documentRepository.findById(DocumentId.of(id))
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        // 权限检查：暂时只允许所有者查看
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限访问该文档");
        }
        
        return document;
    }
    
    /**
     * 查询知识库下的所有文档
     */
    public List<Document> getDocumentsByKnowledgeBase(Long knowledgeBaseId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBaseId kbId = KnowledgeBaseId.of(knowledgeBaseId);
        
        // 校验知识库权限
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        if (!kb.isOwnedBy(userId)) {
            throw new RuntimeException("无权限访问该知识库");
        }
        
        return documentRepository.findByKnowledgeBaseId(kbId);
    }
    
    /**
     * 查询知识库根目录文档
     */
    public List<Document> getRootDocuments(Long knowledgeBaseId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBaseId kbId = KnowledgeBaseId.of(knowledgeBaseId);
        
        // 校验知识库权限
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        if (!kb.isOwnedBy(userId)) {
            throw new RuntimeException("无权限访问该知识库");
        }
        
        return documentRepository.findRootDocuments(kbId);
    }
    
    /**
     * 查询子文档列表
     */
    public List<Document> getChildDocuments(Long parentId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 校验父文档权限
        Document parent = documentRepository.findById(DocumentId.of(parentId))
                .orElseThrow(() -> new RuntimeException("父文档不存在"));
        
        if (!parent.isOwnedBy(userId)) {
            throw new RuntimeException("无权限访问该文档");
        }
        
        return documentRepository.findByParentId(DocumentId.of(parentId));
    }
}
