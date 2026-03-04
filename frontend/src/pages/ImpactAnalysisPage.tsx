import React, { useEffect, useState } from 'react';
import { Card, Select, Button, Space, Typography, Row, Col, Tag, Progress, Alert, Divider, List, Tooltip, Empty, Spin } from 'antd';
import { ThunderboltOutlined, WarningOutlined, CheckCircleOutlined, ExclamationCircleOutlined, SafetyOutlined, ApiOutlined, FolderOutlined, CheckSquareOutlined, ExperimentOutlined } from '@ant-design/icons';
import { impactService, ChangeType, ImpactAnalysisResponse } from '../services/impactService';
import { microserviceService } from '../services/microserviceService';

const { Title, Text, Paragraph } = Typography;

interface Microservice {
  id: string;
  name: string;
  description?: string;
}

interface SelectedChange {
  microserviceId: string;
  microserviceName: string;
  changeTypes: string[];
}

const ImpactAnalysisPage: React.FC = () => {
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [changeTypes, setChangeTypes] = useState<ChangeType[]>([]);
  const [selectedMicroservices, setSelectedMicroservices] = useState<string[]>([]);
  const [selectedChangeTypes, setSelectedChangeTypes] = useState<Record<string, string[]>>({});
  const [loading, setLoading] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [result, setResult] = useState<ImpactAnalysisResponse | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [msData, ctData] = await Promise.all([
        microserviceService.getAll({ size: 500 }),
        impactService.getChangeTypes(),
      ]);
      setMicroservices(msData.content || []);
      setChangeTypes(ctData);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMicroserviceChange = (values: string[]) => {
    setSelectedMicroservices(values);
    // Initialize change types for new selections
    const newChangeTypes = { ...selectedChangeTypes };
    values.forEach(id => {
      if (!newChangeTypes[id]) {
        newChangeTypes[id] = [];
      }
    });
    // Remove unselected
    Object.keys(newChangeTypes).forEach(id => {
      if (!values.includes(id)) {
        delete newChangeTypes[id];
      }
    });
    setSelectedChangeTypes(newChangeTypes);
  };

  const handleChangeTypeChange = (microserviceId: string, values: string[]) => {
    setSelectedChangeTypes(prev => ({
      ...prev,
      [microserviceId]: values,
    }));
  };

  const calculateImpact = async () => {
    const changes = selectedMicroservices
      .filter(id => selectedChangeTypes[id]?.length > 0)
      .map(id => ({
        microserviceId: id,
        changeTypes: selectedChangeTypes[id],
      }));

    if (changes.length === 0) {
      return;
    }

    setCalculating(true);
    try {
      const response = await impactService.previewImpact({
        microserviceChanges: changes,
      });
      setResult(response);
    } catch (error) {
      console.error('Failed to calculate impact:', error);
    } finally {
      setCalculating(false);
    }
  };

  const getRiskColor = (level: string) => {
    switch (level) {
      case 'CRITICAL': return '#ff4d4f';
      case 'HIGH': return '#fa8c16';
      case 'MEDIUM': return '#faad14';
      case 'LOW': return '#52c41a';
      default: return '#d9d9d9';
    }
  };

  const getRiskEmoji = (level: string) => {
    switch (level) {
      case 'CRITICAL': return '🚨';
      case 'HIGH': return '⚠️';
      case 'MEDIUM': return '⚡';
      case 'LOW': return '✅';
      default: return '❓';
    }
  };

  const getRiskDescription = (level: string, score: number) => {
    switch (level) {
      case 'CRITICAL': return `Very High Risk (Score: ${score}) - Requires extensive testing and careful deployment planning. Consider phased rollout.`;
      case 'HIGH': return `High Risk (Score: ${score}) - Significant testing required. Ensure rollback plan is ready.`;
      case 'MEDIUM': return `Moderate Risk (Score: ${score}) - Standard testing procedures recommended.`;
      case 'LOW': return `Low Risk (Score: ${score}) - Minimal impact expected. Basic testing sufficient.`;
      default: return 'Unable to determine risk level.';
    }
  };

  const getChangeTypeLabel = (value: string) => {
    const ct = changeTypes.find(c => c.value === value);
    return ct?.label || value;
  };

  const getChangeTypeColor = (value: string) => {
    const ct = changeTypes.find(c => c.value === value);
    if (!ct) return 'default';
    if (ct.riskWeight >= 5) return 'red';
    if (ct.riskWeight >= 4) return 'orange';
    if (ct.riskWeight >= 3) return 'blue';
    return 'green';
  };

  const canCalculate = selectedMicroservices.some(id => selectedChangeTypes[id]?.length > 0);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Title level={2}>
        <ThunderboltOutlined style={{ marginRight: 12, color: '#faad14' }} />
        Impact Analysis Calculator
      </Title>
      <Paragraph type="secondary">
        Select the microservices you're planning to change and the types of changes you'll make. 
        The calculator will show you what areas might be affected and how risky the changes are.
      </Paragraph>

      <Row gutter={[24, 24]}>
        <Col xs={24} lg={10}>
          <Card title="📋 Step 1: Select What You're Changing" style={{ height: '100%' }}>
            <div style={{ marginBottom: 16 }}>
              <Text strong>Which microservices will you modify?</Text>
              <Select
                mode="multiple"
                placeholder="Click to select microservices..."
                style={{ width: '100%', marginTop: 8 }}
                value={selectedMicroservices}
                onChange={handleMicroserviceChange}
                optionFilterProp="children"
                showSearch
                maxTagCount={5}
              >
                {microservices.map(ms => (
                  <Select.Option key={ms.id} value={ms.id}>{ms.name}</Select.Option>
                ))}
              </Select>
            </div>

            {selectedMicroservices.length > 0 && (
              <div style={{ marginTop: 24 }}>
                <Text strong>What types of changes will you make?</Text>
                <div style={{ marginTop: 12 }}>
                  {selectedMicroservices.map(msId => {
                    const ms = microservices.find(m => m.id === msId);
                    return (
                      <Card 
                        key={msId} 
                        size="small" 
                        style={{ marginBottom: 12, background: '#fafafa' }}
                        title={<><ApiOutlined /> {ms?.name}</>}
                      >
                        <Select
                          mode="multiple"
                          placeholder="Select change types..."
                          style={{ width: '100%' }}
                          value={selectedChangeTypes[msId] || []}
                          onChange={(values) => handleChangeTypeChange(msId, values)}
                          tagRender={(props) => (
                            <Tag color={getChangeTypeColor(props.value as string)} closable={props.closable} onClose={props.onClose}>
                              {getChangeTypeLabel(props.value as string)}
                            </Tag>
                          )}
                        >
                          {changeTypes.map(ct => (
                            <Select.Option key={ct.value} value={ct.value}>
                              <Space>
                                <span>{ct.label}</span>
                                <Tag color={ct.riskWeight >= 5 ? 'red' : ct.riskWeight >= 4 ? 'orange' : ct.riskWeight >= 3 ? 'blue' : 'green'}>
                                  Risk: {ct.riskWeight}
                                </Tag>
                              </Space>
                            </Select.Option>
                          ))}
                        </Select>
                      </Card>
                    );
                  })}
                </div>
              </div>
            )}

            <Button
              type="primary"
              size="large"
              icon={<ThunderboltOutlined />}
              onClick={calculateImpact}
              loading={calculating}
              disabled={!canCalculate}
              style={{ marginTop: 24, width: '100%' }}
            >
              Calculate Impact
            </Button>
          </Card>
        </Col>

        <Col xs={24} lg={14}>
          {result ? (
            <Card title="📊 Impact Analysis Results">
              {/* Risk Summary */}
              <Alert
                message={
                  <Space>
                    <span style={{ fontSize: 24 }}>{getRiskEmoji(result.riskLevel)}</span>
                    <span style={{ fontSize: 18, fontWeight: 600 }}>{result.riskLevel} RISK</span>
                  </Space>
                }
                description={getRiskDescription(result.riskLevel, result.riskScore)}
                type={result.riskLevel === 'CRITICAL' ? 'error' : result.riskLevel === 'HIGH' ? 'warning' : result.riskLevel === 'MEDIUM' ? 'info' : 'success'}
                showIcon
                icon={result.riskLevel === 'CRITICAL' ? <WarningOutlined /> : result.riskLevel === 'HIGH' ? <ExclamationCircleOutlined /> : result.riskLevel === 'MEDIUM' ? <ExclamationCircleOutlined /> : <CheckCircleOutlined />}
                style={{ marginBottom: 24 }}
              />

              {/* Risk Score Gauge */}
              <div style={{ textAlign: 'center', marginBottom: 24 }}>
                <Progress
                  type="dashboard"
                  percent={Math.min(result.riskScore * 5, 100)}
                  strokeColor={getRiskColor(result.riskLevel)}
                  format={() => (
                    <div>
                      <div style={{ fontSize: 28, fontWeight: 700, color: getRiskColor(result.riskLevel) }}>
                        {result.riskScore}
                      </div>
                      <div style={{ fontSize: 12, color: '#888' }}>Risk Score</div>
                    </div>
                  )}
                  size={150}
                />
              </div>

              <Divider />

              {/* What's Being Changed */}
              <div style={{ marginBottom: 24 }}>
                <Title level={5}><ApiOutlined /> What You're Changing ({result.impactedMicroservices?.length || 0} microservices)</Title>
                <Row gutter={[8, 8]}>
                  {result.impactedMicroservices?.map((ms) => (
                    <Col key={ms.id} xs={24} sm={12}>
                      <Card size="small" style={{ borderLeft: `4px solid ${ms.riskColor}` }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Text strong>{ms.name}</Text>
                          <Tag color={ms.riskScore >= 8 ? 'red' : ms.riskScore >= 5 ? 'orange' : 'blue'}>
                            Risk: {ms.riskScore}
                          </Tag>
                        </div>
                        <div style={{ marginTop: 8 }}>
                          {ms.changeTypes?.map((ct, idx) => (
                            <Tag key={idx} style={{ marginBottom: 4 }}>{ct}</Tag>
                          ))}
                        </div>
                      </Card>
                    </Col>
                  ))}
                </Row>
              </div>

              {/* Affected Areas */}
              {result.impactedAreas?.length > 0 && (
                <div style={{ marginBottom: 24 }}>
                  <Title level={5}><FolderOutlined /> Areas That May Be Affected</Title>
                  <Paragraph type="secondary" style={{ marginBottom: 12 }}>
                    These business areas/domains might be impacted by your changes:
                  </Paragraph>
                  <Space wrap>
                    {result.impactedAreas.map((area, idx) => (
                      <Tag 
                        key={idx} 
                        color={area.impactLevel >= 3 ? 'red' : area.impactLevel >= 2 ? 'orange' : 'blue'}
                        style={{ padding: '4px 12px', fontSize: 14 }}
                      >
                        {area.name}
                      </Tag>
                    ))}
                  </Space>
                </div>
              )}

              {/* Checklists to Review */}
              {result.criticalChecklists?.length > 0 && (
                <div style={{ marginBottom: 24 }}>
                  <Title level={5}><CheckSquareOutlined /> Checklists to Review ({result.criticalChecklists.length})</Title>
                  <Paragraph type="secondary" style={{ marginBottom: 12 }}>
                    Make sure to go through these checklists before deployment:
                  </Paragraph>
                  <List
                    size="small"
                    dataSource={result.criticalChecklists.slice(0, 10)}
                    renderItem={(item) => (
                      <List.Item>
                        <Space>
                          <CheckSquareOutlined />
                          <Text>{item.name}</Text>
                          <Tag color={item.priority === 'HIGH' ? 'red' : item.priority === 'MEDIUM' ? 'orange' : 'blue'}>
                            {item.priority}
                          </Tag>
                        </Space>
                      </List.Item>
                    )}
                  />
                  {result.criticalChecklists.length > 10 && (
                    <Text type="secondary">...and {result.criticalChecklists.length - 10} more</Text>
                  )}
                </div>
              )}

              {/* Recommended Tests */}
              {result.recommendedTests?.length > 0 && (
                <div style={{ marginBottom: 24 }}>
                  <Title level={5}><ExperimentOutlined /> Recommended Testing</Title>
                  <Paragraph type="secondary" style={{ marginBottom: 12 }}>
                    Based on your changes, we recommend these tests:
                  </Paragraph>
                  <List
                    size="small"
                    dataSource={result.recommendedTests.slice(0, 8)}
                    renderItem={(item) => (
                      <List.Item>
                        <div style={{ width: '100%' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Text strong>{item.testType}</Text>
                            <Tag color={item.priority === 'CRITICAL' ? 'red' : item.priority === 'HIGH' ? 'orange' : 'blue'}>
                              {item.priority}
                            </Tag>
                          </div>
                          <Text type="secondary" style={{ fontSize: 12 }}>{item.description}</Text>
                        </div>
                      </List.Item>
                    )}
                  />
                </div>
              )}

              {/* Summary */}
              <Alert
                message="Summary"
                description={result.analysisSummary}
                type="info"
                showIcon
                icon={<SafetyOutlined />}
              />
            </Card>
          ) : (
            <Card style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 400 }}>
              <Empty 
                description={
                  <div>
                    <Title level={4} type="secondary">No Analysis Yet</Title>
                    <Paragraph type="secondary">
                      Select microservices and change types on the left, then click "Calculate Impact" to see the analysis.
                    </Paragraph>
                  </div>
                }
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ImpactAnalysisPage;
