import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, Card, Row, Col, Drawer, Typography, Divider, Upload, List, Tabs, Timeline, Avatar } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, BugOutlined, UserOutlined, CheckCircleOutlined, UploadOutlined, FileOutlined, CommentOutlined, PaperClipOutlined, SolutionOutlined, EyeOutlined } from '@ant-design/icons';
import AIRephraseButton from '../components/AIRephraseButton';
import DuplicateDetection from '../components/DuplicateDetection';
import ResolutionDrawer from '../components/ResolutionDrawer';
import { issueService } from '../services/issueService';
import { featureService } from '../services/featureService';
import { userService } from '../services/userService';
import { Issue, IssueRequest, IssueStatus, IssuePriority, IssueCategory, Feature, UserSummary, IssueAttachment, IssueComment } from '../types';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;
const { Text, Title } = Typography;

const priorityColors: Record<IssuePriority, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
};

const statusColors: Record<IssueStatus, string> = {
  OPEN: 'default',
  ASSIGNED: 'blue',
  IN_PROGRESS: 'orange',
  RESOLVED: 'green',
  CLOSED: 'default',
};

const categoryColors: Record<IssueCategory, string> = {
  TECH_DEBT: 'volcano',
  TECHNICAL_ISSUE: 'orange',
  PROD_ISSUE: 'red',
  BUG: 'magenta',
  ENHANCEMENT: 'green',
  SECURITY: 'purple',
  PERFORMANCE: 'cyan',
  OTHER: 'default',
};

const categoryLabels: Record<IssueCategory, string> = {
  TECH_DEBT: 'Tech Debt',
  TECHNICAL_ISSUE: 'Technical Issue',
  PROD_ISSUE: 'Production Issue',
  BUG: 'Bug',
  ENHANCEMENT: 'Enhancement',
  SECURITY: 'Security',
  PERFORMANCE: 'Performance',
  OTHER: 'Other',
};

