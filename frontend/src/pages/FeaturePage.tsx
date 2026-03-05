import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col, Progress, List, DatePicker, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ApiOutlined, CheckSquareOutlined, EyeOutlined, RobotOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { useNavigate } from 'react-router-dom';
import { featureService } from '../services/featureService';
import { microserviceService } from '../services/microserviceService';
import { userService } from '../services/userService';
import { Feature, FeatureRequest, FeatureStatus, Microservice, Checklist, UserSummary, Domain } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { domainService } from '../services/domainService';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;
const { Text } = Typography;

const FeaturePage: React.FC = () => {
  const [features, setFeatures] = useState<Feature[]>([]);
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [microserviceModalVisible, setMicroserviceModalVisible] = useState(false);
  const [checklistModalVisible, setChecklistModalVisible] = useState(false);
  const [selectedFeature, setSelectedFeature] = useState<Feature | null>(null);
  const [impactedMicroservices, setImpactedMicroservices] = useState<Microservice[]>([]);
  const [aggregatedChecklists, setAggregatedChecklists] = useState<Checklist[]>([]);
  const [editingFeature, setEditingFeature] = useState<Feature | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filters, setFilters] = useState<{ status?: FeatureStatus; search?: string }>({});
  const { isAdmin, user } = useAuth();
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    loadFeatures();
    loadMicroservices();
    loadUsers();
    loadDomains();
  }, [pagination.current, pagination.pageSize, filters]);

  const loadDomains = async () => {
    try {
      const data = await domainService.getAll();
      setDomains(data);
    } catch (error) {
      console.error('Failed to load domains');
    }
  };

  const loadUsers = async () => {
    try {
      const approvedUsers = await userService.getApprovedUsers();
      setUsers(approvedUsers);
    } catch (error) {
      console.error('Failed to load users');
    }
  };

  const loadFeatures = async () => {
    setLoading(true);
    try {
      // Non-admin users only see their assigned features
      const params: { page: number; size: number; status?: FeatureStatus; search?: string; assignedToId?: string } = {
        page: pagination.current - 1,
        size: pagination.pageSize,
        ...filters,
      };
      
      // If not admin, filter by current user's assigned features
      if (!isAdmin && user?.id) {
        params.assignedToId = user.id;
      }
      
      const response = await featureService.getAll(params);
      setFeatures(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load features');
    } finally {
      setLoading(false);
    }
  };

  const loadMicroservices = async () => {
    try {
      const response = await microserviceService.getAll({ size: 1000 });
      setMicroservices(response.content);
    } catch (error) {
      console.error('Failed to load microservices');
    }
  };

  const handleCreate = () => {
    setEditingFeature(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Feature) => {
    setEditingFeature(record);
    form.setFieldsValue({
      ...record,
      targetDate: record.targetDate ? dayjs(record.targetDate) : null,
      microserviceIds: record.microservices?.map(m => m.id) || [],
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await featureService.delete(id);
      message.success('Feature deleted successfully');
      loadFeatures();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to delete feature');
    }
  };

  const handleSubmit = async (values: FeatureRequest & { targetDate?: dayjs.Dayjs }) => {
    try {
      const payload: FeatureRequest = {
        ...values,
        targetDate: values.targetDate ? values.targetDate.format('YYYY-MM-DD') : undefined,
      };
      
      if (editingFeature) {
        await featureService.update(editingFeature.id, payload);
        message.success('Feature updated successfully');
      } else {
        await featureService.create(payload);
        message.success('Feature created successfully');
      }
      setModalVisible(false);
      loadFeatures();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleViewMicroservices = async (feature: Feature) => {
    setSelectedFeature(feature);
    try {
      const data = await featureService.getImpactedMicroservices(feature.id);
      setImpactedMicroservices(data);
      setMicroserviceModalVisible(true);
    } catch (error) {
      message.error('Failed to load microservices');
    }
  };

  const handleViewChecklists = async (feature: Feature) => {
    setSelectedFeature(feature);
    try {
      const data = await featureService.getAggregatedChecklists(feature.id);
      setAggregatedChecklists(data);
      setChecklistModalVisible(true);
    } catch (error) {
      message.error('Failed to load checklists');
    }
  };

  const statusColors: Record<FeatureStatus, string> = {
    PLANNED: 'blue',
    IN_PROGRESS: 'orange',
    COMPLETED: 'cyan',
    RELEASED: 'green',
  };

  // Build domain colors map from loaded domains
  const domainColors: Record<string, string> = domains.reduce((acc, d) => {
    acc[d.name] = d.colorCode || '#8c8c8c';
    return acc;
  }, {} as Record<string, string>);

  const columns: ColumnsType<Feature> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      render: (name: string, record: Feature) => (
        <Button type="link" onClick={() => navigate(`/features/${record.id}`)} style={{ padding: 0 }}>
          {name}
        </Button>
      ),
    },
    {
      title: 'Domain',
      dataIndex: 'domain',
      key: 'domain',
      render: (domain: string) => (
        <Tag color={domainColors[domain] || '#8c8c8c'}>{domain}</Tag>
      ),
    },
    {
      title: 'Release Version',
      dataIndex: 'releaseVersion',
      key: 'releaseVersion',
    },
    {
      title: 'Target Date',
      dataIndex: 'targetDate',
      key: 'targetDate',
      render: (date: string) => date ? dayjs(date).format('YYYY-MM-DD') : '-',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: FeatureStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Progress',
      key: 'progress',
      render: (_, record: Feature) => (
        <Progress 
          percent={record.progressPercentage} 
          size="small" 
          status={record.progressPercentage === 100 ? 'success' : 'active'}
        />
      ),
    },
    {
      title: 'Microservices',
      key: 'microservices',
      render: (_, record: Feature) => (
        <span>{record.microserviceCount}</span>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 280,
      render: (_, record: Feature) => (
        <Space wrap>
          <Button 
            type="primary" 
            size="small" 
            icon={<EyeOutlined />}
            onClick={() => navigate(`/features/${record.id}`)}
          >
            Details
          </Button>
          <Button 
            type="primary" 
            ghost 
            size="small" 
            icon={<ApiOutlined />}
            onClick={() => handleViewMicroservices(record)}
          >
            Microservices
          </Button>
          <Button 
            type="primary" 
            ghost 
            size="small" 
            icon={<CheckSquareOutlined />}
            onClick={() => handleViewChecklists(record)}
          >
            Checklists
          </Button>
          {isAdmin && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
              <Popconfirm title="Are you sure?" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  const completedChecklists = aggregatedChecklists.filter(c => c.status === 'DONE').length;
  const checklistProgress = aggregatedChecklists.length > 0 
    ? Math.round((completedChecklists / aggregatedChecklists.length) * 100) 
    : 0;

  return (
    <div>
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col flex="auto">
            <Space>
              <Input
                placeholder="Search features..."
                prefix={<SearchOutlined />}
                onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                style={{ width: 250 }}
                allowClear
              />
              <Select
                placeholder="Status"
                allowClear
                style={{ width: 150 }}
                onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
              >
                <Option value="PLANNED">Planned</Option>
                <Option value="IN_PROGRESS">In Progress</Option>
                <Option value="RELEASED">Released</Option>
              </Select>
            </Space>
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Add Feature
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={features}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} items`,
          }}
          onChange={(pag) => setPagination(prev => ({ ...prev, current: pag.current || 1, pageSize: pag.pageSize || 10 }))}
        />
      </Card>

      <Modal
        title={editingFeature ? 'Edit Feature' : 'Create Feature'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnHidden
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Please enter feature name' }]}
          >
            <Input placeholder="Enter feature name" />
          </Form.Item>
          <Form.Item 
            name="description" 
            label={
              <Space>
                <span>Description</span>
                <AIRephraseButton 
                  getText={() => form.getFieldValue('description') || ''} 
                  onApply={(newText) => form.setFieldsValue({ description: newText })}
                  fieldName="description"
                />
              </Space>
            }
          >
            <TextArea rows={3} placeholder="Enter description" />
          </Form.Item>
          <Form.Item
            name="domain"
            label="Domain"
            rules={[{ required: true, message: 'Please select a domain' }]}
            initialValue="General"
          >
            <Select placeholder="Select domain" showSearch optionFilterProp="children">
              {domains.map(domain => (
                <Option key={domain.id} value={domain.name}>
                  <Tag color={domain.colorCode}>{domain.name}</Tag>
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="releaseVersion" label="Release Version">
                <Input placeholder="e.g., v1.0.0" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="targetDate" label="Target Date">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="status" label="Status" initialValue="PLANNED">
                <Select>
                  <Option value="PLANNED">Planned</Option>
                  <Option value="IN_PROGRESS">In Progress</Option>
                  <Option value="RELEASED">Released</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="ownerId" label="Owner">
                <Select placeholder="Select owner" allowClear showSearch optionFilterProp="children">
                  {users.map(u => (
                    <Option key={u.id} value={u.id}>{u.fullName || u.username}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="microserviceIds"
            label="Microservices"
            rules={[{ required: true, message: 'Please select at least one microservice' }]}
          >
            <Select mode="multiple" placeholder="Select microservices" optionFilterProp="children">
              {microservices.map(m => (
                <Option key={m.id} value={m.id}>{m.name}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingFeature ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`Impacted Microservices - ${selectedFeature?.name}`}
        open={microserviceModalVisible}
        onCancel={() => setMicroserviceModalVisible(false)}
        footer={null}
        width={700}
      >
        <List
          dataSource={impactedMicroservices}
          renderItem={(item: Microservice) => (
            <List.Item>
              <List.Item.Meta
                title={item.name}
                description={
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <Text type="secondary">{item.description}</Text>
                    <Space>
                      <Tag color={item.status === 'COMPLETED' ? 'success' : item.status === 'IN_PROGRESS' ? 'processing' : 'default'}>
                        {item.status.replace('_', ' ')}
                      </Tag>
                      <span>Owner: {typeof item.owner === 'string' ? item.owner : item.owner?.username || 'N/A'}</span>
                      <span>Version: {item.version || 'N/A'}</span>
                    </Space>
                    <Progress percent={item.progressPercentage} size="small" />
                  </Space>
                }
              />
            </List.Item>
          )}
        />
      </Modal>

      <Modal
        title={`Aggregated Checklists - ${selectedFeature?.name}`}
        open={checklistModalVisible}
        onCancel={() => setChecklistModalVisible(false)}
        footer={null}
        width={700}
      >
        <div style={{ marginBottom: 16 }}>
          <Text strong>Overall Completion: </Text>
          <Progress percent={checklistProgress} />
          <Text type="secondary">{completedChecklists} of {aggregatedChecklists.length} checklists completed</Text>
        </div>
        <List
          dataSource={aggregatedChecklists}
          renderItem={(item: Checklist) => (
            <List.Item>
              <List.Item.Meta
                title={item.name}
                description={item.description}
              />
              <Space>
                <Tag color={item.priority === 'HIGH' ? 'red' : item.priority === 'MEDIUM' ? 'orange' : 'green'}>
                  {item.priority}
                </Tag>
                <Tag color={item.status === 'DONE' ? 'success' : item.status === 'IN_PROGRESS' ? 'processing' : item.status === 'BLOCKED' ? 'error' : 'default'}>
                  {item.status.replace('_', ' ')}
                </Tag>
              </Space>
            </List.Item>
          )}
        />
      </Modal>
    </div>
  );
};

export default FeaturePage;
