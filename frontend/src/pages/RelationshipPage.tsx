import React, { useEffect, useMemo, useCallback, useState } from 'react';
import { Card, Row, Col, Input, Select, Switch, Segmented, Spin, Typography, Badge, Tooltip, Progress, Tag, Empty } from 'antd';
import { SearchOutlined, ApiOutlined, AppstoreOutlined, NodeIndexOutlined, UnorderedListOutlined, ClusterOutlined, BlockOutlined } from '@ant-design/icons';
import { motion, AnimatePresence } from 'framer-motion';
import { useRelationshipStore } from '../store/relationshipStore';
import { relationshipService } from '../services/relationshipService';
import { MicroserviceNode, FeatureNode } from '../types/relationship';
import RelationshipGraph from '../components/RelationshipGraph';
import RelationshipView from '../components/RelationshipView';

const { Title, Text } = Typography;
const { Option } = Select;

const statusColors: Record<string, string> = {
  NOT_STARTED: 'default',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
  PLANNED: 'blue',
  RELEASED: 'green',
};

const domainColors: Record<string, string> = {
  'KYC': '#722ed1',
  'Payments': '#1890ff',
  'Wallet': '#52c41a',
  'Fraud': '#f5222d',
  'Infrastructure': '#fa8c16',
  'Admin': '#13c2c2',
  'Reporting': '#eb2f96',
  'User Experience': '#2f54eb',
  'General': '#8c8c8c',
};

const MicroserviceCard: React.FC<{
  microservice: MicroserviceNode;
  isSelected: boolean;
  isConnected: boolean;
  isDimmed: boolean;
  onClick: () => void;
}> = React.memo(({ microservice, isSelected, isConnected, isDimmed, onClick }) => {
  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 20 }}
      animate={{ 
        opacity: isDimmed ? 0.3 : 1, 
        y: 0,
        scale: isSelected ? 1.02 : 1,
      }}
      transition={{ duration: 0.2 }}
      whileHover={{ scale: isDimmed ? 1 : 1.02 }}
    >
      <Card
        size="small"
        onClick={onClick}
        style={{
          marginBottom: 8,
          cursor: 'pointer',
          borderColor: isSelected ? '#1890ff' : isConnected ? '#52c41a' : undefined,
          borderWidth: isSelected || isConnected ? 2 : 1,
          boxShadow: isSelected ? '0 4px 12px rgba(24, 144, 255, 0.3)' : 
                     isConnected ? '0 2px 8px rgba(82, 196, 26, 0.2)' : undefined,
          background: isConnected && !isSelected ? '#f6ffed' : undefined,
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div style={{ flex: 1 }}>
            <Tooltip title={microservice.description || 'No description'}>
              <Text strong style={{ fontSize: 14 }}>{microservice.name}</Text>
            </Tooltip>
            <div style={{ marginTop: 4 }}>
              <Tag color={statusColors[microservice.status]} style={{ fontSize: 11 }}>
                {microservice.status.replace('_', ' ')}
              </Tag>
              {microservice.version && (
                <Tag style={{ fontSize: 11 }}>v{microservice.version}</Tag>
              )}
              {microservice.highRisk && (
                <Tag color="red" style={{ fontSize: 11 }}>HIGH IMPACT</Tag>
              )}
            </div>
          </div>
          <Tooltip title={`Connected to ${microservice.featureCount} feature(s)`}>
            <Badge 
              count={microservice.featureCount} 
              style={{ backgroundColor: microservice.featureCount > 0 ? '#1890ff' : '#d9d9d9' }}
            />
          </Tooltip>
        </div>
        <Progress 
          percent={microservice.progressPercentage} 
          size="small" 
          style={{ marginTop: 8, marginBottom: 0 }}
          status={microservice.progressPercentage === 100 ? 'success' : 'active'}
        />
        <Text type="secondary" style={{ fontSize: 11 }}>
          {microservice.completedChecklistCount}/{microservice.checklistCount} checklists
        </Text>
      </Card>
    </motion.div>
  );
});

