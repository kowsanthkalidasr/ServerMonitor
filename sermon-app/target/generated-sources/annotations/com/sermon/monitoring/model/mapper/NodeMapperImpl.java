package com.sermon.monitoring.model.mapper;

import com.sermon.monitoring.node_mgmt.model.entity.Node;
import com.sermon.monitoring.node_mgmt.model.mapper.NodeMapper;
import com.sermon.monitoring.node_mgmt.web.dto.NodeCreationRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-26T02:37:41+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class NodeMapperImpl implements NodeMapper {

    @Override
    public Node toEntity(NodeCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        Node.NodeBuilder node = Node.builder();

        node.name( request.name() );
        node.osName( request.osName() );
        node.ipAddress( request.ipAddress() );
        node.type( request.type() );
        node.opStatus( request.opStatus() );

        return node.build();
    }
}
