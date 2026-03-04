import React, { useState, useEffect } from 'react';
import {
  Drawer,
  Timeline,
  Typography,
  Button,
  Input,
  Upload,
  Space,
  Tag,
  List,
  Spin,
  Empty,
  message,
  Popconfirm,
  Checkbox,
  Divider,
} from 'antd';
import {
  UploadOutlined,
  FileTextOutlined,
  DownloadOutlined,
  DeleteOutlined,
  SendOutlined,
  PaperClipOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { issueResolutionService } from '../services/issueResolutionService';
import { IssueResolution, IssueResolutionAttachment, Issue } from '../types';
import { useAuth } from '../contexts/AuthContext';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const { Text, Title } = Typography;
const { TextArea } = Input;

interface ResolutionDrawerProps {
  visible: boolean;
  onClose: () => void;
  issue: Issue | null;
}

const ResolutionDrawer: React.FC<ResolutionDrawerProps> = ({ visible, onClose, issue }) => {
  const [resolutions, setResolutions] = useState<IssueResolution[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [comment, setComment] = useState('');
  const [isResolutionComment, setIsResolutionComment] = useState(false);
  const [files, setFiles] = useState<File[]>([]);
  const { user, isAdmin } = useAuth();

  const canEdit = isAdmin || (issue?.owner?.username === user?.username);

  useEffect(() => {
    if (visible && issue) {
      loadResolutions();
    }
  }, [visible, issue]);

  const loadResolutions = async () => {
    if (!issue) return;
    setLoading(true);
    try {
      const data = await issueResolutionService.getAllResolutions(issue.id.toString());
      setResolutions(data);
    } catch (error) {
      message.error('Failed to load resolutions');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!issue || (!comment.trim() && files.length === 0)) {
      message.warning('Please add a comment or attach files');
      return;
    }

    setSubmitting(true);
    try {
      await issueResolutionService.createResolution(
        issue.id.toString(),
        comment,
        isResolutionComment,
        files
      );
      message.success('Resolution entry added');
      setComment('');
      setIsResolutionComment(false);
      setFiles([]);
      loadResolutions();
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Failed to add resolution');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteAttachment = async (resolution: IssueResolution, attachment: IssueResolutionAttachment) => {
    if (!issue) return;
    try {
      await issueResolutionService.deleteAttachment(
        issue.id.toString(),
        resolution.id,
        attachment.id
      );
      message.success('Attachment deleted');
      loadResolutions();
    } catch (error) {
      message.error('Failed to delete attachment');
    }
  };

  const handleFileRemove = (file: File) => {
    setFiles(files.filter(f => f !== file));
  };

  const getFileIcon = (fileType: string) => {
    if (fileType?.includes('image')) return '🖼️';
    if (fileType?.includes('pdf')) return '📄';
    if (fileType?.includes('word') || fileType?.includes('document')) return '📝';
    if (fileType?.includes('excel') || fileType?.includes('sheet')) return '📊';
    if (fileType?.includes('zip')) return '📦';
    return '📎';
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  };

  return (
    <Drawer
      title={
        <Space>
          <FileTextOutlined />
          <span>Resolution Timeline</span>
          {issue && <Tag color="blue">{issue.title}</Tag>}
        </Space>
      }
      placement="right"
      width={600}
      onClose={onClose}
      open={visible}
      extra={
        <Text type="secondary">
          {resolutions.length} {resolutions.length === 1 ? 'entry' : 'entries'}
        </Text>
      }
    >
      {loading ? (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          {/* Add Resolution Form */}
          {canEdit && (
            <div style={{ marginBottom: 24, padding: 16, background: '#fafafa', borderRadius: 8 }}>
              <Title level={5} style={{ marginBottom: 12 }}>Add Resolution Entry</Title>
              <TextArea
                rows={3}
                placeholder="Enter your comment or resolution details..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                style={{ marginBottom: 12 }}
              />
              
              <Space direction="vertical" style={{ width: '100%' }}>
                <Checkbox
                  checked={isResolutionComment}
                  onChange={(e) => setIsResolutionComment(e.target.checked)}
                >
                  Mark as resolution comment
                </Checkbox>

                <Upload
                  multiple
                  beforeUpload={(file) => {
                    setFiles([...files, file]);
                    return false;
                  }}
                  fileList={[]}
                  accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg,.zip,.txt"
                >
                  <Button icon={<UploadOutlined />}>Attach Files</Button>
                </Upload>

                {files.length > 0 && (
                  <List
                    size="small"
                    dataSource={files}
                    renderItem={(file) => (
                      <List.Item
                        actions={[
                          <Button
                            type="link"
                            size="small"
                            danger
                            onClick={() => handleFileRemove(file)}
                          >
                            Remove
                          </Button>,
                        ]}
                      >
                        <Space>
                          <PaperClipOutlined />
                          <Text>{file.name}</Text>
                          <Text type="secondary">({formatFileSize(file.size)})</Text>
                        </Space>
                      </List.Item>
                    )}
                  />
                )}

                <Button
                  type="primary"
                  icon={<SendOutlined />}
                  onClick={handleSubmit}
                  loading={submitting}
                  disabled={!comment.trim() && files.length === 0}
                >
                  Submit
                </Button>
              </Space>
            </div>
          )}

          <Divider />

          {/* Resolution Timeline */}
          {resolutions.length === 0 ? (
            <Empty description="No resolution entries yet" />
          ) : (
            <Timeline
              items={resolutions.map((resolution) => ({
                color: resolution.isResolutionComment ? 'green' : 'blue',
                dot: resolution.isResolutionComment ? (
                  <Tag color="green" style={{ margin: 0 }}>✓</Tag>
                ) : (
                  <ClockCircleOutlined />
                ),
                children: (
                  <div style={{ paddingBottom: 16 }}>
                    <Space direction="vertical" style={{ width: '100%' }}>
                      <Space>
                        <Text strong>{resolution.createdBy?.username || 'Unknown'}</Text>
                        <Text type="secondary">
                          {dayjs(resolution.createdAt).fromNow()}
                        </Text>
                        {resolution.isResolutionComment && (
                          <Tag color="green">Resolution</Tag>
                        )}
                      </Space>

                      {resolution.comment && (
                        <div
                          style={{
                            padding: 12,
                            background: '#f5f5f5',
                            borderRadius: 6,
                            whiteSpace: 'pre-wrap',
                          }}
                        >
                          {resolution.comment}
                        </div>
                      )}

                      {resolution.attachments && resolution.attachments.length > 0 && (
                        <List
                          size="small"
                          bordered
                          dataSource={resolution.attachments}
                          renderItem={(attachment) => (
                            <List.Item
                              actions={[
                                <a
                                  href={issueResolutionService.downloadFile(attachment.mongoFileId)}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                >
                                  <DownloadOutlined /> Download
                                </a>,
                                canEdit && (
                                  <Popconfirm
                                    title="Delete this attachment?"
                                    onConfirm={() => handleDeleteAttachment(resolution, attachment)}
                                  >
                                    <Button type="link" size="small" danger icon={<DeleteOutlined />} />
                                  </Popconfirm>
                                ),
                              ].filter(Boolean)}
                            >
                              <List.Item.Meta
                                avatar={<span style={{ fontSize: 20 }}>{getFileIcon(attachment.fileType)}</span>}
                                title={attachment.fileName}
                                description={
                                  <Space>
                                    <Text type="secondary">{formatFileSize(attachment.fileSize)}</Text>
                                    <Text type="secondary">•</Text>
                                    <Text type="secondary">
                                      {dayjs(attachment.uploadedAt).format('MMM DD, HH:mm')}
                                    </Text>
                                  </Space>
                                }
                              />
                            </List.Item>
                          )}
                        />
                      )}
                    </Space>
                  </div>
                ),
              }))}
            />
          )}
        </>
      )}
    </Drawer>
  );
};

export default ResolutionDrawer;
