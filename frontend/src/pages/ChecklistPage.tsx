import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col, Alert } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, EyeOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { checklistService } from '../services/checklistService';
import { Checklist, ChecklistRequest, ChecklistPriority } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const ChecklistPage: React.FC = () => {
  const [checklists, setChecklists] = useState<Checklist[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingChecklist, setEditingChecklist] = useState<Checklist | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filters, setFilters] = useState<{ priority?: ChecklistPriority; search?: string }>({});
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  useEffect(() => {
    loadChecklists();
  }, [pagination.current, pagination.pageSize, filters]);

  const loadChecklists = async () => {
    setLoading(true);
    try {
      const response = await checklistService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
        ...filters,
      });
      setChecklists(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load checklists');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingChecklist(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Checklist) => {
    setEditingChecklist(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await checklistService.delete(id);
      message.success('Checklist deleted successfully');
      loadChecklists();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to delete checklist');
    }
  };

  const handleSubmit = async (values: ChecklistRequest) => {
    try {
      if (editingChecklist) {
        await checklistService.update(editingChecklist.id, values);
        message.success('Checklist updated successfully');
      } else {
        await checklistService.create(values);
        message.success('Checklist created successfully');
      }
      setModalVisible(false);
      loadChecklists();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const priorityColors: Record<ChecklistPriority, string> = {
    LOW: 'green',
    MEDIUM: 'orange',
    HIGH: 'red',
  };

  const columns: ColumnsType<Checklist> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority: ChecklistPriority) => (
        <Tag color={priorityColors[priority]}>{priority}</Tag>
      ),
    },
    {
      title: 'Created By',
      dataIndex: 'createdBy',
      key: 'createdBy',
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Checklist) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/checklists/${record.id}`)} title="View Details" />
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
      <Alert
        message="Checklist Templates"
        description="Checklists are reusable templates. Execution status tracking is managed per Feature in the Feature Details page."
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
      />
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col flex="auto">
            <Space>
              <Input
                placeholder="Search checklists..."
                prefix={<SearchOutlined />}
                onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                style={{ width: 250 }}
                allowClear
              />
                            <Select
                placeholder="Priority"
                allowClear
                style={{ width: 150 }}
                onChange={(value) => setFilters(prev => ({ ...prev, priority: value }))}
              >
                <Option value="LOW">Low</Option>
                <Option value="MEDIUM">Medium</Option>
                <Option value="HIGH">High</Option>
              </Select>
            </Space>
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Add Checklist
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={checklists}
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
        title={editingChecklist ? 'Edit Checklist' : 'Create Checklist'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Please enter checklist name' }]}
          >
            <Input placeholder="Enter checklist name" />
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
          <Form.Item name="priority" label="Priority" initialValue="MEDIUM">
            <Select>
              <Option value="LOW">Low</Option>
              <Option value="MEDIUM">Medium</Option>
              <Option value="HIGH">High</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingChecklist ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ChecklistPage;
