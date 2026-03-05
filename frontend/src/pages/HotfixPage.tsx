import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ThunderboltOutlined, EyeOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { hotfixService } from '../services/hotfixService';
import { featureService } from '../services/featureService';
import { microserviceService } from '../services/microserviceService';
import { userService } from '../services/userService';
import { Hotfix, HotfixRequest, HotfixStatus, Feature, Microservice, UserSummary } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const statusColors: Record<HotfixStatus, string> = {
  PLANNED: 'blue',
  IN_PROGRESS: 'orange',
  DEPLOYED: 'green',
};

const HotfixPage: React.FC = () => {
  const [hotfixes, setHotfixes] = useState<Hotfix[]>([]);
  const [features, setFeatures] = useState<Feature[]>([]);
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingHotfix, setEditingHotfix] = useState<Hotfix | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  useEffect(() => {
    loadHotfixes();
    loadFeatures();
    loadMicroservices();
    loadUsers();
  }, [pagination.current, pagination.pageSize]);

  const loadHotfixes = async () => {
    setLoading(true);
    try {
      const response = await hotfixService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      setHotfixes(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load hotfixes');
    } finally {
      setLoading(false);
    }
  };

  const loadFeatures = async () => {
    try {
      const response = await featureService.getAll({ size: 1000 });
      setFeatures(response.content);
      console.log('Features loaded in Hotfixes:', response.content.length);
    } catch (error) {
      console.error('Failed to load features', error);
      message.error('Failed to load features');
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

  const loadUsers = async () => {
    try {
      const response = await userService.getApprovedUsers();
      setUsers(response);
    } catch (error) {
      console.error('Failed to load users');
    }
  };

  const handleCreate = () => {
    setEditingHotfix(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Hotfix) => {
    setEditingHotfix(record);
    form.setFieldsValue({
      ...record,
      mainFeatureId: record.mainFeature.id,
      ownerId: record.owner?.id,
      microserviceIds: record.microservices?.map(m => m.id) || [],
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await hotfixService.delete(id);
      message.success('Hotfix deleted successfully');
      loadHotfixes();
    } catch (error) {
      message.error('Failed to delete hotfix');
    }
  };

  const handleSubmit = async (values: HotfixRequest) => {
    try {
      if (editingHotfix) {
        await hotfixService.update(editingHotfix.id, values);
        message.success('Hotfix updated successfully');
      } else {
        await hotfixService.create(values);
        message.success('Hotfix created successfully');
      }
      setModalVisible(false);
      loadHotfixes();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  const columns: ColumnsType<Hotfix> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (title: string) => (
        <span>
          <ThunderboltOutlined style={{ color: '#faad14', marginRight: 8 }} />
          <strong>{title}</strong>
        </span>
      ),
    },
    {
      title: 'Release Version',
      dataIndex: 'releaseVersion',
      key: 'releaseVersion',
      render: (version: string) => version ? <Tag>{version}</Tag> : '-',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: HotfixStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Main Feature',
      dataIndex: 'mainFeature',
      key: 'mainFeature',
      render: (feature: Hotfix['mainFeature']) => (
        <Tag color="blue">{feature.name}</Tag>
      ),
    },
    {
      title: 'Owner',
      dataIndex: 'owner',
      key: 'owner',
      render: (owner: Hotfix['owner']) => owner?.username || '-',
    },
    {
      title: 'Microservices',
      dataIndex: 'microserviceCount',
      key: 'microserviceCount',
      render: (count: number) => <Tag>{count} services</Tag>,
    },
    {
      title: 'Deployed',
      dataIndex: 'deployedAt',
      key: 'deployedAt',
      render: (date: string | null) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record: Hotfix) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/hotfixes/${record.id}`)} title="View Details" />
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
              placeholder="Search hotfixes..."
              prefix={<SearchOutlined />}
              style={{ width: 250 }}
              allowClear
            />
          </Col>
          <Col>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Hotfix
            </Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={hotfixes}
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
        title={editingHotfix ? 'Edit Hotfix' : 'Create Hotfix'}
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
            rules={[{ required: true, message: 'Please enter hotfix title' }]}
          >
            <Input placeholder="Enter hotfix title" />
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
            <TextArea rows={3} placeholder="Describe the hotfix" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="releaseVersion" label="Release Version">
                <Input placeholder="e.g., v1.0.1-hotfix" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="Status" initialValue="PLANNED">
                <Select>
                  <Option value="PLANNED">Planned</Option>
                  <Option value="IN_PROGRESS">In Progress</Option>
                  <Option value="DEPLOYED">Deployed</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="mainFeatureId"
            label="Main Feature"
            rules={[{ required: true, message: 'Please select main feature' }]}
          >
            <Select 
              placeholder="Select main feature" 
              showSearch 
              optionFilterProp="children"
              loading={features.length === 0}
              notFoundContent={features.length === 0 ? "Loading features..." : "No features found"}
            >
              {features.map(f => (
                <Option key={f.id} value={f.id}>{f.name}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="ownerId"
            label="Owner"
          >
            <Select placeholder="Select owner" showSearch optionFilterProp="children" allowClear>
              {users.map(u => (
                <Option key={u.id} value={u.id}>{u.fullName || u.username}</Option>
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
                {editingHotfix ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default HotfixPage;
