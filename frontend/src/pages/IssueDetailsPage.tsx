import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Typography, Row, Col, Tag, Descriptions, Button, Empty, Divider } from 'antd';
import { ArrowLeftOutlined, BugOutlined, CalendarOutlined, UserOutlined } from '@ant-design/icons';
import { issueService } from '../services/issueService';
import { Issue } from '../types';
import ResolutionDrawer from '../components/ResolutionDrawer';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const priorityColors: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  URGENT: 'red',
};

const statusColors: Record<string, string> = {
  OPEN: 'default',
  ASSIGNED: 'blue',
  IN_PROGRESS: 'orange',
  RESOLVED: 'green',
  CLOSED: 'default',
};

const IssueDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [issue, setIssue] = useState<Issue | null>(null);
  const [loading, setLoading] = useState(true);
  const [resolutionDrawerVisible, setResolutionDrawerVisible] = useState(false);

  useEffect(() => {
    const loadIssue = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await issueService.getById(id);
        setIssue(data);
      } catch (error) {
        console.error('Failed to load issue', error);
      } finally {
        setLoading(false);
      }
    };
    loadIssue();
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!issue) {
    return <Empty description="Issue not found" />;
  }

  const iss = issue as any;

  return (
    <div>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/issues')}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Back to Issues
      </Button>

      <Card>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <BugOutlined style={{ fontSize: 24, color: '#eb2f96' }} />
              <Title level={3} style={{ margin: 0 }}>{issue.title}</Title>
              <Tag color={priorityColors[issue.priority]}>{issue.priority}</Tag>
              <Tag color={statusColors[issue.status]}>{issue.status}</Tag>
            </div>
            {issue.description && (
              <Text type="secondary">{issue.description}</Text>
            )}
          </Col>
          <Col>
            <Button type="primary" onClick={() => setResolutionDrawerVisible(true)}>
              View Resolution
            </Button>
          </Col>
        </Row>

        <Divider />

        <Descriptions bordered column={{ xs: 1, sm: 2, md: 2 }}>
          <Descriptions.Item label="Priority">
            <Tag color={priorityColors[issue.priority]}>{issue.priority}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={statusColors[issue.status]}>{issue.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Main Feature">
            {issue.mainFeature ? (
              <Button type="link" onClick={() => navigate(`/features/${issue.mainFeature.id}`)} style={{ padding: 0 }}>
                {issue.mainFeature.name}
              </Button>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Domain">
            <Tag color="purple">{issue.mainFeature?.domain || '-'}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Owner">
            {issue.owner ? (
              <span><UserOutlined style={{ marginRight: 4 }} />{issue.owner.username}</span>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Assigned To">
            {issue.assignedTo ? (
              <span><UserOutlined style={{ marginRight: 4 }} />{issue.assignedTo.username}</span>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(issue.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(issue.updatedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
        </Descriptions>

        {issue.resultComment && (
          <>
            <Divider />
            <Title level={5}>Resolution Comment</Title>
            <Card size="small" style={{ background: '#f6ffed' }}>
              <Text>{issue.resultComment}</Text>
            </Card>
          </>
        )}

        {iss.attachmentUrl && (
          <>
            <Divider />
            <Title level={5}>Attachment</Title>
            <a href={iss.attachmentUrl} target="_blank" rel="noopener noreferrer">
              View Attachment
            </a>
          </>
        )}
      </Card>

      <ResolutionDrawer
        visible={resolutionDrawerVisible}
        onClose={() => setResolutionDrawerVisible(false)}
        issue={issue}
      />
    </div>
  );
};

export default IssueDetailsPage;
