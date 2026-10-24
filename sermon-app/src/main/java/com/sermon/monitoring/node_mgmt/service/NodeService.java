package com.sermon.monitoring.node_mgmt.service;


import com.sermon.exception.ResourceNotFoundException;
import com.sermon.monitoring.node_mgmt.model.entity.Node;
import com.sermon.monitoring.node_mgmt.model.mapper.NodeMapper;
import com.sermon.monitoring.node_mgmt.repository.NodeRepository;
import com.sermon.monitoring.node_mgmt.web.dto.NodeCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@RequiredArgsConstructor

@Service
public class NodeService {

    private final NodeMapper nodeMapper;
    private final NodeRepository nodeRepository;


    @Transactional
    public void createNode(NodeCreationRequest request) {
        Node node = nodeMapper.toEntity(request);
        nodeRepository.save(node);
    }

    @Transactional
    public void deleteNode(UUID id) {
        Node node = nodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Node not found with ID : " + id));

        nodeRepository.delete(node);
    }


    @Transactional
    public Page<UUID> getAllNodeIds(Pageable pageable) {
        return nodeRepository.findAllIds(pageable);
    }








}