const IssuePage: React.FC = () => {
  const [issues, setIssues] = useState<Issue[]>([]);
  const [features, setFeatures] = useState<Feature[]>([]);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [selectedIssue, setSelectedIssue] = useState<Issue | null>(null);
  const [editingIssue, setEditingIssue] = useState<Issue | null>(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [attachments, setAttachments] = useState<IssueAttachment[]>([]);
  const [comments, setComments] = useState<IssueComment[]>([]);
  const [isOwner, setIsOwner] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [uploadLoading, setUploadLoading] = useState(false);
  const [resolutionDrawerVisible, setResolutionDrawerVisible] = useState(false);
  const [resolutionIssue, setResolutionIssue] = useState<Issue | null>(null);
  const [formTitle, setFormTitle] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [resolveForm] = Form.useForm();

  useEffect(() => {
    loadIssues();
    loadFeatures();
    loadUsers();
  }, [pagination.current, pagination.pageSize]);

  const loadIssues = async () => {
    setLoading(true);
    try {
      const response = await issueService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      setIssues(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load issues');
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

  const loadUsers = async () => {
    try {
      const data = await userService.getApprovedUsers();
      setUsers(data);
    } catch (error) {
      console.error('Failed to load users');
    }
  };

  const loadIssueDetails = async (issueId: string) => {
    try {
      const [attachmentsData, commentsData, ownerCheck] = await Promise.all([
        issueService.getAttachments(issueId),
        issueService.getComments(issueId),
        issueService.isOwner(issueId),
      ]);
      setAttachments(attachmentsData);
      setComments(commentsData);
      setIsOwner(ownerCheck);
    } catch (error) {
      console.error('Failed to load issue details');
    }
  };

  const handleCreate = () => {
    setEditingIssue(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Issue) => {
    setEditingIssue(record);
    form.setFieldsValue({
      ...record,
      mainFeatureId: record.mainFeature.id,
      assignedToId: record.assignedTo?.id,
      ownerId: record.owner?.id,
    });
    setModalVisible(true);
  };

  const handleViewDetails = async (record: Issue) => {
    setSelectedIssue(record);
    setDrawerVisible(true);
    await loadIssueDetails(record.id);
  };

  const handleUploadAttachment = async (file: File) => {
    if (!selectedIssue) return;
    setUploadLoading(true);
    try {
      await issueService.uploadAttachment(selectedIssue.id, file);
      message.success('File uploaded successfully');
      await loadIssueDetails(selectedIssue.id);
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploadLoading(false);
    }
  };

  const handleDeleteAttachment = async (attachmentId: string) => {
    if (!selectedIssue) return;
    try {
      await issueService.deleteAttachment(selectedIssue.id, attachmentId);
      message.success('Attachment deleted');
      await loadIssueDetails(selectedIssue.id);
    } catch (error) {
      message.error('Failed to delete attachment');
    }
  };

  const handleAddComment = async () => {
    if (!selectedIssue || !commentText.trim()) return;
    try {
      await issueService.addComment(selectedIssue.id, { content: commentText, isResolutionComment: isOwner });
      message.success('Comment added');
      setCommentText('');
      await loadIssueDetails(selectedIssue.id);
    } catch (error) {
      message.error('Failed to add comment');
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!selectedIssue) return;
    try {
      await issueService.deleteComment(selectedIssue.id, commentId);
      message.success('Comment deleted');
      await loadIssueDetails(selectedIssue.id);
    } catch (error) {
      message.error('Failed to delete comment');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await issueService.delete(id);
      message.success('Issue deleted successfully');
      loadIssues();
    } catch (error) {
      message.error('Failed to delete issue');
    }
  };

  const handleSubmit = async (values: IssueRequest) => {
    try {
      if (editingIssue) {
        await issueService.update(editingIssue.id, values);
        message.success('Issue updated successfully');
      } else {
        await issueService.create(values);
        message.success('Issue created successfully');
      }
      setModalVisible(false);
      loadIssues();
    } catch (error) {
      message.error('Operation failed');
    }
  };

  const handleResolve = async (values: { resultComment: string; attachmentUrl?: string }) => {
    if (!selectedIssue) return;
    try {
      await issueService.resolve(selectedIssue.id, values.resultComment, values.attachmentUrl);
      message.success('Issue resolved successfully');
      setDrawerVisible(false);
      loadIssues();
    } catch (error) {
      message.error('Failed to resolve issue');
    }
  };

  const columns: ColumnsType<Issue> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (title: string, record: Issue) => (
        <Button type="link" onClick={() => handleViewDetails(record)} style={{ padding: 0 }}>
          <BugOutlined style={{ marginRight: 8 }} />
          <strong>{title}</strong>
        </Button>
      ),
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority: IssuePriority) => (
        <Tag color={priorityColors[priority]}>{priority}</Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: IssueStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      render: (category: IssueCategory) => (
        <Tag color={categoryColors[category]}>{categoryLabels[category]}</Tag>
      ),
    },
    {
      title: 'Main Feature',
      dataIndex: 'mainFeature',
      key: 'mainFeature',
      render: (feature: Issue['mainFeature']) => (
        <Tag color="purple">{feature.name}</Tag>
      ),
    },
    {
      title: 'Assigned To',
      dataIndex: 'assignedTo',
      key: 'assignedTo',
      render: (user: Issue['assignedTo']) => user ? (
        <Tag icon={<UserOutlined />}>{user.username}</Tag>
      ) : '-',
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
      render: (_, record: Issue) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/issues/${record.id}`)} title="View Details" />
          <Button
            type="link"
            size="small"
            icon={<SolutionOutlined />}
            onClick={() => {
              setResolutionIssue(record);
              setResolutionDrawerVisible(true);
            }}
            title="Resolution"
          />
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
              placeholder="Search issues..."
              prefix={<SearchOutlined />}
              style={{ width: 250 }}
              allowClear
            />
          </Col>
          {isAdmin && (
            <Col>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Create Issue
              </Button>
            </Col>
          )}
        </Row>

        <Table
          columns={columns}
          dataSource={issues}
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
        title={editingIssue ? 'Edit Issue' : 'Create Issue'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        destroyOnHidden
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          {!editingIssue && formTitle && (
            <DuplicateDetection
              entityType="ISSUE"
              title={formTitle}
              description={formDescription}
              onSelectSimilar={(item) => {
                message.info(`Similar issue found: ${item.similarEntityName}`);
              }}
            />
          )}
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: 'Please enter issue title' }]}
          >
            <Input 
              placeholder="Enter issue title" 
              onChange={(e) => setFormTitle(e.target.value)}
            />
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
            rules={[{ required: true, message: 'Description is required' }]}
          >
            <TextArea rows={4} placeholder="Describe the issue in detail" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="priority" label="Priority" initialValue="MEDIUM">
                <Select>
                  <Option value="LOW">Low</Option>
                  <Option value="MEDIUM">Medium</Option>
                  <Option value="HIGH">High</Option>
                  <Option value="URGENT">Urgent</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="category" label="Category" initialValue="OTHER">
                <Select>
                  <Option value="TECH_DEBT">Tech Debt</Option>
                  <Option value="TECHNICAL_ISSUE">Technical Issue</Option>
                  <Option value="PROD_ISSUE">Production Issue</Option>
                  <Option value="BUG">Bug</Option>
                  <Option value="ENHANCEMENT">Enhancement</Option>
                  <Option value="SECURITY">Security</Option>
                  <Option value="PERFORMANCE">Performance</Option>
                  <Option value="OTHER">Other</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="status" label="Status" initialValue="OPEN">
                <Select>
                  <Option value="OPEN">Open</Option>
                  <Option value="ASSIGNED">Assigned</Option>
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
          <Form.Item
            name="ownerId"
            label="Owner"
            rules={[{ required: true, message: 'Please select an owner' }]}
          >
            <Select placeholder="Select owner" showSearch optionFilterProp="children">
              {users.map(u => (
                <Option key={u.id} value={u.id}>{u.fullName || u.username} ({u.email})</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingIssue ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title="Issue Details"
        placement="right"
        width={600}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
      >
        {selectedIssue && (
          <div>
            <Title level={4}>{selectedIssue.title}</Title>
            <Space style={{ marginBottom: 16 }}>
              <Tag color={priorityColors[selectedIssue.priority]}>{selectedIssue.priority}</Tag>
              <Tag color={statusColors[selectedIssue.status]}>{selectedIssue.status}</Tag>
              {isOwner && <Tag color="blue">You are the Owner</Tag>}
            </Space>

            <Divider />

            <div style={{ marginBottom: 16 }}>
              <Text strong>Description:</Text>
              <p>{selectedIssue.description}</p>
            </div>

            <div style={{ marginBottom: 16 }}>
              <Text strong>Main Feature:</Text>
              <p><Tag color="purple">{selectedIssue.mainFeature.name}</Tag></p>
            </div>

            <Row gutter={16}>
              <Col span={12}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>Owner:</Text>
                  <p>{selectedIssue.owner?.username || 'Not assigned'}</p>
                </div>
              </Col>
              <Col span={12}>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>Assigned To:</Text>
                  <p>{selectedIssue.assignedTo?.username || 'Not assigned'}</p>
                </div>
              </Col>
            </Row>

            <div style={{ marginBottom: 16 }}>
              <Text strong>Created By:</Text> {selectedIssue.createdBy} | <Text strong>Created At:</Text> {dayjs(selectedIssue.createdAt).format('YYYY-MM-DD HH:mm')}
            </div>

            <Divider />

            {/* Resolution Section */}
            <Tabs
              defaultActiveKey="attachments"
              items={[
                {
                  key: 'attachments',
                  label: <span><PaperClipOutlined /> Attachments ({attachments.length})</span>,
                  children: (
                    <div>
                      {isOwner && (
                        <Upload
                          beforeUpload={(file) => {
                            handleUploadAttachment(file);
                            return false;
                          }}
                          showUploadList={false}
                          accept=".jpg,.jpeg,.png,.pdf,.doc,.docx,.txt"
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
                              <a href={item.fileUrl} target="_blank" rel="noopener noreferrer">View</a>,
                              isOwner && (
                                <Popconfirm title="Delete this attachment?" onConfirm={() => handleDeleteAttachment(item.id)}>
                                  <Button type="link" size="small" danger>Delete</Button>
                                </Popconfirm>
                              ),
                            ].filter(Boolean)}
                          >
                            <List.Item.Meta
                              avatar={<FileOutlined />}
                              title={item.fileName}
                              description={`${(item.fileSize / 1024).toFixed(1)} KB • ${dayjs(item.createdAt).format('MMM DD, HH:mm')}`}
                            />
                          </List.Item>
                        )}
                      />
                    </div>
                  ),
                },
                {
                  key: 'comments',
                  label: <span><CommentOutlined /> Comments ({comments.length})</span>,
                  children: (
                    <div>
                      <div style={{ marginBottom: 16 }}>
                        <TextArea
                          rows={2}
                          value={commentText}
                          onChange={(e) => setCommentText(e.target.value)}
                          placeholder={isOwner ? "Add a resolution comment..." : "Add a comment..."}
                        />
                        <Button
                          type="primary"
                          size="small"
                          style={{ marginTop: 8 }}
                          onClick={handleAddComment}
                          disabled={!commentText.trim()}
                        >
                          Add Comment
                        </Button>
                      </div>
                      <Timeline
                        items={comments.map((c) => ({
                          color: c.isResolutionComment ? 'green' : 'blue',
                          children: (
                            <div>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Space>
                                  <Avatar size="small" icon={<UserOutlined />} />
                                  <Text strong>{c.user.username}</Text>
                                  {c.isResolutionComment && <Tag color="green" style={{ fontSize: 10 }}>Resolution</Tag>}
                                </Space>
                                <Text type="secondary" style={{ fontSize: 11 }}>{dayjs(c.createdAt).format('MMM DD, HH:mm')}</Text>
                              </div>
                              <p style={{ margin: '8px 0 0 28px' }}>{c.content}</p>
                            </div>
                          ),
                        }))}
                      />
                    </div>
                  ),
                },
              ]}
            />

            <Divider />

            {selectedIssue.status !== 'RESOLVED' && selectedIssue.status !== 'CLOSED' && isOwner && (
              <div>
                <Title level={5}>Resolve Issue</Title>
                <Form form={resolveForm} layout="vertical" onFinish={handleResolve}>
                  <Form.Item
                    name="resultComment"
                    label="Result Comment"
                    rules={[{ required: true, message: 'Please add a result comment' }]}
                  >
                    <TextArea rows={3} placeholder="Describe the resolution" />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" icon={<CheckCircleOutlined />}>
                    Mark as Resolved
                  </Button>
                </Form>
              </div>
            )}

            {selectedIssue.resultComment && (
              <div style={{ marginTop: 16, padding: 12, background: '#f6ffed', borderRadius: 8 }}>
                <Text strong style={{ color: '#52c41a' }}>Resolution:</Text>
                <p style={{ margin: '8px 0 0' }}>{selectedIssue.resultComment}</p>
              </div>
            )}
          </div>
        )}
      </Drawer>
      {/* Resolution Drawer */}
      <ResolutionDrawer
        visible={resolutionDrawerVisible}
        onClose={() => {
          setResolutionDrawerVisible(false);
          setResolutionIssue(null);
        }}
        issue={resolutionIssue}
      />
    </div>
  );
};

export default IssuePage;
