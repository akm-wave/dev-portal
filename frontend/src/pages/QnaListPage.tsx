import React, { useEffect, useState } from 'react';
import { Card, List, Tag, Button, Input, Select, Space, Typography, Badge, Empty, Spin, Modal, Form, message, Upload, Image } from 'antd';
import { PlusOutlined, QuestionCircleOutlined, CheckCircleOutlined, MessageOutlined, EyeOutlined, UploadOutlined, LinkOutlined, DeleteOutlined, PaperClipOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import { useNavigate } from 'react-router-dom';
import { qnaService, QnaQuestionResponse, QnaQuestionRequest } from '../services/qnaService';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const QnaListPage: React.FC = () => {
  const [questions, setQuestions] = useState<QnaQuestionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<string>('recent');
  const [searchQuery, setSearchQuery] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [hyperlinks, setHyperlinks] = useState<{ url: string; title: string }[]>([]);
  const [newLinkUrl, setNewLinkUrl] = useState('');
  const [newLinkTitle, setNewLinkTitle] = useState('');
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    loadQuestions();
  }, [filter, pagination.current]);

  const loadQuestions = async () => {
    setLoading(true);
    try {
      const params: { filter?: string; page?: number; size?: number } = {
        page: pagination.current - 1,
        size: pagination.pageSize,
      };
      if (filter && filter !== 'recent') {
        params.filter = filter;
      }
      const response = await qnaService.getQuestions(params);
      setQuestions(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      console.error('Failed to load questions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadQuestions();
      return;
    }
    setLoading(true);
    try {
      const response = await qnaService.searchQuestions(searchQuery);
      setQuestions(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateQuestion = async (values: QnaQuestionRequest) => {
    setSubmitting(true);
    try {
      const tagsArray = values.tags ? (typeof values.tags === 'string' ? (values.tags as string).split(',').map(t => t.trim()) : values.tags) : [];
      const question = await qnaService.createQuestion({
        ...values,
        tags: tagsArray,
        hyperlinks: hyperlinks.length > 0 ? hyperlinks : undefined,
      });

      // Upload files if any
      for (const file of fileList) {
        if (file.originFileObj) {
          await qnaService.uploadAttachment(file.originFileObj, question.id);
        }
      }

      message.success('Question posted successfully!');
      setModalVisible(false);
      form.resetFields();
      setFileList([]);
      setHyperlinks([]);
      loadQuestions();
    } catch (error) {
      message.error('Failed to post question');
    } finally {
      setSubmitting(false);
    }
  };

  const addHyperlink = () => {
    if (newLinkUrl.trim()) {
      setHyperlinks([...hyperlinks, { url: newLinkUrl.trim(), title: newLinkTitle.trim() || newLinkUrl.trim() }]);
      setNewLinkUrl('');
      setNewLinkTitle('');
    }
  };

  const removeHyperlink = (index: number) => {
    setHyperlinks(hyperlinks.filter((_, i) => i !== index));
  };

  const isImageFile = (fileName: string) => {
    const ext = fileName.toLowerCase().split('.').pop();
    return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext || '');
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={2} style={{ margin: 0 }}>
          <QuestionCircleOutlined style={{ marginRight: 12 }} />
          Q&A Knowledge Base
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
          Ask Question
        </Button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap style={{ width: '100%', justifyContent: 'space-between' }}>
          <Space>
            <Input.Search
              placeholder="Search questions..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onSearch={handleSearch}
              style={{ width: 300 }}
              allowClear
            />
          </Space>
          <Select
            value={filter}
            onChange={setFilter}
            style={{ width: 150 }}
            options={[
              { value: 'recent', label: 'Recent' },
              { value: 'unanswered', label: 'Unanswered' },
              { value: 'active', label: 'Most Active' },
            ]}
          />
        </Space>
      </Card>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 50 }}>
          <Spin size="large" />
        </div>
      ) : questions.length === 0 ? (
        <Empty description="No questions found" />
      ) : (
        <List
          itemLayout="vertical"
          dataSource={questions}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: (page) => setPagination(prev => ({ ...prev, current: page })),
          }}
          renderItem={(question) => (
            <Card 
              style={{ marginBottom: 12, cursor: 'pointer' }}
              hoverable
              onClick={() => navigate(`/qna/${question.id}`)}
            >
              <div style={{ display: 'flex', gap: 16 }}>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 60 }}>
                  <div style={{ textAlign: 'center', marginBottom: 8 }}>
                    <div style={{ fontSize: 18, fontWeight: 600 }}>{question.upvotes}</div>
                    <Text type="secondary" style={{ fontSize: 11 }}>votes</Text>
                  </div>
                  <Badge 
                    count={question.answerCount} 
                    style={{ 
                      backgroundColor: question.answerCount > 0 ? (question.isResolved ? '#52c41a' : '#1890ff') : '#d9d9d9'
                    }}
                  >
                    <div style={{ 
                      padding: '4px 8px', 
                      border: `1px solid ${question.isResolved ? '#52c41a' : '#d9d9d9'}`,
                      borderRadius: 4,
                      background: question.isResolved ? '#f6ffed' : 'transparent'
                    }}>
                      <MessageOutlined />
                    </div>
                  </Badge>
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                    {question.isResolved && (
                      <Tag color="success" icon={<CheckCircleOutlined />}>Resolved</Tag>
                    )}
                    <Title level={5} style={{ margin: 0 }}>{question.title}</Title>
                  </div>
                  <Paragraph ellipsis={{ rows: 2 }} type="secondary" style={{ marginBottom: 8 }}>
                    {question.content.replace(/<[^>]*>/g, '')}
                  </Paragraph>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Space size={4} wrap>
                      {question.tags?.map((tag, idx) => (
                        <Tag key={idx} color="blue">{tag}</Tag>
                      ))}
                    </Space>
                    <Space>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        <EyeOutlined /> {question.viewCount}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        asked {dayjs(question.createdAt).fromNow()} by{' '}
                        <Text strong>{question.createdBy?.username || 'Unknown'}</Text>
                      </Text>
                    </Space>
                  </div>
                </div>
              </div>
            </Card>
          )}
        />
      )}

      <Modal
        title="Ask a Question"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleCreateQuestion}>
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: 'Please enter a title' }]}
          >
            <Input placeholder="What's your question? Be specific." />
          </Form.Item>
          <Form.Item
            name="content"
            label="Description"
            rules={[{ required: true, message: 'Please describe your question' }]}
          >
            <TextArea rows={6} placeholder="Provide details about your question..." />
          </Form.Item>
          <Form.Item
            name="tags"
            label="Tags"
            help="Comma-separated tags (e.g., microservice, api, database)"
          >
            <Input placeholder="api, database, authentication" />
          </Form.Item>

          <Form.Item label="Attachments">
            <Upload
              fileList={fileList}
              onChange={({ fileList }) => setFileList(fileList)}
              beforeUpload={() => false}
              multiple
              listType="picture"
            >
              <Button icon={<UploadOutlined />}>Add Files (Images, PDF, DOCX, etc.)</Button>
            </Upload>
          </Form.Item>

          <Form.Item label="Hyperlinks">
            <Space direction="vertical" style={{ width: '100%' }}>
              {hyperlinks.map((link, index) => (
                <div key={index} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <LinkOutlined />
                  <a href={link.url} target="_blank" rel="noopener noreferrer" style={{ flex: 1 }}>
                    {link.title || link.url}
                  </a>
                  <Button type="text" danger icon={<DeleteOutlined />} onClick={() => removeHyperlink(index)} />
                </div>
              ))}
              <Space.Compact style={{ width: '100%' }}>
                <Input
                  placeholder="URL (e.g., https://example.com)"
                  value={newLinkUrl}
                  onChange={(e) => setNewLinkUrl(e.target.value)}
                  style={{ flex: 2 }}
                />
                <Input
                  placeholder="Title (optional)"
                  value={newLinkTitle}
                  onChange={(e) => setNewLinkTitle(e.target.value)}
                  style={{ flex: 1 }}
                />
                <Button type="primary" onClick={addHyperlink} disabled={!newLinkUrl.trim()}>
                  Add
                </Button>
              </Space.Compact>
            </Space>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting}>
                Post Question
              </Button>
              <Button onClick={() => setModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default QnaListPage;
