import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, message, Popconfirm, Card, Row, Col, ColorPicker, Switch } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { domainService } from '../services/domainService';
import { Domain, DomainRequest } from '../types';
import { useAuth } from '../contexts/AuthContext';
import type { ColumnsType } from 'antd/es/table';
import type { Color } from 'antd/es/color-picker';
import dayjs from 'dayjs';

const { TextArea } = Input;

const DomainPage: React.FC = () => {
  const [domains, setDomains] = useState<Domain[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDomain, setEditingDomain] = useState<Domain | null>(null);
  const [searchText, setSearchText] = useState('');
  const { isAdmin } = useAuth();
  const [form] = Form.useForm();

  useEffect(() => {
    loadDomains();
  }, []);

  const loadDomains = async () => {
    setLoading(true);
    try {
      const data = isAdmin 
        ? await domainService.getAllIncludingInactive()
        : await domainService.getAll();
      setDomains(data);
    } catch (error) {
      message.error('Failed to load domains');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingDomain(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true, colorCode: '#1890ff' });
    setModalVisible(true);
  };

  const handleEdit = (record: Domain) => {
    setEditingDomain(record);
    form.setFieldsValue({
      ...record,
      colorCode: record.colorCode || '#1890ff',
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await domainService.delete(id);
      message.success('Domain deleted successfully');
      loadDomains();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Failed to delete domain');
    }
  };

  const handleSubmit = async (values: DomainRequest & { colorCode: string | Color }) => {
    try {
      let colorCode = '#1890ff';
      if (typeof values.colorCode === 'string') {
        colorCode = values.colorCode;
      } else if (values.colorCode && typeof values.colorCode === 'object') {
        colorCode = (values.colorCode as Color).toHexString();
      }
      
      const data: DomainRequest = {
        ...values,
        colorCode,
      };

      if (editingDomain) {
        await domainService.update(editingDomain.id, data);
        message.success('Domain updated successfully');
      } else {
        await domainService.create(data);
        message.success('Domain created successfully');
      }
      setModalVisible(false);
      loadDomains();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      message.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const filteredDomains = domains.filter(domain =>
    domain.name.toLowerCase().includes(searchText.toLowerCase()) ||
    domain.description?.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns: ColumnsType<Domain> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Domain) => (
        <Tag color={record.colorCode || '#1890ff'}>{name}</Tag>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Color',
      dataIndex: 'colorCode',
      key: 'colorCode',
      render: (color: string) => (
        <div style={{ 
          width: 24, 
          height: 24, 
          backgroundColor: color || '#1890ff', 
          borderRadius: 4,
          border: '1px solid #d9d9d9'
        }} />
      ),
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean) => (
        <Tag color={isActive ? 'success' : 'default'}>
          {isActive ? 'Active' : 'Inactive'}
        </Tag>
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
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    ...(isAdmin ? [{
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Domain) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="Are you sure?" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    }] : []),
  ];

  return (
    <div>
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col flex="auto">
            <Input
              placeholder="Search domains..."
              prefix={<SearchOutlined />}
              onChange={(e) => setSearchText(e.target.value)}
              style={{ width: 250 }}
              allowClear
            />
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Add Domain
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={filteredDomains}
          rowKey="id"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} items`,
          }}
        />
      </Card>

      <Modal
        title={editingDomain ? 'Edit Domain' : 'Create Domain'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Please enter domain name' }]}
          >
            <Input placeholder="Enter domain name" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Description"
          >
            <TextArea rows={3} placeholder="Enter description" />
          </Form.Item>

          <Form.Item
            name="colorCode"
            label="Color"
          >
            <ColorPicker showText />
          </Form.Item>

          <Form.Item
            name="isActive"
            label="Active"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingDomain ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DomainPage;
