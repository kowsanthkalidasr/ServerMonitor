package com.sermon.monitoring.node_mgmt.model.mapper;

import com.sermon.monitoring.node_mgmt.model.entity.Node;
import com.sermon.monitoring.node_mgmt.web.dto.NodeCreationRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-24T12:36:46+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20260930-0211, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class NodeMapperImpl implements NodeMapper {

    @Override
    public Node toEntity(NodeCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        Node.NodeBuilder node = Node.builder();

        node.ipAddress( request.ipAddress() );
        node.name( request.name() );
        node.opStatus( request.opStatus() );
        node.osName( request.osName() );
        node.type( request.type() );

        return node.build();
    }
}
