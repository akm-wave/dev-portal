import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col, Drawer, Typography, Divider, Upload, List } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, FileTextOutlined, UploadOutlined, DownloadOutlined, EyeOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import { utilityService } from '../services/utilityService';
import { Utility, UtilityRequest, UtilityType, UtilityAttachment } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;
const { Text, Title } = Typography;

const typeLabels: Record<UtilityType, string> = {
  MOP: 'MOP',
  CR_REQUIREMENT: 'CR Requirement',
  DEVELOPMENT_GUIDELINE: 'Development Guideline',
  SOP: 'SOP',
  OTHERS: 'Others',
};

const typeColors: Record<UtilityType, string> = {
  MOP: 'blue',
  CR_REQUIREMENT: 'purple',
  DEVELOPMENT_GUIDELINE: 'green',
  SOP: 'orange',
  OTHERS: 'default',
};

const UtilityPage: React.FC = () => {
  const [utilities, setUtilities] = useState<Utility[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [selectedUtility, setSelectedUtility] = useState<Utility | null>(null);
  const [editingUtility, setEditingUtility] = useState<Utility | null>(null);
  const [attachments, setAttachments] = useState<UtilityAttachment[]>([]);
  const [uploadLoading, setUploadLoading] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [searchText, setSearchText] = useState('');
  const [filterType, setFilterType] = useState<UtilityType | undefined>(undefined);
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();

  useEffect(() => {
    loadUtilities();
  }, [pagination.current, pagination.pageSize, searchText, filterType]);

  const loadUtilities = async () => {
    setLoading(true);
    try {
      const response = await utilityService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
        sortBy: 'createdAt',
        sortDir: 'desc',
        type: filterType,
        search: searchText || undefined,
      });
      setUtilities(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load utilities');
    } finally {
      setLoading(false);
    }
  };

  const loadAttachments = async (utilityId: string) => {
    try {
      const data = await utilityService.getAttachments(utilityId);
      setAttachments(data);
    } catch (error) {
      console.error('Failed to load attachments');
    }
  };

  const handleCreate = () => {
    setEditingUtility(null);
    form.resetFields();
    setPendingFiles([]);
    setModalVisible(true);
  };

  const handleEdit = async (record: Utility) => {
    setEditingUtility(record);
    form.setFieldsValue(record);
    setPendingFiles([]);
    await loadAttachments(record.id);
    setModalVisible(true);
  };

  const handleViewDetails = async (record: Utility) => {
    setSelectedUtility(record);
    setDrawerVisible(true);
    await loadAttachments(record.id);
  };

  const handleDelete = async (id: string) => {
    try {
      await utilityService.delete(id);
      message.success('Utility deleted successfully');
      loadUtilities();
    } catch (error) {
      message.error('Failed to delete utility');
    }
  };

  const handleSubmit = async (values: UtilityRequest) => {
    try {
      let utilityId: string;
      if (editingUtility) {
        await utilityService.update(editingUtility.id, values);
        utilityId = editingUtility.id;
        message.success('Utility updated successfully');
      } else {
        const created = await utilityService.create(values);
        utilityId = created.id;
        message.success('Utility created successfully');
      }

      // Upload pending files
      if (pendingFiles.length > 0) {
        for (const file of pendingFiles) {
          try {
            await utilityService.uploadAttachment(utilityId, file);
          } catch (err) {
            console.error('Failed to upload file:', file.name);
          }
        }
        message.success(`${pendingFiles.length} file(s) uploaded`);
      }

      setPendingFiles([]);
      setModalVisible(false);
      loadUtilities();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  const handleUploadAttachment = async (file: File) => {
    if (!selectedUtility) return;
    setUploadLoading(true);
    try {
      await utilityService.uploadAttachment(selectedUtility.id, file);
      message.success('File uploaded successfully');
      await loadAttachments(selectedUtility.id);
      loadUtilities();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploadLoading(false);
    }
  };

  const handleDeleteAttachment = async (attachmentId: string) => {
    if (!selectedUtility) return;
    try {
      await utilityService.deleteAttachment(selectedUtility.id, attachmentId);
      message.success('Attachment deleted');
      await loadAttachments(selectedUtility.id);
      loadUtilities();
    } catch (error) {
      message.error('Failed to delete attachment');
    }
  };

  const columns: ColumnsType<Utility> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (title: string, record: Utility) => (
        <Button type="link" onClick={() => handleViewDetails(record)} style={{ padding: 0 }}>
          <FileTextOutlined style={{ marginRight: 8 }} />
          <strong>{title}</strong>
        </Button>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type: UtilityType) => (
        <Tag color={typeColors[type]}>{typeLabels[type]}</Tag>
      ),
    },
    {
      title: 'Version',
      dataIndex: 'version',
      key: 'version',
      render: (version: string) => version || '-',
    },
    {
      title: 'Attachments',
      dataIndex: 'attachmentCount',
      key: 'attachmentCount',
      render: (count: number) => count > 0 ? <Tag color="blue">{count} files</Tag> : '-',
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record: Utility) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/utilities/${record.id}`)} title="View Details" />
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

  return (
    <div>
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col flex="auto">
            <Space>
              <Input
                placeholder="Search utilities..."
                prefix={<SearchOutlined />}
                style={{ width: 250 }}
                allowClear
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
              <Select
                placeholder="Filter by type"
                style={{ width: 180 }}
                allowClear
                value={filterType}
                onChange={(value) => setFilterType(value)}
              >
                {Object.entries(typeLabels).map(([key, label]) => (
                  <Option key={key} value={key}>{label}</Option>
                ))}
              </Select>
            </Space>
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Create Utility
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={utilities}
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
        title={editingUtility ? 'Edit Utility' : 'Create Utility'}
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
            rules={[{ required: true, message: 'Please enter utility title' }]}
          >
            <Input placeholder="Enter utility title" />
          </Form.Item>
          <Form.Item name="type" label="Type" initialValue="OTHERS">
            <Select>
              {Object.entries(typeLabels).map(([key, label]) => (
                <Option key={key} value={key}>{label}</Option>
              ))}
            </Select>
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
            <TextArea rows={4} placeholder="Enter description" />
          </Form.Item>
          <Form.Item name="version" label="Version">
            <Input placeholder="e.g., 1.0.0" />
          </Form.Item>

          {/* File Upload Section */}
          <Form.Item label="Attachments">
            <Upload
              multiple
              beforeUpload={(file) => {
                setPendingFiles(prev => [...prev, file]);
                return false;
              }}
              onRemove={(file) => {
                setPendingFiles(prev => prev.filter(f => f.name !== file.name));
              }}
              fileList={pendingFiles.map(f => ({ uid: f.name, name: f.name, status: 'done' as const }))}
              accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.zip,.txt"
            >
              <Button icon={<UploadOutlined />}>Add Files</Button>
            </Upload>
            <Text type="secondary" style={{ fontSize: 12, marginTop: 4, display: 'block' }}>
              Supported: PDF, DOCX, XLSX, PNG, JPG, ZIP (max 20MB)
            </Text>
          </Form.Item>

          {/* Show existing attachments when editing */}
          {editingUtility && attachments.length > 0 && (
            <Form.Item label="Existing Attachments">
              <List
                size="small"
                bordered
                dataSource={attachments}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <a
                        href={item.mongoFileId ? `/api/files/${item.mongoFileId}` : item.fileUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        <DownloadOutlined /> Download
                      </a>,
                      <Popconfirm
                        title="Delete this attachment?"
                        onConfirm={async () => {
                          await handleDeleteAttachment(item.id);
                          await loadAttachments(editingUtility.id);
                        }}
                      >
                        <Button type="link" size="small" danger>Delete</Button>
                      </Popconfirm>,
                    ]}
                  >
                    <List.Item.Meta
                      avatar={<FileTextOutlined />}
                      title={item.fileName}
                      description={`${(item.fileSize / 1024).toFixed(1)} KB`}
                    />
                  </List.Item>
                )}
              />
            </Form.Item>
          )}

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingUtility ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title="Utility Details"
        placement="right"
        width={600}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
      >
        {selectedUtility && (
          <div>
            <Title level={4}>{selectedUtility.title}</Title>
            <Tag color={typeColors[selectedUtility.type]}>{typeLabels[selectedUtility.type]}</Tag>
            {selectedUtility.version && <Tag>{selectedUtility.version}</Tag>}

            <Divider />

            <div style={{ marginBottom: 16 }}>
              <Text strong>Description:</Text>
              <p>{selectedUtility.description || 'No description'}</p>
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>Created By:</Text> {selectedUtility.createdBy?.username || '-'}
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>Created At:</Text> {dayjs(selectedUtility.createdAt).format('YYYY-MM-DD HH:mm')}
            </div>

            <Divider />

            <Title level={5}>Attachments ({attachments.length})</Title>

            {isAdmin && (
              <Upload
                beforeUpload={(file) => {
                  handleUploadAttachment(file);
                  return false;
                }}
                showUploadList={false}
                accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.txt"
              >
                <Button icon={<UploadOutlined />} loading={uploadLoading} style={{ marginBottom: 16 }}>
                  Upload File
                </Button>
              </Upload>
            )}

            <List
              size="small"
              dataSource={attachments}
              locale={{ emptyText: 'No attachments' }}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <a
                      href={item.mongoFileId ? `/api/files/${item.mongoFileId}` : item.fileUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      <DownloadOutlined /> Download
                    </a>,
                    isAdmin && (
                      <Popconfirm title="Delete this attachment?" onConfirm={() => handleDeleteAttachment(item.id)}>
                        <Button type="link" size="small" danger>Delete</Button>
                      </Popconfirm>
                    ),
                  ].filter(Boolean)}
                >
                  <List.Item.Meta
                    avatar={<FileTextOutlined />}
                    title={item.fileName}
                    description={`${(item.fileSize / 1024).toFixed(1)} KB • ${dayjs(item.uploadedAt).format('MMM DD, HH:mm')}`}
                  />
                </List.Item>
              )}
            />
          </div>
        )}
      </Drawer>
    </div>
  );
};

export default UtilityPage;
