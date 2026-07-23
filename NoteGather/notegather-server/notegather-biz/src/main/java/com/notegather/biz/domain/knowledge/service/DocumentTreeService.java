package com.notegather.biz.domain.knowledge.service;

import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.repository.DocumentRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文档目录树领域服务
 * 负责处理目录树相关的复杂业务逻辑
 */
@Service
@RequiredArgsConstructor
public class DocumentTreeService {
    
    private final DocumentRepository documentRepository;
    
    /**
     * 最大目录层级深度（0-4，共5层）
     */
    private static final int MAX_DEPTH = 4;
    
    /**
     * 校验父节点深度是否允许添加子节点
     * 
     * @param parentId 父节点ID，null表示根目录
     * @return 新节点应该的深度
     */
    public int validateAndCalculateDepth(DocumentId parentId) {
        if (parentId == null) {
            // 根目录，深度为0
            return 0;
        }
        
        Document parent = documentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("父文档不存在"));
        
        int newDepth = parent.getDepth() + 1;
        
        if (newDepth > MAX_DEPTH) {
            throw new IllegalStateException("目录树最大深度为5层，当前父节点已在第" + (parent.getDepth() + 1) + "层");
        }
        
        return newDepth;
    }
    
    /**
     * 检测环路：防止将父文档移动到自己的子文档下
     * 
     * @param documentId 要移动的文档ID
     * @param newParentId 新的父文档ID
     * @return true表示存在环路，false表示不存在
     */
    public boolean detectCycle(DocumentId documentId, DocumentId newParentId) {
        if (newParentId == null) {
            // 移动到根目录，不可能有环路
            return false;
        }
        
        if (documentId.equals(newParentId)) {
            // 不能将文档移动到自己下面
            return true;
        }
        
        // 从新父节点向上遍历，检查是否会遇到要移动的文档
        Set<Long> visited = new HashSet<>();
        DocumentId current = newParentId;
        
        while (current != null) {
            // 检测到环路：新父节点的祖先链中包含要移动的文档
            if (current.equals(documentId)) {
                return true;
            }
            
            // 防止数据库中已存在环路导致的无限循环
            if (visited.contains(current.getValue())) {
                throw new IllegalStateException("检测到数据库中存在环路，数据不一致");
            }
            visited.add(current.getValue());
            
            // 向上查找父节点
            final DocumentId currentId = current;
            Document doc = documentRepository.findById(current)
                    .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + currentId.getValue()));
            
            current = doc.getParentId();
        }
        
        return false;
    }
    
    /**
     * 计算文档路径（从根到当前节点的完整路径）
     * 
     * @param documentId 文档ID
     * @return 路径列表，按层级从根到叶
     */
    public List<Document> calculatePath(DocumentId documentId) {
        List<Document> path = new ArrayList<>();
        DocumentId current = documentId;
        
        while (current != null) {
            final DocumentId currentId = current;
            Document doc = documentRepository.findById(current)
                    .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + currentId.getValue()));
            
            path.add(0, doc);  // 插入到列表头部
            current = doc.getParentId();
        }
        
        return path;
    }
    
    /**
     * 递归更新子文档的深度
     * 当移动文档时，需要同步更新所有子文档的深度
     * 
     * @param documentId 文档ID
     * @param newDepth 新深度
     */
    public void updateChildrenDepth(DocumentId documentId, int newDepth) {
        List<Document> children = documentRepository.findByParentId(documentId);
        
        for (Document child : children) {
            int childNewDepth = newDepth + 1;
            
            if (childNewDepth > MAX_DEPTH) {
                throw new IllegalStateException(
                    String.format("移动操作将导致子文档 '%s' 超过最大深度限制", child.getTitle())
                );
            }
            
            child.moveTo(child.getParentId(), childNewDepth);
            documentRepository.save(child);
            
            // 递归更新子文档的子文档
            updateChildrenDepth(child.getId(), childNewDepth);
        }
    }
    
    /**
     * 校验移动操作的合法性
     * 
     * @param documentId 要移动的文档ID
     * @param newParentId 新的父文档ID
     */
    public void validateMove(DocumentId documentId, DocumentId newParentId) {
        // 1. 检测环路
        if (detectCycle(documentId, newParentId)) {
            throw new IllegalStateException("不能将文档移动到自己的子文档下");
        }
        
        // 2. 校验深度
        int newDepth = validateAndCalculateDepth(newParentId);
        
        // 3. 检查移动后子文档是否会超过深度限制
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在"));
        
        if (document.isFolder()) {
            // 如果是文件夹，需要检查其最深的子文档
            int maxChildDepth = calculateMaxChildDepth(documentId, 0);
            if (newDepth + maxChildDepth > MAX_DEPTH) {
                throw new IllegalStateException(
                    String.format("移动后将超过最大深度限制（当前文件夹下最深子文档为%d层）", maxChildDepth)
                );
            }
        }
    }
    
    /**
     * 计算文档下最深子文档的相对深度
     * 
     * @param documentId 文档ID
     * @param currentRelativeDepth 当前相对深度
     * @return 最深子文档的相对深度
     */
    private int calculateMaxChildDepth(DocumentId documentId, int currentRelativeDepth) {
        List<Document> children = documentRepository.findByParentId(documentId);
        
        if (children.isEmpty()) {
            return currentRelativeDepth;
        }
        
        final int nextDepth = currentRelativeDepth + 1;
        int maxDepth = currentRelativeDepth;
        
        for (Document child : children) {
            int childMaxDepth = calculateMaxChildDepth(child.getId(), nextDepth);
            maxDepth = Math.max(maxDepth, childMaxDepth);
        }
        
        return maxDepth;
    }
}
