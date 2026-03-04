import React, { useMemo, useCallback, useEffect, useState } from 'react';
import {
  ReactFlow,
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  MarkerType,
  Panel,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import dagre from 'dagre';
import { Card, Tag, Space, Typography } from 'antd';
import { RelationshipData } from '../types/relationship';
import { useRelationshipStore } from '../store/relationshipStore';

const { Text } = Typography;

interface RelationshipGraphProps {
  data: RelationshipData;
}

const domainColors: Record<string, string> = {
  'KYC': '#9254de',
  'Payments': '#6366f1',
  'Wallet': '#22c55e',
  'Fraud': '#ef4444',
  'Infrastructure': '#f59e0b',
  'Admin': '#06b6d4',
  'Reporting': '#ec4899',
  'User Experience': '#3b82f6',
  'General': '#64748b',
};

const statusColors = {
  feature: {
    PLANNED: '#3b82f6',
    IN_PROGRESS: '#f59e0b',
    RELEASED: '#22c55e',
  },
  microservice: {
    NOT_STARTED: '#64748b',
    IN_PROGRESS: '#3b82f6',
    COMPLETED: '#22c55e',
  },
};

// Node dimensions for DAGRE layout
const NODE_DIMENSIONS = {
  domain: { width: 150, height: 50 },
  feature: { width: 180, height: 50 },
  microservice: { width: 160, height: 45 },
};

// DAGRE layout function
const getLayoutedElements = (nodes: Node[], edges: Edge[], direction = 'TB') => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  
  // Configure the layout
  dagreGraph.setGraph({ 
    rankdir: direction,
    nodesep: 80,      // Horizontal spacing between nodes
    ranksep: 120,     // Vertical spacing between ranks/layers
    marginx: 50,
    marginy: 50,
    align: 'UL',
  });

  // Add nodes to dagre graph
  nodes.forEach((node) => {
    const entityType = node.data?.entityType as string;
    const dims = NODE_DIMENSIONS[entityType as keyof typeof NODE_DIMENSIONS] || { width: 150, height: 50 };
    dagreGraph.setNode(node.id, { width: dims.width, height: dims.height });
  });

  // Add edges to dagre graph
  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  // Run the layout algorithm
  dagre.layout(dagreGraph);

  // Apply positions to nodes
  const layoutedNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    const entityType = node.data?.entityType as string;
    const dims = NODE_DIMENSIONS[entityType as keyof typeof NODE_DIMENSIONS] || { width: 150, height: 50 };
    
    return {
      ...node,
      position: {
        x: nodeWithPosition.x - dims.width / 2,
        y: nodeWithPosition.y - dims.height / 2,
      },
    };
  });

  return { nodes: layoutedNodes, edges };
};

