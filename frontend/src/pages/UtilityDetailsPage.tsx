import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Typography, Row, Col, Tag, Descriptions, Button, Empty, Divider, List } from 'antd';
import { ArrowLeftOutlined, FileTextOutlined, CalendarOutlined, UserOutlined, DownloadOutlined } from '@ant-design/icons';
import { utilityService } from '../services/utilityService';
import { Utility, UtilityAttachment } from '../types';
import VersionHistory from '../components/VersionHistory';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const typeLabels: Record<string, string> = {
  MOP: 'MOP',
  CR_REQUIREMENT: 'CR Requirement',
  DEVELOPMENT_GUIDELINE: 'Development Guideline',
  SOP: 'SOP',
  OTHERS: 'Others',
};

const typeColors: Record<string, string> = {
  MOP: 'blue',
  CR_REQUIREMENT: 'purple',
  DEVELOPMENT_GUIDELINE: 'green',
  SOP: 'orange',
  OTHERS: 'default',
};

const UtilityDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [utility, setUtility] = useState<Utility | null>(null);
  const [attachments, setAttachments] = useState<UtilityAttachment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUtility = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await utilityService.getById(id);
        setUtility(data);
        const attachmentData = await utilityService.getAttachments(id);
        setAttachments(attachmentData);
      } catch (error) {
        console.error('Failed to load utility', error);
      } finally {
        setLoading(false);
      }
    };
    loadUtility();
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!utility) {
    return <Empty description="Utility not found" />;
  }

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
    <div>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/utilities')}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Back to Utilities
      </Button>

      <Card>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <FileTextOutlined style={{ fontSize: 24, color: '#14b8a6' }} />
              <Title level={3} style={{ margin: 0 }}>{utility.title}</Title>
              <Tag color={typeColors[utility.type]}>{typeLabels[utility.type]}</Tag>
              {utility.version && <Tag>v{utility.version}</Tag>}
            </div>
          </Col>
        </Row>

        <Divider />

        <Descriptions bordered column={{ xs: 1, sm: 2, md: 2 }}>
          <Descriptions.Item label="Type">
            <Tag color={typeColors[utility.type]}>{typeLabels[utility.type]}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Version">{utility.version || '-'}</Descriptions.Item>
          <Descriptions.Item label="Created By">
            {utility.createdBy ? (
              <span><UserOutlined style={{ marginRight: 4 }} />{utility.createdBy.username}</span>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Attachments">{attachments.length} files</Descriptions.Item>
          <Descriptions.Item label="Created At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(utility.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(utility.updatedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
        </Descriptions>

        {utility.description && (
          <>
            <Divider />
            <Title level={5}>Description</Title>
            <Card size="small" style={{ background: '#fafafa' }}>
              <Text style={{ whiteSpace: 'pre-wrap' }}>{utility.description}</Text>
            </Card>
          </>
        )}

        <Divider />
        <Title level={5}>Attachments ({attachments.length})</Title>
        
        {attachments.length > 0 ? (
          <List
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
                ]}
              >
                <List.Item.Meta
                  avatar={<span style={{ fontSize: 24 }}>{getFileIcon(item.fileType)}</span>}
                  title={item.fileName}
                  description={
                    <span>
                      {formatFileSize(item.fileSize)} • {item.fileType} • 
                      Uploaded {dayjs(item.uploadedAt).format('MMM DD, YYYY HH:mm')}
                      {item.uploadedBy && ` by ${item.uploadedBy.username}`}
                    </span>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description="No attachments" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}

        <Divider />
        <VersionHistory utilityId={id!} onRevert={() => window.location.reload()} />
      </Card>
    </div>
  );
};

export default UtilityDetailsPage;
