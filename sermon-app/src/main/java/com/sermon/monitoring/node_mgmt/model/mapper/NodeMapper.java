package com.sermon.monitoring.node_mgmt.model.mapper;


import com.sermon.monitoring.node_mgmt.model.entity.Node;
import com.sermon.monitoring.node_mgmt.web.dto.NodeCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeMapper {
    Node toEntity(NodeCreationRequest request);
}
