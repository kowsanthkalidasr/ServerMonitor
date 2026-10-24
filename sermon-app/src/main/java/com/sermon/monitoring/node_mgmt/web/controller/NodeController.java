package com.sermon.monitoring.node_mgmt.web.controller;


import com.sermon.monitoring.node_mgmt.service.NodeService;
import com.sermon.monitoring.node_mgmt.web.dto.NodeCreationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequiredArgsConstructor

@RestController
@RequestMapping("api/v1/nodes")
public class NodeController {
    private final NodeService nodeService;

    @PostMapping
    public String createNode (@Valid @RequestBody NodeCreationRequest request) {
        nodeService.createNode(request);
        return "Node created successfully";
    }


    @DeleteMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<Void> deleteNode(@PathVariable UUID id) {
        nodeService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/ids")
    public ResponseEntity<Page<UUID>> findAllIds(Pageable page) {
        Page<UUID> uuidPage = nodeService.getAllNodeIds(page);
        return ResponseEntity.ok(uuidPage);
    }

}
