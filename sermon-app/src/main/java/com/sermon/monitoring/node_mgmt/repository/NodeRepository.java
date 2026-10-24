package com.sermon.monitoring.node_mgmt.repository;

import com.sermon.monitoring.node_mgmt.model.entity.Node;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID> {

    @Query("SELECT n.id from Node n")
    Page<UUID> findAllIds(Pageable pageable);





}
