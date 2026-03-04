import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, WarningOutlined, EyeOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { incidentService } from '../services/incidentService';
import { featureService } from '../services/featureService';
import { microserviceService } from '../services/microserviceService';
import { Incident, IncidentRequest, Severity, IncidentStatus, Feature, Microservice } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const severityColors: Record<Severity, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
  CRITICAL: 'magenta',
};

const statusColors: Record<IncidentStatus, string> = {
  OPEN: 'red',
  IN_PROGRESS: 'orange',
  RESOLVED: 'green',
  CLOSED: 'default',
};

const IncidentPage: React.FC = () => {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [features, setFeatures] = useState<Feature[]>([]);
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingIncident, setEditingIncident] = useState<Incident | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  useEffect(() => {
    loadIncidents();
    loadFeatures();
    loadMicroservices();
  }, [pagination.current, pagination.pageSize]);

  const loadIncidents = async () => {
    setLoading(true);
    try {
      const response = await incidentService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      setIncidents(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load incidents');
    } finally {
      setLoading(false);
    }
  };

  const loadFeatures = async () => {
    try {
      const response = await featureService.getAll({ size: 1000 });
      setFeatures(response.content);
    } catch (error) {
      console.error('Failed to load features');
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
    setEditingIncident(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Incident) => {
    setEditingIncident(record);
    form.setFieldsValue({
      ...record,
      mainFeatureId: record.mainFeature.id,
      microserviceIds: record.microservices?.map(m => m.id) || [],
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await incidentService.delete(id);
      message.success('Incident deleted successfully');
      loadIncidents();
    } catch (error) {
      message.error('Failed to delete incident');
    }
  };

  const handleSubmit = async (values: IncidentRequest) => {
    try {
      if (editingIncident) {
        await incidentService.update(editingIncident.id, values);
        message.success('Incident updated successfully');
      } else {
        await incidentService.create(values);
        message.success('Incident created successfully');
      }
      setModalVisible(false);
      loadIncidents();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  const columns: ColumnsType<Incident> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (title: string) => <strong>{title}</strong>,
    },
    {
      title: 'Severity',
      dataIndex: 'severity',
      key: 'severity',
      render: (severity: Severity) => (
        <Tag color={severityColors[severity]} icon={<WarningOutlined />}>
          {severity}
        </Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: IncidentStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Main Feature',
      dataIndex: 'mainFeature',
      key: 'mainFeature',
      render: (feature: Incident['mainFeature']) => (
        <Tag color="blue">{feature.name}</Tag>
      ),
    },
    {
      title: 'Owner',
      dataIndex: 'owner',
      key: 'owner',
      render: (owner: Incident['owner']) => owner?.username || '-',
    },
    {
      title: 'Microservices',
      dataIndex: 'microserviceCount',
      key: 'microserviceCount',
      render: (count: number) => <Tag>{count} services</Tag>,
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record: Incident) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/incidents/${record.id}`)} title="View Details" />
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          {isAdmin && (
            <Popconfirm title="Are you sure?" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col flex="auto">
            <Input
              placeholder="Search incidents..."
              prefix={<SearchOutlined />}
              style={{ width: 250 }}
              allowClear
            />
          </Col>
          <Col>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Report Incident
            </Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={incidents}
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
        title={editingIncident ? 'Edit Incident' : 'Report Incident'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnHidden
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: 'Please enter incident title' }]}
          >
            <Input placeholder="Enter incident title" />
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
            <TextArea rows={3} placeholder="Describe the incident" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="severity"
                label="Severity"
                rules={[{ required: true, message: 'Please select severity' }]}
              >
                <Select placeholder="Select severity">
                  <Option value="LOW">Low</Option>
                  <Option value="MEDIUM">Medium</Option>
                  <Option value="HIGH">High</Option>
                  <Option value="CRITICAL">Critical</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="Status" initialValue="OPEN">
                <Select>
                  <Option value="OPEN">Open</Option>
                  <Option value="IN_PROGRESS">In Progress</Option>
                  <Option value="RESOLVED">Resolved</Option>
                  <Option value="CLOSED">Closed</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="mainFeatureId"
            label="Main Feature"
            rules={[{ required: true, message: 'Please select main feature' }]}
          >
            <Select placeholder="Select main feature" showSearch optionFilterProp="children">
              {features.map(f => (
                <Option key={f.id} value={f.id}>{f.name} ({f.domain})</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="microserviceIds" label="Impacted Microservices">
            <Select mode="multiple" placeholder="Select impacted microservices" optionFilterProp="children">
              {microservices.map(m => (
                <Option key={m.id} value={m.id}>{m.name}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingIncident ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default IncidentPage;
