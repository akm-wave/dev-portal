import React, { useState, useMemo } from 'react';
import { Card, Row, Col, Tag, Typography, Input, Select, Collapse, Badge, Space, Button, Empty, Tooltip } from 'antd';
import { 
  FolderOutlined, AppstoreOutlined, ApiOutlined, SearchOutlined, 
  ExpandOutlined, CompressOutlined, RightOutlined, DownOutlined,
  LinkOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { RelationshipData } from '../types/relationship';

const { Title, Text } = Typography;
const { Option } = Select;
const { Panel } = Collapse;

interface RelationshipViewProps {
  data: RelationshipData;
}

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

const statusColors: Record<string, string> = {
  PLANNED: 'blue',
  IN_PROGRESS: 'orange',
  RELEASED: 'green',
  NOT_STARTED: 'default',
  COMPLETED: 'green',
};

const RelationshipView: React.FC<RelationshipViewProps> = ({ data }) => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDomain, setSelectedDomain] = useState<string | null>(null);
  const [expandedDomains, setExpandedDomains] = useState<string[]>([]);
  const [viewMode, setViewMode] = useState<'hierarchy' | 'grid' | 'list'>('hierarchy');

  // Group data by domain
  const domainData = useMemo(() => {
    const domains = [...new Set(data.features.map(f => f.domain || 'General'))];
    
    return domains.map(domain => {
      const features = data.features.filter(f => (f.domain || 'General') === domain);
      const featureIds = features.map(f => f.id);
      
      // Get all microservices connected to features in this domain
      const microserviceIds = new Set<string>();
      featureIds.forEach(fId => {
        const msIds = data.featureToMicroservices?.[fId] || [];
        msIds.forEach(id => microserviceIds.add(id));
      });
      
      const microservices = data.microservices.filter(ms => microserviceIds.has(ms.id));
      
      return {
        name: domain,
        color: domainColors[domain] || '#8c8c8c',
        features,
        microservices,
        featureCount: features.length,
        microserviceCount: microservices.length,
      };
    }).sort((a, b) => b.featureCount - a.featureCount);
  }, [data]);

  // Filter based on search
  const filteredDomains = useMemo(() => {
    if (!searchTerm) return domainData;
    
    const term = searchTerm.toLowerCase();
    return domainData.map(domain => ({
      ...domain,
      features: domain.features.filter(f => 
        f.name.toLowerCase().includes(term) || 
        f.description?.toLowerCase().includes(term)
      ),
      microservices: domain.microservices.filter(ms => 
        ms.name.toLowerCase().includes(term) || 
        ms.description?.toLowerCase().includes(term)
      ),
    })).filter(d => 
      d.name.toLowerCase().includes(term) || 
      d.features.length > 0 || 
      d.microservices.length > 0
    );
  }, [domainData, searchTerm]);

  // Apply domain filter
  const displayDomains = selectedDomain 
    ? filteredDomains.filter(d => d.name === selectedDomain)
    : filteredDomains;

  const toggleDomain = (domain: string) => {
    setExpandedDomains(prev => 
      prev.includes(domain) 
        ? prev.filter(d => d !== domain) 
        : [...prev, domain]
    );
  };

  const expandAll = () => setExpandedDomains(domainData.map(d => d.name));
  const collapseAll = () => setExpandedDomains([]);

  const getMicroservicesForFeature = (featureId: string) => {
    const msIds = data.featureToMicroservices?.[featureId] || [];
    return data.microservices.filter(ms => msIds.includes(ms.id));
  };

  const renderHierarchyView = () => (
    <div>
      {displayDomains.map(domain => (
        <Card 
          key={domain.name}
          style={{ 
            marginBottom: 16, 
            borderLeft: `4px solid ${domain.color}`,
            borderRadius: 8,
          }}
        >
          <div 
            style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center',
              cursor: 'pointer',
              marginBottom: expandedDomains.includes(domain.name) ? 16 : 0,
            }}
            onClick={() => toggleDomain(domain.name)}
          >
            <Space>
              <FolderOutlined style={{ color: domain.color, fontSize: 20 }} />
              <Title level={4} style={{ margin: 0, color: domain.color }}>{domain.name}</Title>
              <Badge count={domain.featureCount} style={{ backgroundColor: domain.color }} />
            </Space>
            <Space>
              <Tag>{domain.featureCount} Features</Tag>
              <Tag color="green">{domain.microserviceCount} Services</Tag>
              {expandedDomains.includes(domain.name) ? <DownOutlined /> : <RightOutlined />}
            </Space>
          </div>

          {expandedDomains.includes(domain.name) && (
            <Row gutter={[16, 16]}>
              {domain.features.map(feature => {
                const connectedMs = getMicroservicesForFeature(feature.id);
                return (
                  <Col xs={24} md={12} lg={8} key={feature.id}>
                    <Card 
                      size="small" 
                      hoverable
                      style={{ 
                        borderRadius: 8,
                        border: `1px solid ${statusColors[feature.status] === 'green' ? '#b7eb8f' : statusColors[feature.status] === 'orange' ? '#ffd591' : '#91caff'}`,
                      }}
                      onClick={() => navigate(`/features/${feature.id}`)}
                    >
                      <div style={{ marginBottom: 8 }}>
                        <Space>
                          <AppstoreOutlined style={{ color: '#6366f1' }} />
                          <Text strong style={{ fontSize: 13 }}>{feature.name}</Text>
                        </Space>
                      </div>
                      <div style={{ marginBottom: 8 }}>
                        <Tag color={statusColors[feature.status]}>{feature.status}</Tag>
                        {feature.releaseVersion && <Tag>v{feature.releaseVersion}</Tag>}
                      </div>
                      {connectedMs.length > 0 && (
                        <div>
                          <Text type="secondary" style={{ fontSize: 11 }}>
                            <LinkOutlined /> Connected Services:
                          </Text>
                          <div style={{ marginTop: 4 }}>
                            {connectedMs.slice(0, 3).map(ms => (
                              <Tooltip key={ms.id} title={ms.description}>
                                <Tag 
                                  color="cyan" 
                                  style={{ fontSize: 10, marginBottom: 2, cursor: 'pointer' }}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(`/microservices/${ms.id}`);
                                  }}
                                >
                                  <ApiOutlined /> {ms.name}
                                </Tag>
                              </Tooltip>
                            ))}
                            {connectedMs.length > 3 && (
                              <Tag style={{ fontSize: 10 }}>+{connectedMs.length - 3} more</Tag>
                            )}
                          </div>
                        </div>
                      )}
                    </Card>
                  </Col>
                );
              })}
            </Row>
          )}
        </Card>
      ))}
    </div>
  );

  const renderGridView = () => (
    <Row gutter={[16, 16]}>
      {displayDomains.map(domain => (
        <Col xs={24} md={12} lg={8} xl={6} key={domain.name}>
          <Card
            style={{ 
              height: '100%',
              borderTop: `4px solid ${domain.color}`,
              borderRadius: 8,
            }}
            hoverable
          >
            <div style={{ textAlign: 'center', marginBottom: 16 }}>
              <FolderOutlined style={{ fontSize: 32, color: domain.color }} />
              <Title level={5} style={{ margin: '8px 0', color: domain.color }}>{domain.name}</Title>
            </div>
            
            <Row gutter={8} style={{ marginBottom: 12 }}>
              <Col span={12}>
                <Card size="small" style={{ textAlign: 'center', background: '#f0f5ff' }}>
                  <Text strong style={{ fontSize: 20, color: '#1890ff' }}>{domain.featureCount}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: 11 }}>Features</Text>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" style={{ textAlign: 'center', background: '#f6ffed' }}>
                  <Text strong style={{ fontSize: 20, color: '#52c41a' }}>{domain.microserviceCount}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: 11 }}>Services</Text>
                </Card>
              </Col>
            </Row>

            <div>
              <Text type="secondary" style={{ fontSize: 11 }}>Top Features:</Text>
              <div style={{ marginTop: 4 }}>
                {domain.features.slice(0, 3).map(f => (
                  <Tag 
                    key={f.id} 
                    color={statusColors[f.status]} 
                    style={{ marginBottom: 4, cursor: 'pointer' }}
                    onClick={() => navigate(`/features/${f.id}`)}
                  >
                    {f.name.length > 15 ? f.name.substring(0, 15) + '...' : f.name}
                  </Tag>
                ))}
                {domain.features.length > 3 && (
                  <Tag>+{domain.features.length - 3}</Tag>
                )}
              </div>
            </div>
          </Card>
        </Col>
      ))}
    </Row>
  );

  const renderListView = () => (
    <Collapse 
      activeKey={expandedDomains}
      onChange={(keys) => setExpandedDomains(keys as string[])}
    >
      {displayDomains.map(domain => (
        <Panel
          key={domain.name}
          header={
            <Space>
              <FolderOutlined style={{ color: domain.color }} />
              <Text strong style={{ color: domain.color }}>{domain.name}</Text>
              <Badge count={domain.featureCount} style={{ backgroundColor: domain.color }} />
              <Tag color="green">{domain.microserviceCount} services</Tag>
            </Space>
          }
        >
          <Row gutter={[8, 8]}>
            <Col span={24}>
              <Text type="secondary" style={{ fontSize: 12 }}>Features:</Text>
            </Col>
            {domain.features.map(feature => (
              <Col key={feature.id}>
                <Tag 
                  color={statusColors[feature.status]}
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/features/${feature.id}`)}
                >
                  <AppstoreOutlined /> {feature.name}
                </Tag>
              </Col>
            ))}
          </Row>
          <Row gutter={[8, 8]} style={{ marginTop: 12 }}>
            <Col span={24}>
              <Text type="secondary" style={{ fontSize: 12 }}>Microservices:</Text>
            </Col>
            {domain.microservices.map(ms => (
              <Col key={ms.id}>
                <Tag 
                  color="cyan"
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/microservices/${ms.id}`)}
                >
                  <ApiOutlined /> {ms.name}
                </Tag>
              </Col>
            ))}
          </Row>
        </Panel>
      ))}
    </Collapse>
  );

  // Summary stats
  const stats = useMemo(() => ({
    totalDomains: domainData.length,
    totalFeatures: data.features.length,
    totalMicroservices: data.microservices.length,
    totalRelationships: data.relationships.length,
  }), [domainData, data]);

  return (
    <div>
      {/* Summary Cards */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card size="small" style={{ textAlign: 'center', borderRadius: 8 }}>
            <FolderOutlined style={{ fontSize: 24, color: '#722ed1' }} />
            <div style={{ fontSize: 24, fontWeight: 700, color: '#722ed1' }}>{stats.totalDomains}</div>
            <Text type="secondary">Domains</Text>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" style={{ textAlign: 'center', borderRadius: 8 }}>
            <AppstoreOutlined style={{ fontSize: 24, color: '#1890ff' }} />
            <div style={{ fontSize: 24, fontWeight: 700, color: '#1890ff' }}>{stats.totalFeatures}</div>
            <Text type="secondary">Features</Text>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" style={{ textAlign: 'center', borderRadius: 8 }}>
            <ApiOutlined style={{ fontSize: 24, color: '#52c41a' }} />
            <div style={{ fontSize: 24, fontWeight: 700, color: '#52c41a' }}>{stats.totalMicroservices}</div>
            <Text type="secondary">Microservices</Text>
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small" style={{ textAlign: 'center', borderRadius: 8 }}>
            <LinkOutlined style={{ fontSize: 24, color: '#fa8c16' }} />
            <div style={{ fontSize: 24, fontWeight: 700, color: '#fa8c16' }}>{stats.totalRelationships}</div>
            <Text type="secondary">Connections</Text>
          </Card>
        </Col>
      </Row>

      {/* Filters and Controls */}
      <Card style={{ marginBottom: 16, borderRadius: 8 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Space wrap>
              <Input
                placeholder="Search domains, features, services..."
                prefix={<SearchOutlined />}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ width: 280 }}
                allowClear
              />
              <Select
                placeholder="Filter by Domain"
                value={selectedDomain}
                onChange={setSelectedDomain}
                style={{ width: 180 }}
                allowClear
              >
                {domainData.map(d => (
                  <Option key={d.name} value={d.name}>
                    <Space>
                      <div style={{ width: 8, height: 8, borderRadius: 4, background: d.color }} />
                      {d.name}
                    </Space>
                  </Option>
                ))}
              </Select>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button.Group>
                <Button 
                  type={viewMode === 'hierarchy' ? 'primary' : 'default'}
                  onClick={() => setViewMode('hierarchy')}
                >
                  Hierarchy
                </Button>
                <Button 
                  type={viewMode === 'grid' ? 'primary' : 'default'}
                  onClick={() => setViewMode('grid')}
                >
                  Grid
                </Button>
                <Button 
                  type={viewMode === 'list' ? 'primary' : 'default'}
                  onClick={() => setViewMode('list')}
                >
                  List
                </Button>
              </Button.Group>
              {viewMode !== 'grid' && (
                <Button.Group>
                  <Button icon={<ExpandOutlined />} onClick={expandAll}>Expand</Button>
                  <Button icon={<CompressOutlined />} onClick={collapseAll}>Collapse</Button>
                </Button.Group>
              )}
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Content */}
      {displayDomains.length === 0 ? (
        <Empty description="No matching results" />
      ) : (
        <>
          {viewMode === 'hierarchy' && renderHierarchyView()}
          {viewMode === 'grid' && renderGridView()}
          {viewMode === 'list' && renderListView()}
        </>
      )}
    </div>
  );
};

export default RelationshipView;