const FeatureCard: React.FC<{
  feature: FeatureNode;
  isSelected: boolean;
  isConnected: boolean;
  isDimmed: boolean;
  onClick: () => void;
}> = React.memo(({ feature, isSelected, isConnected, isDimmed, onClick }) => {
  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 20 }}
      animate={{ 
        opacity: isDimmed ? 0.3 : 1, 
        y: 0,
        scale: isSelected ? 1.02 : 1,
      }}
      transition={{ duration: 0.2 }}
      whileHover={{ scale: isDimmed ? 1 : 1.02 }}
    >
      <Card
        size="small"
        onClick={onClick}
        style={{
          marginBottom: 8,
          cursor: 'pointer',
          borderColor: isSelected ? '#1890ff' : isConnected ? '#52c41a' : feature.isShared ? '#faad14' : undefined,
          borderWidth: isSelected || isConnected || feature.isShared ? 2 : 1,
          boxShadow: isSelected ? '0 4px 12px rgba(24, 144, 255, 0.3)' : 
                     isConnected ? '0 2px 8px rgba(82, 196, 26, 0.2)' :
                     feature.isShared ? '0 2px 8px rgba(250, 173, 20, 0.2)' : undefined,
          background: isConnected && !isSelected ? '#f6ffed' : feature.isShared ? '#fffbe6' : undefined,
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div style={{ flex: 1 }}>
            <Tooltip title={feature.description || 'No description'}>
              <Text strong style={{ fontSize: 14 }}>{feature.name}</Text>
            </Tooltip>
            <div style={{ marginTop: 4 }}>
              <Tag color={domainColors[feature.domain] || '#8c8c8c'} style={{ fontSize: 11 }}>
                {feature.domain}
              </Tag>
              <Tag color={statusColors[feature.status]} style={{ fontSize: 11 }}>
                {feature.status.replace('_', ' ')}
              </Tag>
              {feature.releaseVersion && (
                <Tag style={{ fontSize: 11 }}>{feature.releaseVersion}</Tag>
              )}
              {feature.isShared && (
                <Tag color="warning" style={{ fontSize: 11 }}>SHARED</Tag>
              )}
            </div>
          </div>
          <Tooltip title={`Connected to ${feature.microserviceCount} microservice(s)`}>
            <Badge 
              count={feature.microserviceCount} 
              style={{ backgroundColor: feature.microserviceCount > 1 ? '#faad14' : feature.microserviceCount > 0 ? '#52c41a' : '#d9d9d9' }}
            />
          </Tooltip>
        </div>
        {feature.targetDate && (
          <Text type="secondary" style={{ fontSize: 11, marginTop: 4, display: 'block' }}>
            Target: {feature.targetDate}
          </Text>
        )}
      </Card>
    </motion.div>
  );
});

