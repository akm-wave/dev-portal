import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card, Tabs, Tag, Progress, Spin, Typography, Row, Col, Statistic, Badge,
  Table, Input, Select, Button, Tooltip, message, Checkbox, Space, Collapse, Empty, Upload, Popconfirm
} from 'antd';
import {
  ArrowLeftOutlined, ApiOutlined, CheckSquareOutlined, AppstoreOutlined,
  EditOutlined, PaperClipOutlined, WarningOutlined, ClockCircleOutlined,
  FilterOutlined, SearchOutlined, UploadOutlined, DownloadOutlined, DeleteOutlined
} from '@ant-design/icons';
import { featureDetailsService } from '../services/featureDetailsService';
import { domainService } from '../services/domainService';
import { FeatureDetails, CheckpointAnalysis, MicroserviceAnalysis, ChecklistStatus, Domain } from '../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { Panel } = Collapse;
const { Option } = Select;

const statusColors: Record<string, string> = {
  PLANNED: 'blue',
  IN_PROGRESS: 'orange',
  RELEASED: 'green',
  NOT_STARTED: 'default',
  COMPLETED: 'green',
  PENDING: 'default',
  DONE: 'green',
  BLOCKED: 'red',
};

const FeatureDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [details, setDetails] = useState<FeatureDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [checkpointSearch, setCheckpointSearch] = useState('');
  const [checkpointStatusFilter, setCheckpointStatusFilter] = useState<string | null>(null);
  const [checkpointMsFilter, setCheckpointMsFilter] = useState<string | null>(null);
  const [showOnlyPending, setShowOnlyPending] = useState(false);
  const [domains, setDomains] = useState<Domain[]>([]);

  // Build domain colors map from loaded domains
  const domainColors: Record<string, string> = domains.reduce((acc, d) => {
    acc[d.name] = d.colorCode || '#8c8c8c';
    return acc;
  }, {} as Record<string, string>);

  const loadDomains = useCallback(async () => {
    try {
      const data = await domainService.getAll();
      setDomains(data);
    } catch (error) {
      console.error('Failed to load domains');
    }
  }, []);

  const loadDetails = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await featureDetailsService.getFeatureDetails(id);
      setDetails(data);
    } catch (error) {
      message.error('Failed to load feature details');
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadDetails();
    loadDomains();
  }, [loadDetails, loadDomains]);

  const handleCheckpointStatusChange = async (checkpoint: CheckpointAnalysis, newStatus: ChecklistStatus) => {
    if (!id) return;
    try {
      await featureDetailsService.updateCheckpointProgress(id, checkpoint.id, { status: newStatus });
      message.success('Checkpoint status updated');
      loadDetails();
    } catch (error) {
      message.error('Failed to update checkpoint status');
    }
  };

  const handleRemarkSave = async (checkpoint: CheckpointAnalysis, remark: string) => {
    if (!id) return;
    try {
      await featureDetailsService.updateCheckpointProgress(id, checkpoint.id, { remark });
      message.success('Remark saved');
      loadDetails();
    } catch (error) {
      message.error('Failed to save remark');
    }
  };

  const handleUploadAttachment = async (checkpoint: CheckpointAnalysis, file: File) => {
    if (!id) return;
    try {
      await featureDetailsService.uploadCheckpointAttachment(id, checkpoint.id, file);
      message.success('Attachment uploaded');
      loadDetails();
    } catch (error) {
      message.error('Failed to upload attachment');
    }
  };

  const handleDownloadAttachment = async (checkpoint: CheckpointAnalysis) => {
    if (!id || !checkpoint.mongoFileId) return;
    try {
      const blob = await featureDetailsService.downloadCheckpointAttachment(id, checkpoint.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = checkpoint.attachmentFilename || 'attachment';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      message.error('Failed to download attachment');
    }
  };

  const handleDeleteAttachment = async (checkpoint: CheckpointAnalysis) => {
    if (!id) return;
    try {
      await featureDetailsService.deleteCheckpointAttachment(id, checkpoint.id);
      message.success('Attachment deleted');
      loadDetails();
    } catch (error) {
      message.error('Failed to delete attachment');
    }
  };

  const filteredCheckpoints = details?.checkpoints.filter(cp => {
    const matchesSearch = !checkpointSearch ||
      cp.name.toLowerCase().includes(checkpointSearch.toLowerCase()) ||
      cp.description?.toLowerCase().includes(checkpointSearch.toLowerCase());
    const matchesStatus = !checkpointStatusFilter || cp.featureStatus === checkpointStatusFilter;
    const matchesMs = !checkpointMsFilter || cp.connectedMicroservices.includes(checkpointMsFilter);
    const matchesPending = !showOnlyPending || cp.featureStatus !== 'DONE';
    return matchesSearch && matchesStatus && matchesMs && matchesPending;
  }) || [];

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!details) {
    return <Empty description="Feature not found" />;
  }

  const checkpointColumns = [
    {
      title: '',
      dataIndex: 'featureStatus',
      key: 'checkbox',
      width: 50,
      render: (status: ChecklistStatus, record: CheckpointAnalysis) => (
        <Checkbox
          checked={status === 'DONE'}
          onChange={(e) => handleCheckpointStatusChange(record, e.target.checked ? 'DONE' : 'PENDING')}
        />
      ),
    },
    {
      title: 'Checkpoint',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: CheckpointAnalysis) => (
        <div>
          <Text strong>{name}</Text>
          {record.description && (
            <div><Text type="secondary" style={{ fontSize: 12 }}>{record.description}</Text></div>
          )}
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'featureStatus',
      key: 'status',
      width: 120,
      render: (status: ChecklistStatus, record: CheckpointAnalysis) => (
        <Select
          value={status}
          size="small"
          style={{ width: 110 }}
          onChange={(value) => handleCheckpointStatusChange(record, value)}
        >
          <Option value="PENDING">Pending</Option>
          <Option value="IN_PROGRESS">In Progress</Option>
          <Option value="DONE">Done</Option>
          <Option value="BLOCKED">Blocked</Option>
        </Select>
      ),
    },
    {
      title: 'Connected Services',
      dataIndex: 'connectedMicroservices',
      key: 'microservices',
      render: (services: string[]) => (
        <div>
          {services.map(s => (
            <Tag key={s} color="blue" style={{ marginBottom: 2 }}>{s}</Tag>
          ))}
        </div>
      ),
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      render: (priority: string) => (
        <Tag color={priority === 'HIGH' ? 'red' : priority === 'MEDIUM' ? 'orange' : 'default'}>
          {priority}
        </Tag>
      ),
    },
    {
      title: 'Remark',
      dataIndex: 'remark',
      key: 'remark',
      width: 200,
      render: (remark: string | null, record: CheckpointAnalysis) => (
        <Input.TextArea
          defaultValue={remark || ''}
          placeholder="Add remark..."
          autoSize={{ minRows: 1, maxRows: 3 }}
          onBlur={(e) => {
            if (e.target.value !== (remark || '')) {
              handleRemarkSave(record, e.target.value);
            }
          }}
          style={{ fontSize: 12 }}
        />
      ),
    },
    {
      title: 'Attachment',
      key: 'attachment',
      width: 120,
      render: (_: unknown, record: CheckpointAnalysis) => (
        <Space size="small">
          {record.mongoFileId ? (
            <>
              <Tooltip title={record.attachmentFilename || 'Download'}>
                <Button 
                  type="link" 
                  size="small"
                  icon={<DownloadOutlined />} 
                  onClick={() => handleDownloadAttachment(record)}
                />
              </Tooltip>
              <Popconfirm title="Delete attachment?" onConfirm={() => handleDeleteAttachment(record)}>
                <Button type="link" size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </>
          ) : (
            <Upload
              beforeUpload={(file) => {
                handleUploadAttachment(record, file);
                return false;
              }}
              showUploadList={false}
            >
              <Button type="link" size="small" icon={<UploadOutlined />}>
                Upload
              </Button>
            </Upload>
          )}
        </Space>
      ),
    },
    {
      title: 'Updated',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 120,
      render: (date: string, record: CheckpointAnalysis) => (
        <Tooltip title={record.updatedBy ? `By ${record.updatedBy}` : ''}>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {dayjs(date).format('MMM D, HH:mm')}
          </Text>
        </Tooltip>
      ),
    },
  ];

  const uniqueMicroservices = [...new Set(details.checkpoints.flatMap(cp => cp.connectedMicroservices))];

  return (
    <div>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/features')}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Back to Features
      </Button>

      <Card style={{ marginBottom: 16 }}>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <Title level={3} style={{ margin: 0 }}>{details.name}</Title>
              <Tag color={domainColors[details.domain] || domainColors['General']} style={{ fontSize: 14 }}>
                {details.domain}
              </Tag>
              <Tag color={statusColors[details.status]}>{details.status.replace('_', ' ')}</Tag>
            </div>
            {details.description && (
              <Text type="secondary">{details.description}</Text>
            )}
            <div style={{ marginTop: 8 }}>
              {details.releaseVersion && <Tag>v{details.releaseVersion}</Tag>}
              {details.targetDate && (
                <Tag icon={<ClockCircleOutlined />}>
                  Target: {dayjs(details.targetDate).format('MMM D, YYYY')}
                </Tag>
              )}
            </div>
          </Col>
          <Col>
            <Progress
              type="circle"
              percent={details.overallProgress}
              width={80}
              status={details.overallProgress === 100 ? 'success' : 'active'}
            />
          </Col>
        </Row>
      </Card>

      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab={<span><AppstoreOutlined /> Overview</span>} key="overview">
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Total Microservices"
                  value={details.totalMicroservices}
                  prefix={<ApiOutlined style={{ color: '#1890ff' }} />}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Unique Checkpoints"
                  value={details.totalUniqueCheckpoints}
                  prefix={<CheckSquareOutlined style={{ color: '#52c41a' }} />}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Completed"
                  value={details.checkpoints.filter(c => c.featureStatus === 'DONE').length}
                  suffix={`/ ${details.totalUniqueCheckpoints}`}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Blocked"
                  value={details.checkpoints.filter(c => c.featureStatus === 'BLOCKED').length}
                  valueStyle={{ color: '#f5222d' }}
                  prefix={<WarningOutlined />}
                />
              </Card>
            </Col>
          </Row>

          <Card title="Feature Information" style={{ marginTop: 16 }}>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Text type="secondary">Domain:</Text>
                <div>
                  <Tag color={domainColors[details.domain] || domainColors['General']}>
                    {details.domain}
                  </Tag>
                </div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Status:</Text>
                <div><Tag color={statusColors[details.status]}>{details.status}</Tag></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Release Version:</Text>
                <div><Text strong>{details.releaseVersion || 'Not set'}</Text></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Target Date:</Text>
                <div>
                  <Text strong>
                    {details.targetDate ? dayjs(details.targetDate).format('MMMM D, YYYY') : 'Not set'}
                  </Text>
                </div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Created:</Text>
                <div><Text>{dayjs(details.createdAt).format('MMMM D, YYYY HH:mm')}</Text></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Last Updated:</Text>
                <div><Text>{dayjs(details.updatedAt).format('MMMM D, YYYY HH:mm')}</Text></div>
              </Col>
            </Row>
          </Card>
        </TabPane>

        <TabPane tab={<span><ApiOutlined /> Microservice Analysis</span>} key="microservices">
          <Row gutter={[16, 16]}>
            {details.microservices.map(ms => (
              <Col xs={24} lg={12} key={ms.id}>
                <Collapse>
                  <Panel
                    header={
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                        <div>
                          <Text strong>{ms.name}</Text>
                          {ms.highRisk && (
                            <Tag color="red" style={{ marginLeft: 8 }}>HIGH IMPACT</Tag>
                          )}
                          <Tag color={statusColors[ms.status]} style={{ marginLeft: 8 }}>
                            {ms.status.replace('_', ' ')}
                          </Tag>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                          <Text type="secondary">
                            {ms.completedCheckpoints}/{ms.totalCheckpoints} checkpoints
                          </Text>
                          <Progress
                            percent={ms.progressPercentage}
                            size="small"
                            style={{ width: 100, margin: 0 }}
                          />
                        </div>
                      </div>
                    }
                    key={ms.id}
                  >
                    <div style={{ marginBottom: 8 }}>
                      <Text type="secondary">{ms.description}</Text>
                    </div>
                    <div style={{ marginBottom: 12 }}>
                      {ms.owner && <Tag>Owner: {ms.owner}</Tag>}
                      {ms.version && <Tag>v{ms.version}</Tag>}
                      <Tag>Used in {ms.featureCount} features</Tag>
                    </div>
                    <div>
                      <Text strong style={{ marginBottom: 8, display: 'block' }}>Checkpoints:</Text>
                      {ms.checkpoints.map(cp => (
                        <div
                          key={cp.id}
                          style={{
                            padding: '4px 8px',
                            marginBottom: 4,
                            borderRadius: 4,
                            background: cp.status === 'DONE' ? '#f6ffed' :
                                       cp.status === 'BLOCKED' ? '#fff2f0' : '#fafafa',
                            border: `1px solid ${cp.status === 'DONE' ? '#b7eb8f' :
                                                 cp.status === 'BLOCKED' ? '#ffccc7' : '#d9d9d9'}`
                          }}
                        >
                          <Tag color={statusColors[cp.status]} style={{ marginRight: 8 }}>
                            {cp.status}
                          </Tag>
                          {cp.name}
                        </div>
                      ))}
                    </div>
                  </Panel>
                </Collapse>
              </Col>
            ))}
          </Row>
        </TabPane>

        <TabPane tab={<span><CheckSquareOutlined /> Checkpoint Analysis</span>} key="checkpoints">
          <Card style={{ marginBottom: 16 }}>
            <Row gutter={16} align="middle">
              <Col flex="auto">
                <Space wrap>
                  <Input
                    placeholder="Search checkpoints..."
                    prefix={<SearchOutlined />}
                    value={checkpointSearch}
                    onChange={(e) => setCheckpointSearch(e.target.value)}
                    style={{ width: 200 }}
                    allowClear
                  />
                  <Select
                    placeholder="Filter by status"
                    value={checkpointStatusFilter}
                    onChange={setCheckpointStatusFilter}
                    style={{ width: 140 }}
                    allowClear
                  >
                    <Option value="PENDING">Pending</Option>
                    <Option value="IN_PROGRESS">In Progress</Option>
                    <Option value="DONE">Done</Option>
                    <Option value="BLOCKED">Blocked</Option>
                  </Select>
                  <Select
                    placeholder="Filter by service"
                    value={checkpointMsFilter}
                    onChange={setCheckpointMsFilter}
                    style={{ width: 160 }}
                    allowClear
                  >
                    {uniqueMicroservices.map(ms => (
                      <Option key={ms} value={ms}>{ms}</Option>
                    ))}
                  </Select>
                  <Checkbox
                    checked={showOnlyPending}
                    onChange={(e) => setShowOnlyPending(e.target.checked)}
                  >
                    Show only pending
                  </Checkbox>
                </Space>
              </Col>
              <Col>
                <Badge count={filteredCheckpoints.length} showZero>
                  <Tag icon={<FilterOutlined />}>Results</Tag>
                </Badge>
              </Col>
            </Row>
          </Card>

          <Table
            dataSource={filteredCheckpoints}
            columns={checkpointColumns}
            rowKey="id"
            pagination={{ pageSize: 20 }}
            size="small"
            rowClassName={(record) =>
              record.featureStatus === 'BLOCKED' ? 'checkpoint-blocked' :
              record.featureStatus === 'DONE' ? 'checkpoint-done' : ''
            }
          />
        </TabPane>

        <TabPane tab={<span><AppstoreOutlined /> Domain Relationships</span>} key="relationships">
          <DomainRelationshipsTab details={details} domainColors={domainColors} />
        </TabPane>

      </Tabs>

      <style>{`
        .checkpoint-blocked {
          background-color: #fff2f0 !important;
        }
        .checkpoint-done {
          background-color: #f6ffed !important;
        }
      `}</style>
    </div>
  );
};