const RelationshipGraph: React.FC<RelationshipGraphProps> = ({ data }) => {
  const { selection, selectMicroservice, selectFeature, clearSelection } = useRelationshipStore();
  const [selectedDomain, setSelectedDomain] = useState<string | null>(null);

  const getConnectedIds = useCallback((type: string, id: string) => {
    const connected = new Set<string>();
    if (type === 'domain') {
      data.features.filter(f => (f.domain || 'General') === id).forEach(f => {
        connected.add(`f-${f.id}`);
        const msIds = data.featureToMicroservices?.[f.id] || [];
        msIds.forEach(msId => connected.add(`ms-${msId}`));
      });
      connected.add(`domain-${id}`);
    } else if (type === 'feature') {
      const feature = data.features.find(f => f.id === id);
      if (feature) {
        connected.add(`domain-${feature.domain || 'General'}`);
        connected.add(`f-${id}`);
        const msIds = data.featureToMicroservices?.[id] || [];
        msIds.forEach(msId => connected.add(`ms-${msId}`));
      }
    } else if (type === 'microservice') {
      connected.add(`ms-${id}`);
      const featureIds = data.microserviceToFeatures?.[id] || [];
      featureIds.forEach(fId => {
        connected.add(`f-${fId}`);
        const feature = data.features.find(f => f.id === fId);
        if (feature) {
          connected.add(`domain-${feature.domain || 'General'}`);
        }
      });
    }
    return connected;
  }, [data]);

  // Build initial nodes (without positions - DAGRE will handle positioning)
  const buildInitialNodes = useCallback((): Node[] => {
    const domains = [...new Set(data.features.map(f => f.domain || 'General'))];
    const allNodes: Node[] = [];
    
    // Create domain nodes (TOP LAYER)
    domains.forEach((domain) => {
      const color = domainColors[domain] || '#64748b';
      allNodes.push({
        id: `domain-${domain}`,
        type: 'default',
        position: { x: 0, y: 0 }, // Will be set by DAGRE
        data: { label: domain, entityId: domain, entityType: 'domain' },
        style: {
          background: `linear-gradient(135deg, ${color}, ${color}cc)`,
          color: '#fff',
          border: `2px solid ${color}`,
          borderRadius: 10,
          padding: '10px 14px',
          width: NODE_DIMENSIONS.domain.width,
          fontWeight: 700,
          fontSize: 12,
          textAlign: 'center',
          boxShadow: `0 4px 14px ${color}40`,
        },
      });
    });
    
    // Create feature nodes (MIDDLE LAYER)
    data.features.forEach((f) => {
      const color = statusColors.feature[f.status as keyof typeof statusColors.feature] || '#3b82f6';
      allNodes.push({
        id: `f-${f.id}`,
        type: 'default',
        position: { x: 0, y: 0 }, // Will be set by DAGRE
        data: { label: f.name, entityId: f.id, entityType: 'feature', status: f.status, domain: f.domain },
        style: {
          background: '#fff',
          color: '#1e293b',
          border: `2px solid ${color}`,
          borderRadius: 8,
          padding: '8px 12px',
          width: NODE_DIMENSIONS.feature.width,
          fontWeight: 600,
          fontSize: 11,
          textAlign: 'center',
          boxShadow: `0 2px 8px ${color}25`,
        },
      });
    });
    
    // Create microservice nodes (BOTTOM LAYER)
    data.microservices.forEach((ms) => {
      const color = statusColors.microservice[ms.status as keyof typeof statusColors.microservice] || '#64748b';
      allNodes.push({
        id: `ms-${ms.id}`,
        type: 'default',
        position: { x: 0, y: 0 }, // Will be set by DAGRE
        data: { label: ms.name, entityId: ms.id, entityType: 'microservice', status: ms.status },
        style: {
          background: '#fff',
          color: '#1e293b',
          border: `2px solid ${color}`,
          borderRadius: 8,
          padding: '6px 10px',
          width: NODE_DIMENSIONS.microservice.width,
          fontWeight: 600,
          fontSize: 10,
          textAlign: 'center',
          boxShadow: `0 2px 6px ${color}20`,
        },
      });
    });
    
    return allNodes;
  }, [data]);

  const buildEdges = useCallback((): Edge[] => {
    const edges: Edge[] = [];
    
    // Domain → Feature edges
    data.features.forEach((f) => {
      const domain = f.domain || 'General';
      const color = domainColors[domain] || '#64748b';
      edges.push({
        id: `d-f-${domain}-${f.id}`,
        source: `domain-${domain}`,
        target: `f-${f.id}`,
        type: 'smoothstep',
        style: { stroke: color, strokeWidth: 2, opacity: 0.7 },
        markerEnd: { type: MarkerType.ArrowClosed, color, width: 15, height: 15 },
      });
    });
    
    // Feature → Microservice edges
    data.relationships.forEach((rel, idx) => {
      edges.push({
        id: `f-ms-${idx}`,
        source: `f-${rel.featureId}`,
        target: `ms-${rel.microserviceId}`,
        type: 'smoothstep',
        style: { stroke: '#94a3b8', strokeWidth: 2, opacity: 0.6 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#94a3b8', width: 15, height: 15 },
      });
    });
    
    return edges;
  }, [data]);

  // Apply DAGRE layout
  const { nodes: layoutedNodes, edges: layoutedEdges } = useMemo(() => {
    const initialNodes = buildInitialNodes();
    const initialEdges = buildEdges();
    return getLayoutedElements(initialNodes, initialEdges, 'TB');
  }, [buildInitialNodes, buildEdges]);

  const [nodes, setNodes, onNodesChange] = useNodesState(layoutedNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(layoutedEdges);

  useEffect(() => {
    const initialNodes = buildInitialNodes();
    const initialEdges = buildEdges();
    const { nodes: newLayoutedNodes, edges: newLayoutedEdges } = getLayoutedElements(initialNodes, initialEdges, 'TB');
    setNodes(newLayoutedNodes);
    setEdges(newLayoutedEdges);
  }, [data, buildInitialNodes, buildEdges, setNodes, setEdges]);

  const onNodeClick = useCallback((_: React.MouseEvent, node: Node) => {
    const entityType = node.data.entityType as string;
    const entityId = node.data.entityId as string;

    if (entityType === 'domain') {
      setSelectedDomain(prev => prev === entityId ? null : entityId);
      clearSelection();
    } else if (entityType === 'microservice') {
      setSelectedDomain(null);
      selection.type === 'microservice' && selection.id === entityId ? clearSelection() : selectMicroservice(entityId);
    } else if (entityType === 'feature') {
      setSelectedDomain(null);
      selection.type === 'feature' && selection.id === entityId ? clearSelection() : selectFeature(entityId);
    }
  }, [selection, selectMicroservice, selectFeature, clearSelection]);

  const styledNodes = useMemo(() => {
    const hasSelection = selectedDomain || selection.id;
    if (!hasSelection) return nodes;

    let connectedNodeIds = new Set<string>();
    if (selectedDomain) {
      connectedNodeIds = getConnectedIds('domain', selectedDomain);
    } else if (selection.type && selection.id) {
      connectedNodeIds = getConnectedIds(selection.type, selection.id);
    }

    return nodes.map(node => {
      const isConnected = connectedNodeIds.has(node.id);
      const isSelected = 
        (selectedDomain && node.id === `domain-${selectedDomain}`) ||
        (selection.type === 'microservice' && node.id === `ms-${selection.id}`) ||
        (selection.type === 'feature' && node.id === `f-${selection.id}`);

      return {
        ...node,
        style: {
          ...node.style,
          opacity: isConnected || isSelected ? 1 : 0.35,
          border: isSelected ? '3px solid #6366f1' : isConnected ? '3px solid #22c55e' : node.style?.border,
          boxShadow: isSelected 
            ? '0 0 0 4px rgba(99, 102, 241, 0.3), 0 4px 20px rgba(99, 102, 241, 0.4)' 
            : isConnected 
              ? '0 0 0 3px rgba(34, 197, 94, 0.2), 0 4px 16px rgba(34, 197, 94, 0.3)' 
              : node.style?.boxShadow,
          transform: isSelected ? 'scale(1.08)' : isConnected ? 'scale(1.02)' : undefined,
          zIndex: isSelected ? 100 : isConnected ? 50 : 1,
        },
      };
    });
  }, [nodes, selection, selectedDomain, getConnectedIds]);

  const styledEdges = useMemo(() => {
    const hasSelection = selectedDomain || selection.id;
    if (!hasSelection) return edges;

    let connectedNodeIds = new Set<string>();
    if (selectedDomain) {
      connectedNodeIds = getConnectedIds('domain', selectedDomain);
    } else if (selection.type && selection.id) {
      connectedNodeIds = getConnectedIds(selection.type, selection.id);
    }

    return edges.map(edge => {
      const isHighlighted = connectedNodeIds.has(edge.source) && connectedNodeIds.has(edge.target);
      return {
        ...edge,
        animated: isHighlighted,
        style: {
          ...edge.style,
          stroke: isHighlighted ? '#6366f1' : '#e2e8f0',
          strokeWidth: isHighlighted ? 3 : 1,
          opacity: isHighlighted ? 1 : 0.25,
        },
        markerEnd: isHighlighted 
          ? { type: MarkerType.ArrowClosed, color: '#6366f1', width: 18, height: 18 }
          : { type: MarkerType.ArrowClosed, color: '#e2e8f0', width: 12, height: 12 },
      };
    });
  }, [edges, selection, selectedDomain, getConnectedIds]);

  // Get selected entity details
  const selectedDetails = useMemo(() => {
    if (selectedDomain) {
      const features = data.features.filter(f => (f.domain || 'General') === selectedDomain);
      const msIds = new Set<string>();
      features.forEach(f => {
        (data.featureToMicroservices?.[f.id] || []).forEach(id => msIds.add(id));
      });
      return { type: 'Domain', name: selectedDomain, features: features.length, microservices: msIds.size };
    }
    if (selection.type === 'feature' && selection.id) {
      const feature = data.features.find(f => f.id === selection.id);
      const msIds = data.featureToMicroservices?.[selection.id] || [];
      return { type: 'Feature', name: feature?.name, status: feature?.status, microservices: msIds.length };
    }
    if (selection.type === 'microservice' && selection.id) {
      const ms = data.microservices.find(m => m.id === selection.id);
      const featureIds = data.microserviceToFeatures?.[selection.id] || [];
      return { type: 'Microservice', name: ms?.name, status: ms?.status, features: featureIds.length };
    }
    return null;
  }, [selectedDomain, selection, data]);

  return (
    <div style={{ width: '100%', height: '100%', minHeight: 600 }}>
      <ReactFlow
        nodes={styledNodes}
        edges={styledEdges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={onNodeClick}
        onPaneClick={() => { setSelectedDomain(null); clearSelection(); }}
        fitView
        fitViewOptions={{ padding: 0.2, minZoom: 0.3, maxZoom: 1.5 }}
        minZoom={0.1}
        maxZoom={2}
        attributionPosition="bottom-left"
        style={{ background: '#f8fafc' }}
      >
        <Background color="#e2e8f0" gap={24} size={1} />
        <Controls style={{ borderRadius: 8, overflow: 'hidden' }} />
        <MiniMap 
          nodeColor={(node) => {
            const type = node.data?.entityType as string;
            if (type === 'domain') return '#6366f1';
            if (type === 'feature') return '#3b82f6';
            return '#22c55e';
          }}
          maskColor="rgba(0,0,0,0.1)"
          style={{ background: '#fff', borderRadius: 8 }}
        />
        
        {/* Selection Details Panel */}
        {selectedDetails && (
          <Panel position="top-right">
            <Card size="small" style={{ minWidth: 200, borderRadius: 8, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
              <Space direction="vertical" size={4}>
                <Tag color="blue">{selectedDetails.type}</Tag>
                <Text strong style={{ fontSize: 14 }}>{selectedDetails.name}</Text>
                {selectedDetails.status && (
                  <Tag color={selectedDetails.status === 'RELEASED' || selectedDetails.status === 'COMPLETED' ? 'green' : selectedDetails.status === 'IN_PROGRESS' ? 'orange' : 'blue'}>
                    {selectedDetails.status}
                  </Tag>
                )}
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {selectedDetails.features !== undefined && `Features: ${selectedDetails.features}`}
                  {selectedDetails.microservices !== undefined && ` • Services: ${selectedDetails.microservices}`}
                </Text>
              </Space>
            </Card>
          </Panel>
        )}
        
        {/* Legend Panel */}
        <Panel position="bottom-right">
          <Card size="small" style={{ borderRadius: 8, opacity: 0.9 }}>
            <Space size={12}>
              <Space size={4}><div style={{ width: 12, height: 12, borderRadius: 3, background: '#6366f1' }} /><Text style={{ fontSize: 11 }}>Domain</Text></Space>
              <Space size={4}><div style={{ width: 12, height: 12, borderRadius: 3, background: '#3b82f6' }} /><Text style={{ fontSize: 11 }}>Feature</Text></Space>
              <Space size={4}><div style={{ width: 12, height: 12, borderRadius: 3, background: '#22c55e' }} /><Text style={{ fontSize: 11 }}>Service</Text></Space>
            </Space>
          </Card>
        </Panel>
      </ReactFlow>
    </div>
  );
};

export default RelationshipGraph;