const RelationshipPage: React.FC = () => {
  const {
    data,
    loading,
    viewMode,
    highlightMode,
    selection,
    microserviceSearch,
    featureSearch,
    microserviceStatusFilter,
    featureStatusFilter,
    setData,
    setLoading,
    setError,
    setViewMode,
    toggleHighlightMode,
    selectMicroservice,
    selectFeature,
    clearSelection,
    setMicroserviceSearch,
    setFeatureSearch,
    setMicroserviceStatusFilter,
    setFeatureStatusFilter,
  } = useRelationshipStore();

  useEffect(() => {
    loadRelationships();
  }, []);

  const loadRelationships = async () => {
    setLoading(true);
    try {
      const response = await relationshipService.getRelationships();
      setData(response);
    } catch (error) {
      setError('Failed to load relationships');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const filteredMicroservices = useMemo(() => {
    if (!data) return [];
    return data.microservices.filter(ms => {
      const matchesSearch = !microserviceSearch || 
        ms.name.toLowerCase().includes(microserviceSearch.toLowerCase()) ||
        ms.description?.toLowerCase().includes(microserviceSearch.toLowerCase());
      const matchesStatus = !microserviceStatusFilter || ms.status === microserviceStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [data, microserviceSearch, microserviceStatusFilter]);

  const filteredFeatures = useMemo(() => {
    if (!data) return [];
    return data.features.filter(f => {
      const matchesSearch = !featureSearch || 
        f.name.toLowerCase().includes(featureSearch.toLowerCase()) ||
        f.description?.toLowerCase().includes(featureSearch.toLowerCase());
      const matchesStatus = !featureStatusFilter || f.status === featureStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [data, featureSearch, featureStatusFilter]);

  const handleMicroserviceClick = useCallback((id: string) => {
    if (selection.type === 'microservice' && selection.id === id) {
      clearSelection();
    } else {
      selectMicroservice(id);
    }
  }, [selection, clearSelection, selectMicroservice]);

  const handleFeatureClick = useCallback((id: string) => {
    if (selection.type === 'feature' && selection.id === id) {
      clearSelection();
    } else {
      selectFeature(id);
    }
  }, [selection, clearSelection, selectFeature]);

  const getMicroserviceState = useCallback((ms: MicroserviceNode) => {
    if (!highlightMode || !selection.id) {
      return { isSelected: false, isConnected: false, isDimmed: false };
    }
    
    const isSelected = selection.type === 'microservice' && selection.id === ms.id;
    const isConnected = selection.type === 'feature' && selection.connectedIds.has(ms.id);
    const isDimmed = selection.id !== null && !isSelected && !isConnected;
    
    return { isSelected, isConnected, isDimmed };
  }, [highlightMode, selection]);

  const getFeatureState = useCallback((f: FeatureNode) => {
    if (!highlightMode || !selection.id) {
      return { isSelected: false, isConnected: false, isDimmed: false };
    }
    
    const isSelected = selection.type === 'feature' && selection.id === f.id;
    const isConnected = selection.type === 'microservice' && selection.connectedIds.has(f.id);
    const isDimmed = selection.id !== null && !isSelected && !isConnected;
    
    return { isSelected, isConnected, isDimmed };
  }, [highlightMode, selection]);

  const summaryStats = useMemo(() => {
    if (!data) return null;
    const sharedFeatures = data.features.filter(f => f.isShared).length;
    const totalConnections = data.relationships.length;
    const uniqueDomains = [...new Set(data.features.map(f => f.domain || 'General'))];
    return { sharedFeatures, totalConnections, domainCount: uniqueDomains.length };
  }, [data]);

  // Domain data aggregation
  const domainData = useMemo(() => {
    if (!data) return [];
    const domainMap = new Map<string, { name: string; features: FeatureNode[]; microserviceIds: Set<string> }>();
    
    data.features.forEach(feature => {
      const domain = feature.domain || 'General';
      if (!domainMap.has(domain)) {
        domainMap.set(domain, { name: domain, features: [], microserviceIds: new Set() });
      }
      const domainEntry = domainMap.get(domain)!;
      domainEntry.features.push(feature);
      
      // Get connected microservices for this feature
      const featureMsIds = data.featureToMicroservices?.[feature.id] || [];
      featureMsIds.forEach(msId => domainEntry.microserviceIds.add(msId));
    });
    
    return Array.from(domainMap.values()).map(d => ({
      name: d.name,
      featureCount: d.features.length,
      microserviceCount: d.microserviceIds.size,
      features: d.features,
    }));
  }, [data]);

  const [selectedDomain, setSelectedDomain] = useState<string | null>(null);
  const [domainSearch, setDomainSearch] = useState('');

  const filteredDomains = useMemo(() => {
    return domainData.filter(d => 
      !domainSearch || d.name.toLowerCase().includes(domainSearch.toLowerCase())
    );
  }, [domainData, domainSearch]);

  const handleDomainClick = useCallback((domainName: string) => {
    if (selectedDomain === domainName) {
      setSelectedDomain(null);
    } else {
      setSelectedDomain(domainName);
    }
  }, [selectedDomain]);

  // Filter features by selected domain
  const domainFilteredFeatures = useMemo(() => {
    if (!selectedDomain) return filteredFeatures;
    return filteredFeatures.filter(f => (f.domain || 'General') === selectedDomain);
  }, [filteredFeatures, selectedDomain]);

  // Filter microservices by selected domain (through features)
  const domainFilteredMicroservices = useMemo(() => {
    if (!selectedDomain || !data) return filteredMicroservices;
    const domainFeatureIds = data.features
      .filter(f => (f.domain || 'General') === selectedDomain)
      .map(f => f.id);
    const connectedMsIds = new Set<string>();
    domainFeatureIds.forEach(fId => {
      const msIds = data.featureToMicroservices?.[fId] || [];
      msIds.forEach(msId => connectedMsIds.add(msId));
    });
    return filteredMicroservices.filter(ms => connectedMsIds.has(ms.id));
  }, [filteredMicroservices, selectedDomain, data]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0 }}>Relationship Visualization</Title>
          </Col>
          <Col>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Text>Highlight Mode:</Text>
                <Switch checked={highlightMode} onChange={toggleHighlightMode} />
              </div>
              <Segmented
                value={viewMode}
                onChange={(value) => setViewMode(value as 'list' | 'graph' | 'cards')}
                options={[
                  { label: 'List View', value: 'list', icon: <UnorderedListOutlined /> },
                  { label: 'Cards View', value: 'cards', icon: <BlockOutlined /> },
                  { label: 'Graph View', value: 'graph', icon: <NodeIndexOutlined /> },
                ]}
              />
            </div>
          </Col>
        </Row>
      </div>

      {summaryStats && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Row gutter={24}>
            <Col>
              <Text type="secondary">Domains: </Text>
              <Text strong>{summaryStats.domainCount}</Text>
            </Col>
            <Col>
              <Text type="secondary">Features: </Text>
              <Text strong>{data?.features.length || 0}</Text>
            </Col>
            <Col>
              <Text type="secondary">Microservices: </Text>
              <Text strong>{data?.microservices.length || 0}</Text>
            </Col>
            <Col>
              <Text type="secondary">Connections: </Text>
              <Text strong>{summaryStats.totalConnections}</Text>
            </Col>
            <Col>
              <Text type="secondary">Shared Features: </Text>
              <Text strong style={{ color: '#faad14' }}>{summaryStats.sharedFeatures}</Text>
            </Col>
            {selection.id && (
              <Col>
                <Tag color="blue" closable onClose={clearSelection}>
                  {selection.type === 'microservice' ? 'Microservice' : 'Feature'} selected: {selection.connectedIds.size} connections
                </Tag>
              </Col>
            )}
            {selectedDomain && (
              <Col>
                <Tag color={domainColors[selectedDomain] || '#8c8c8c'} closable onClose={() => setSelectedDomain(null)}>
                  Domain: {selectedDomain}
                </Tag>
              </Col>
            )}
          </Row>
        </Card>
      )}

      {viewMode === 'cards' ? (
        data && <RelationshipView data={data} />
      ) : viewMode === 'list' ? (
        <Row gutter={16}>
          {/* Domain Column */}
          <Col span={8}>
            <Card 
              title={
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <ClusterOutlined />
                  <span>Domains ({filteredDomains.length})</span>
                </div>
              }
              extra={
                <Input
                  placeholder="Search..."
                  prefix={<SearchOutlined />}
                  value={domainSearch}
                  onChange={(e) => setDomainSearch(e.target.value)}
                  style={{ width: 150 }}
                  allowClear
                  size="small"
                />
              }
              style={{ height: 'calc(100vh - 280px)' }}
              styles={{ body: { height: 'calc(100% - 57px)', overflow: 'auto', padding: 12 } }}
            >
              <AnimatePresence>
                {filteredDomains.length > 0 ? (
                  filteredDomains.map(domain => (
                    <motion.div
                      key={domain.name}
                      layout
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.2 }}
                      whileHover={{ scale: 1.02 }}
                    >
                      <Card
                        size="small"
                        onClick={() => handleDomainClick(domain.name)}
                        style={{
                          marginBottom: 8,
                          cursor: 'pointer',
                          borderColor: selectedDomain === domain.name ? domainColors[domain.name] || '#8c8c8c' : undefined,
                          borderWidth: selectedDomain === domain.name ? 2 : 1,
                          boxShadow: selectedDomain === domain.name ? `0 4px 12px ${domainColors[domain.name]}40` : undefined,
                          background: selectedDomain === domain.name ? `${domainColors[domain.name]}10` : undefined,
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div>
                            <Tag color={domainColors[domain.name] || '#8c8c8c'} style={{ fontSize: 13, fontWeight: 'bold' }}>
                              {domain.name}
                            </Tag>
                          </div>
                          <Badge 
                            count={domain.featureCount} 
                            style={{ backgroundColor: domainColors[domain.name] || '#8c8c8c' }}
                          />
                        </div>
                        <div style={{ marginTop: 8, display: 'flex', gap: 16 }}>
                          <Text type="secondary" style={{ fontSize: 11 }}>
                            <AppstoreOutlined /> {domain.featureCount} features
                          </Text>
                          <Text type="secondary" style={{ fontSize: 11 }}>
                            <ApiOutlined /> {domain.microserviceCount} services
                          </Text>
                        </div>
                      </Card>
                    </motion.div>
                  ))
                ) : (
                  <Empty description="No domains found" />
                )}
              </AnimatePresence>
            </Card>
          </Col>

          {/* Features Column */}
          <Col span={8}>
            <Card 
              title={
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <AppstoreOutlined />
                  <span>Features ({domainFilteredFeatures.length})</span>
                </div>
              }
              extra={
                <div style={{ display: 'flex', gap: 8 }}>
                  <Input
                    placeholder="Search..."
                    prefix={<SearchOutlined />}
                    value={featureSearch}
                    onChange={(e) => setFeatureSearch(e.target.value)}
                    style={{ width: 120 }}
                    allowClear
                    size="small"
                  />
                  <Select
                    placeholder="Status"
                    value={featureStatusFilter}
                    onChange={setFeatureStatusFilter}
                    style={{ width: 100 }}
                    allowClear
                    size="small"
                  >
                    <Option value="PLANNED">Planned</Option>
                    <Option value="IN_PROGRESS">In Progress</Option>
                    <Option value="RELEASED">Released</Option>
                  </Select>
                </div>
              }
              style={{ height: 'calc(100vh - 280px)' }}
              styles={{ body: { height: 'calc(100% - 57px)', overflow: 'auto', padding: 12 } }}
            >
              <AnimatePresence>
                {domainFilteredFeatures.length > 0 ? (
                  domainFilteredFeatures.map(f => {
                    const state = getFeatureState(f);
                    return (
                      <FeatureCard
                        key={f.id}
                        feature={f}
                        isSelected={state.isSelected}
                        isConnected={state.isConnected}
                        isDimmed={state.isDimmed}
                        onClick={() => handleFeatureClick(f.id)}
                      />
                    );
                  })
                ) : (
                  <Empty description="No features found" />
                )}
              </AnimatePresence>
            </Card>
          </Col>

          {/* Microservices Column */}
          <Col span={8}>
            <Card 
              title={
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <ApiOutlined />
                  <span>Microservices ({domainFilteredMicroservices.length})</span>
                </div>
              }
              extra={
                <div style={{ display: 'flex', gap: 8 }}>
                  <Input
                    placeholder="Search..."
                    prefix={<SearchOutlined />}
                    value={microserviceSearch}
                    onChange={(e) => setMicroserviceSearch(e.target.value)}
                    style={{ width: 120 }}
                    allowClear
                    size="small"
                  />
                  <Select
                    placeholder="Status"
                    value={microserviceStatusFilter}
                    onChange={setMicroserviceStatusFilter}
                    style={{ width: 100 }}
                    allowClear
                    size="small"
                  >
                    <Option value="NOT_STARTED">Not Started</Option>
                    <Option value="IN_PROGRESS">In Progress</Option>
                    <Option value="COMPLETED">Completed</Option>
                  </Select>
                </div>
              }
              style={{ height: 'calc(100vh - 280px)' }}
              styles={{ body: { height: 'calc(100% - 57px)', overflow: 'auto', padding: 12 } }}
            >
              <AnimatePresence>
                {domainFilteredMicroservices.length > 0 ? (
                  domainFilteredMicroservices.map(ms => {
                    const state = getMicroserviceState(ms);
                    return (
                      <MicroserviceCard
                        key={ms.id}
                        microservice={ms}
                        isSelected={state.isSelected}
                        isConnected={state.isConnected}
                        isDimmed={state.isDimmed}
                        onClick={() => handleMicroserviceClick(ms.id)}
                      />
                    );
                  })
                ) : (
                  <Empty description="No microservices found" />
                )}
              </AnimatePresence>
            </Card>
          </Col>
        </Row>
      ) : (
        <div style={{ height: 'calc(100vh - 220px)', width: '100%' }}>
          {data && <RelationshipGraph data={data} />}
        </div>
      )}
    </div>
  );
};

export default RelationshipPage;