const DomainRelationshipsTab: React.FC<{ details: FeatureDetails; domainColors: Record<string, string> }> = ({ details, domainColors }) => {
  return (
    <Card>
      <div style={{ marginBottom: 16 }}>
        <Title level={5}>Domain: {details.domain}</Title>
        <Tag color={domainColors[details.domain] || '#8c8c8c'} style={{ fontSize: 14 }}>
          {details.domain}
        </Tag>
      </div>

      <Collapse defaultActiveKey={['feature']}>
        <Panel
          header={
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>
                <AppstoreOutlined style={{ marginRight: 8 }} />
                Feature: {details.name}
              </span>
              <div>
                <Tag>{details.totalMicroservices} microservices</Tag>
                <Tag>{details.totalUniqueCheckpoints} checkpoints</Tag>
              </div>
            </div>
          }
          key="feature"
        >
          <Collapse>
            {details.microservices.map(ms => (
              <Panel
                header={
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>
                      <ApiOutlined style={{ marginRight: 8 }} />
                      {ms.name}
                      {ms.highRisk && <Tag color="red" style={{ marginLeft: 8 }}>HIGH IMPACT</Tag>}
                    </span>
                    <Tag>{ms.totalCheckpoints} checkpoints</Tag>
                  </div>
                }
                key={ms.id}
              >
                <div>
                  <Text type="secondary">{ms.description}</Text>
                  <div style={{ marginTop: 8 }}>
                    {ms.checkpoints.map(cp => (
                      <Tag
                        key={cp.id}
                        color={statusColors[cp.status]}
                        style={{ marginBottom: 4 }}
                      >
                        {cp.name}
                      </Tag>
                    ))}
                  </div>
                </div>
              </Panel>
            ))}
          </Collapse>
        </Panel>
      </Collapse>

      <Card title="Summary" style={{ marginTop: 16 }} size="small">
        <Row gutter={16}>
          <Col span={8}>
            <Statistic title="Total Features in Domain" value={1} />
          </Col>
          <Col span={8}>
            <Statistic title="Total Microservices" value={details.totalMicroservices} />
          </Col>
          <Col span={8}>
            <Statistic title="Unique Checkpoints" value={details.totalUniqueCheckpoints} />
          </Col>
        </Row>
      </Card>
    </Card>
  );
};

export default FeatureDetailsPage;
