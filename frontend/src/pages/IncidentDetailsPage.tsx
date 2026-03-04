import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Typography, Row, Col, Tag, Button, Empty, message, Tabs, Progress, Table, Input, Select, Tooltip, Upload, Popconfirm, Space } from 'antd';
import { ArrowLeftOutlined, WarningOutlined, ApiOutlined, CheckSquareOutlined, SearchOutlined, UploadOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons';
import { incidentService } from '../services/incidentService';
import { ChecklistProgressUpdateRequest, ChecklistStatus } from '../types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { Option } = Select;

interface CheckpointAnalysis {
  id: string;
  name: string;
  description: string;
  originalStatus: ChecklistStatus;
  incidentStatus: ChecklistStatus;
  priority: string;
  remark: string | null;
  mongoFileId: string | null;
  attachmentFilename: string | null;
  updatedBy: string | null;
  updatedAt: string;
  connectedMicroservices: string[];
  connectedMicroserviceIds: string[];
}

interface IncidentDetails {
  id: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  createdBy: string;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
  mainFeature: { id: string; name: string; domain: string };
  owner: { id: string; username: string; fullName: string } | null;
  totalMicroservices: number;
  totalUniqueCheckpoints: number;
  overallProgress: number;
  microservices: any[];
  checkpoints: CheckpointAnalysis[];
}

const severityColors: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  CRITICAL: 'red',
};

const statusColors: Record<string, string> = {
  OPEN: 'red',
  IN_PROGRESS: 'orange',
  RESOLVED: 'green',
  CLOSED: 'default',
};

const checklistStatusColors: Record<string, string> = {
  PENDING: 'default',
  IN_PROGRESS: 'blue',
  DONE: 'green',
  BLOCKED: 'red',
};

const priorityColors: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
};

const IncidentDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [details, setDetails] = useState<IncidentDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [checkpointSearch, setCheckpointSearch] = useState('');
  const [checkpointStatusFilter, setCheckpointStatusFilter] = useState<string | null>(null);
  const [showOnlyPending, setShowOnlyPending] = useState(false);

  const loadDetails = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await incidentService.getDetails(id);
      setDetails(data);
    } catch (error) {
      message.error('Failed to load incident details');
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadDetails();
  }, [loadDetails]);

  const handleStatusChange = async (checkpoint: CheckpointAnalysis, newStatus: ChecklistStatus) => {
    if (!id) return;
    try {
      await incidentService.updateChecklistStatus(id, checkpoint.id, { status: newStatus });
      message.success('Status updated');
      loadDetails();
    } catch (error) {
      message.error('Failed to update status');
    }
  };

  const handleRemarkSave = async (checkpoint: CheckpointAnalysis, remark: string) => {
    if (!id) return;
    try {
      await incidentService.updateChecklistStatus(id, checkpoint.id, { remark });
      message.success('Remark saved');
      loadDetails();
    } catch (error) {
      message.error('Failed to save remark');
    }
  };

  const handleUploadAttachment = async (checkpoint: CheckpointAnalysis, file: File) => {
    if (!id) return;
    try {
      await incidentService.uploadChecklistAttachment(id, checkpoint.id, file);
      message.success('Attachment uploaded');
      loadDetails();
    } catch (error) {
      message.error('Failed to upload attachment');
    }
  };

  const handleDownloadAttachment = async (checkpoint: CheckpointAnalysis) => {
    if (!id || !checkpoint.mongoFileId) return;
    try {
      const blob = await incidentService.downloadChecklistAttachment(id, checkpoint.id);
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
      await incidentService.deleteChecklistAttachment(id, checkpoint.id);
      message.success('Attachment deleted');
      loadDetails();
    } catch (error) {
      message.error('Failed to delete attachment');
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!details) {
    return <Empty description="Incident not found" />;
  }

  const filteredCheckpoints = details.checkpoints.filter(cp => {
    const matchesSearch = !checkpointSearch || 
      cp.name.toLowerCase().includes(checkpointSearch.toLowerCase()) ||
      cp.description?.toLowerCase().includes(checkpointSearch.toLowerCase());
    const matchesStatus = !checkpointStatusFilter || cp.incidentStatus === checkpointStatusFilter;
    const matchesPending = !showOnlyPending || cp.incidentStatus !== 'DONE';
    return matchesSearch && matchesStatus && matchesPending;
  });

  const checkpointColumns: ColumnsType<CheckpointAnalysis> = [
    {
      title: 'Checkpoint',
      key: 'checkpoint',
      width: 280,
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 500 }}>{record.name}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.description}</Text>
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'incidentStatus',
      key: 'status',
      width: 130,
      render: (status: ChecklistStatus, record) => (
        <Select
          value={status}
          size="small"
          style={{ width: 110 }}
          onChange={(val) => handleStatusChange(record, val)}
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
      key: 'services',
      width: 180,
      render: (services: string[]) => (
        <div>
          {services.map((s, i) => (
            <Tag key={i} color="blue" style={{ marginBottom: 2, fontSize: 11 }}>{s}</Tag>
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
        <Tag color={priorityColors[priority] || 'default'}>{priority}</Tag>
      ),
    },
    {
      title: 'Remark',
      dataIndex: 'remark',
      key: 'remark',
      width: 180,
      render: (remark: string | null, record) => (
        <Input
          size="small"
          placeholder="Add remark..."
          defaultValue={remark || ''}
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
      render: (_, record) => (
        <Space size="small">
          {record.mongoFileId ? (
            <>
              <Tooltip title={record.attachmentFilename || 'Download'}>
                <Button type="link" size="small" icon={<DownloadOutlined />} onClick={() => handleDownloadAttachment(record)} />
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
              <Button type="link" size="small" icon={<UploadOutlined />}>Upload</Button>
            </Upload>
          )}
        </Space>
      ),
    },
    {
      title: 'Updated',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 100,
      render: (date: string, record) => (
        <Tooltip title={record.updatedBy ? `By ${record.updatedBy}` : ''}>
          <Text type="secondary" style={{ fontSize: 11 }}>
            {dayjs(date).format('MMM DD, HH:mm')}
          </Text>
        </Tooltip>
      ),
    },
  ];

  return (
    <div>
      <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/incidents')} style={{ marginBottom: 16, padding: 0 }}>
        Back to Incidents
      </Button>

      <Card style={{ marginBottom: 16 }}>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <WarningOutlined style={{ fontSize: 24, color: '#f5222d' }} />
              <Title level={3} style={{ margin: 0 }}>{details.title}</Title>
              <Tag color={severityColors[details.severity]}>{details.severity}</Tag>
              <Tag color={statusColors[details.status]}>{details.status}</Tag>
            </div>
            {details.description && <Text type="secondary">{details.description}</Text>}
          </Col>
          <Col>
            <Progress type="circle" percent={details.overallProgress} size={80} strokeColor="#52c41a" />
          </Col>
        </Row>
      </Card>

      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab={<span><WarningOutlined /> Overview</span>} key="overview">
          <Card>
            <Row gutter={[24, 16]}>
              <Col span={8}>
                <Text type="secondary">Main Feature</Text>
                <div><Tag color="purple">{details.mainFeature.name}</Tag></div>
              </Col>
              <Col span={8}>
                <Text type="secondary">Domain</Text>
                <div><Tag>{details.mainFeature.domain}</Tag></div>
              </Col>
              <Col span={8}>
                <Text type="secondary">Owner</Text>
                <div>{details.owner?.username || '-'}</div>
              </Col>
              <Col span={8}>
                <Text type="secondary">Created By</Text>
                <div>{details.createdBy}</div>
              </Col>
              <Col span={8}>
                <Text type="secondary">Created At</Text>
                <div>{dayjs(details.createdAt).format('YYYY-MM-DD HH:mm')}</div>
              </Col>
              <Col span={8}>
                <Text type="secondary">Resolved At</Text>
                <div>{details.resolvedAt ? dayjs(details.resolvedAt).format('YYYY-MM-DD HH:mm') : '-'}</div>
              </Col>
            </Row>
          </Card>
        </TabPane>

        <TabPane tab={<span><ApiOutlined /> Microservice Analysis</span>} key="microservices">
          <Row gutter={[16, 16]}>
            {details.microservices.map((ms) => (
              <Col xs={24} md={12} lg={8} key={ms.id}>
                <Card size="small" title={ms.name} extra={<Tag color={ms.highRisk ? 'red' : 'default'}>{ms.status}</Tag>}>
                  <Progress percent={ms.progressPercentage} size="small" />
                  <div style={{ marginTop: 8 }}>
                    <Text type="secondary">{ms.completedCheckpoints}/{ms.totalCheckpoints} checkpoints</Text>
                  </div>
                </Card>
              </Col>
            ))}
          </Row>
        </TabPane>

        <TabPane tab={<span><CheckSquareOutlined /> Checkpoint Analysis</span>} key="checkpoints">
          <Card>
            <div style={{ marginBottom: 16, display: 'flex', gap: 16, flexWrap: 'wrap' }}>
              <Input
                placeholder="Search checkpoints..."
                prefix={<SearchOutlined />}
                value={checkpointSearch}
                onChange={(e) => setCheckpointSearch(e.target.value)}
                style={{ width: 250 }}
              />
              <Select
                placeholder="Filter by status"
                allowClear
                value={checkpointStatusFilter}
                onChange={setCheckpointStatusFilter}
                style={{ width: 150 }}
              >
                <Option value="PENDING">Pending</Option>
                <Option value="IN_PROGRESS">In Progress</Option>
                <Option value="DONE">Done</Option>
                <Option value="BLOCKED">Blocked</Option>
              </Select>
              <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <input type="checkbox" checked={showOnlyPending} onChange={(e) => setShowOnlyPending(e.target.checked)} />
                Show only pending
              </label>
              <Text type="secondary" style={{ marginLeft: 'auto' }}>{filteredCheckpoints.length} Results</Text>
            </div>
            <Table
              columns={checkpointColumns}
              dataSource={filteredCheckpoints}
              rowKey="id"
              pagination={{ pageSize: 20 }}
              size="small"
              rowClassName={(record) =>
                record.incidentStatus === 'BLOCKED' ? 'checkpoint-blocked' :
                record.incidentStatus === 'DONE' ? 'checkpoint-done' : ''
              }
            />
          </Card>
        </TabPane>
      </Tabs>

      <style>{`
        .checkpoint-blocked { background-color: #fff2f0 !important; }
        .checkpoint-done { background-color: #f6ffed !important; }
      `}</style>
    </div>
  );
};

export default IncidentDetailsPage;
