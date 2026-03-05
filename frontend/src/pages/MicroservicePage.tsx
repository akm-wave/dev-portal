import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col, Progress, List } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, EyeOutlined, GitlabOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { microserviceService } from '../services/microserviceService';
import { checklistService } from '../services/checklistService';
import { Microservice, MicroserviceRequest, MicroserviceStatus, Checklist } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const MicroservicePage: React.FC = () => {
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [checklists, setChecklists] = useState<Checklist[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedMicroservice, setSelectedMicroservice] = useState<Microservice | null>(null);
  const [editingMicroservice, setEditingMicroservice] = useState<Microservice | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filters, setFilters] = useState<{ status?: MicroserviceStatus; search?: string }>({});
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  useEffect(() => {
    loadMicroservices();
    loadChecklists();
  }, [pagination.current, pagination.pageSize, filters]);

  const loadMicroservices = async () => {
    setLoading(true);
    try {
      const response = await microserviceService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
        ...filters,
      });
      setMicroservices(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load microservices');
    } finally {
      setLoading(false);
    }
  };

  const loadChecklists = async () => {
    try {
      const response = await checklistService.getAll({ size: 1000 });
      setChecklists(response.content);
    } catch (error) {
      console.error('Failed to load checklists');
    }
  };

  const handleCreate = () => {
    setEditingMicroservice(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Microservice) => {
    setEditingMicroservice(record);
    form.setFieldsValue({
      ...record,
      checklistIds: record.checklists?.map(c => c.id) || [],
    });
    setModalVisible(true);
  };

  const handleViewDetails = (record: Microservice) => {
    setSelectedMicroservice(record);
    setDetailModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await microserviceService.delete(id);
      message.success('Microservice deleted successfully');
      loadMicroservices();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to delete microservice');
    }
  };

  const handleSubmit = async (values: MicroserviceRequest) => {
    try {
      if (editingMicroservice) {
        await microserviceService.update(editingMicroservice.id, values);
        message.success('Microservice updated successfully');
      } else {
        await microserviceService.create(values);
        message.success('Microservice created successfully');
      }
      setModalVisible(false);
      loadMicroservices();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const statusColors: Record<MicroserviceStatus, string> = {
    NOT_STARTED: 'default',
    PLANNED: 'blue',
    IN_PROGRESS: 'processing',
    COMPLETED: 'success',
  };

  const columns: ColumnsType<Microservice> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
    },
    {
      title: 'Version',
      dataIndex: 'version',
      key: 'version',
    },
    {
      title: 'Owner',
      dataIndex: 'owner',
      key: 'owner',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: MicroserviceStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Progress',
      key: 'progress',
      render: (_, record: Microservice) => (
        <Progress 
          percent={record.progressPercentage} 
          size="small" 
          status={record.progressPercentage === 100 ? 'success' : 'active'}
        />
      ),
    },
    {
      title: 'Checklists',
      key: 'checklists',
      render: (_, record: Microservice) => (
        <span>{record.completedChecklistCount}/{record.checklistCount}</span>
      ),
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record: Microservice) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/microservices/${record.id}`)} title="View Details" />
          {isAdmin && (
            <>
              <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
              <Popconfirm title="Are you sure?" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </>
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
            <Space>
              <Input
                placeholder="Search microservices..."
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
                <Option value="NOT_STARTED">Not Started</Option>
                <Option value="IN_PROGRESS">In Progress</Option>
                <Option value="COMPLETED">Completed</Option>
              </Select>
            </Space>
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Add Microservice
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={microservices}
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
        title={editingMicroservice ? 'Edit Microservice' : 'Create Microservice'}
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
            rules={[{ required: true, message: 'Please enter microservice name' }]}
          >
            <Input placeholder="Enter microservice name" />
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
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="version" label="Version">
                <Input placeholder="e.g., 1.0.0" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="owner" label="Owner">
                <Input placeholder="Enter owner name" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="status" label="Status" initialValue="NOT_STARTED">
            <Select>
              <Option value="NOT_STARTED">Not Started</Option>
              <Option value="IN_PROGRESS">In Progress</Option>
              <Option value="COMPLETED">Completed</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="checklistIds"
            label="Checklists"
            rules={[{ required: true, message: 'Please select at least one checklist' }]}
          >
            <Select mode="multiple" placeholder="Select checklists" optionFilterProp="children">
              {checklists.map(c => (
                <Option key={c.id} value={c.id}>{c.name}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="gitlabUrl"
            label="GitLab Repository URL"
            rules={[{ type: 'url', message: 'Please enter a valid URL' }]}
          >
            <Input prefix={<GitlabOutlined />} placeholder="https://gitlab.com/your-org/repo" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingMicroservice ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Microservice Details"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedMicroservice && (
          <div>
            <p><strong>Name:</strong> {selectedMicroservice.name}</p>
            <p><strong>Description:</strong> {selectedMicroservice.description}</p>
            <p><strong>Version:</strong> {selectedMicroservice.version}</p>
            <p><strong>Owner:</strong> {typeof selectedMicroservice.owner === 'string' ? selectedMicroservice.owner : selectedMicroservice.owner?.username || '-'}</p>
            <p><strong>Status:</strong> <Tag color={statusColors[selectedMicroservice.status]}>{selectedMicroservice.status.replace('_', ' ')}</Tag></p>
            {selectedMicroservice.gitlabUrl && (
              <p>
                <strong>Repository:</strong>{' '}
                <Button 
                  type="primary" 
                  size="small" 
                  icon={<GitlabOutlined />}
                  href={selectedMicroservice.gitlabUrl}
                  target="_blank"
                >
                  View Repository
                </Button>
              </p>
            )}
            <p><strong>Progress:</strong></p>
            <Progress percent={selectedMicroservice.progressPercentage} />
            <p style={{ marginTop: 16 }}><strong>Checklists:</strong></p>
            <List
              size="small"
              bordered
              dataSource={selectedMicroservice.checklists}
              renderItem={(item: Checklist) => (
                <List.Item>
                  <span>{item.name}</span>
                  <Tag color={item.status === 'DONE' ? 'success' : 'default'}>{item.status}</Tag>
                </List.Item>
              )}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default MicroservicePage;
