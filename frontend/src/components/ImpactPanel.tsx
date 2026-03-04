import React from 'react';
import { Card, Progress, Tag, List, Typography, Row, Col, Tooltip, Badge, Empty } from 'antd';
import { 
  WarningOutlined, 
  CheckCircleOutlined, 
  ExclamationCircleOutlined,
  ApiOutlined,
  DatabaseOutlined,
  CodeOutlined,
  SettingOutlined,
  CloudServerOutlined
} from '@ant-design/icons';
import { ImpactAnalysisResponse } from '../services/impactService';

const { Text, Title } = Typography;

interface ImpactPanelProps {
  impact: ImpactAnalysisResponse | null;
  loading?: boolean;
}

const ImpactPanel: React.FC<ImpactPanelProps> = ({ impact, loading }) => {
  if (!impact) {
    return (
      <Card title="Impact Analysis" loading={loading}>
        <Empty description="No impact analysis available. Select microservices and change types, then click 'Calculate Impact'." />
      </Card>
    );
  }

  const getRiskLevelColor = (level: string) => {
    switch (level) {
      case 'CRITICAL': return '#ff4d4f';
      case 'HIGH': return '#fa8c16';
      case 'MEDIUM': return '#faad14';
      case 'LOW': return '#52c41a';
      default: return '#d9d9d9';
    }
  };

  const getRiskLevelIcon = (level: string) => {
    switch (level) {
      case 'CRITICAL': return <WarningOutlined style={{ color: '#ff4d4f' }} />;
      case 'HIGH': return <ExclamationCircleOutlined style={{ color: '#fa8c16' }} />;
      case 'MEDIUM': return <ExclamationCircleOutlined style={{ color: '#faad14' }} />;
      case 'LOW': return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      default: return null;
    }
  };

  const getChangeTypeIcon = (changeType: string) => {
    if (changeType.includes('Code')) return <CodeOutlined />;
    if (changeType.includes('Database')) return <DatabaseOutlined />;
    if (changeType.includes('API')) return <ApiOutlined />;
    if (changeType.includes('Config')) return <SettingOutlined />;
    if (changeType.includes('Infrastructure')) return <CloudServerOutlined />;
    return <CodeOutlined />;
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL': return 'red';
      case 'HIGH': return 'orange';
      case 'MEDIUM': return 'blue';
      case 'LOW': return 'green';
      default: return 'default';
    }
  };

  return (
    <Card 
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {getRiskLevelIcon(impact.riskLevel)}
          <span>Impact Analysis</span>
          <Tag color={getRiskLevelColor(impact.riskLevel)}>{impact.riskLevel} RISK</Tag>
        </div>
      }
      loading={loading}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <div style={{ 
            background: '#fafafa', 
            padding: 16, 
            borderRadius: 8, 
            marginBottom: 16,
            border: `2px solid ${getRiskLevelColor(impact.riskLevel)}`
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
              <Text strong>Risk Score</Text>
              <Text style={{ fontSize: 24, fontWeight: 700, color: getRiskLevelColor(impact.riskLevel) }}>
                {impact.riskScore}
              </Text>
            </div>
            <Progress 
              percent={Math.min(impact.riskScore * 5, 100)} 
              strokeColor={getRiskLevelColor(impact.riskLevel)}
              showInfo={false}
            />
            <Text type="secondary" style={{ fontSize: 12, marginTop: 8, display: 'block' }}>
              {impact.analysisSummary}
            </Text>
          </div>
        </Col>

        <Col xs={24} md={12}>
          <Card size="small" title="Impacted Microservices" type="inner">
            {impact.impactedMicroservices?.length > 0 ? (
              <List
                size="small"
                dataSource={impact.impactedMicroservices}
                renderItem={(ms) => (
                  <List.Item>
                    <div style={{ width: '100%' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Text strong>{ms.name}</Text>
                        <Badge 
                          count={ms.riskScore} 
                          style={{ backgroundColor: ms.riskColor }}
                        />
                      </div>
                      <div style={{ marginTop: 4 }}>
                        {ms.changeTypes?.map((ct, idx) => (
                          <Tag key={idx} icon={getChangeTypeIcon(ct)} style={{ marginBottom: 4 }}>
                            {ct}
                          </Tag>
                        ))}
                      </div>
                    </div>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="No microservices" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card size="small" title="Impacted Areas / Domains" type="inner">
            {impact.impactedAreas?.length > 0 ? (
              <List
                size="small"
                dataSource={impact.impactedAreas}
                renderItem={(area) => (
                  <List.Item>
                    <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                      <Text>{area.name}</Text>
                      <Tag color={area.impactLevel >= 3 ? 'red' : area.impactLevel >= 2 ? 'orange' : 'blue'}>
                        Level {area.impactLevel}
                      </Tag>
                    </div>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="No areas identified" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card size="small" title="Critical Checklists" type="inner">
            {impact.criticalChecklists?.length > 0 ? (
              <List
                size="small"
                dataSource={impact.criticalChecklists}
                renderItem={(checklist) => (
                  <List.Item>
                    <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                      <Tooltip title={checklist.name}>
                        <Text ellipsis style={{ maxWidth: 150 }}>{checklist.name}</Text>
                      </Tooltip>
                      <div>
                        <Tag color={getPriorityColor(checklist.priority)}>{checklist.priority}</Tag>
                        <Tag>{checklist.status}</Tag>
                      </div>
                    </div>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="No checklists" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card size="small" title="Recommended Tests" type="inner">
            {impact.recommendedTests?.length > 0 ? (
              <List
                size="small"
                dataSource={impact.recommendedTests.slice(0, 6)}
                renderItem={(test) => (
                  <List.Item>
                    <div style={{ width: '100%' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Text strong style={{ fontSize: 12 }}>{test.testType}</Text>
                        <Tag color={getPriorityColor(test.priority)} style={{ fontSize: 10 }}>
                          {test.priority}
                        </Tag>
                      </div>
                      <Text type="secondary" style={{ fontSize: 11 }}>{test.description}</Text>
                    </div>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="No tests recommended" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>
    </Card>
  );
};

export default ImpactPanel;
