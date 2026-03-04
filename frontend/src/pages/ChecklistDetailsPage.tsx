import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Typography, Row, Col, Tag, Descriptions, Button, Empty, Divider, Progress, List } from 'antd';
import { ArrowLeftOutlined, CheckSquareOutlined, CalendarOutlined } from '@ant-design/icons';
import { checklistService } from '../services/checklistService';
import { Checklist } from '../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const statusColors: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'default',
  DRAFT: 'blue',
  ARCHIVED: 'orange',
};

const priorityColors: Record<string, string> = {
  LOW: 'default',
  MEDIUM: 'blue',
  HIGH: 'orange',
  CRITICAL: 'red',
};

const ChecklistDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [checklist, setChecklist] = useState<Checklist | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadChecklist = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await checklistService.getById(id);
        setChecklist(data);
      } catch (error) {
        console.error('Failed to load checklist', error);
      } finally {
        setLoading(false);
      }
    };
    loadChecklist();
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!checklist) {
    return <Empty description="Checklist not found" />;
  }

  const checkpoints = (checklist as any).checkpoints || [];
  const completedItems = checkpoints.filter((cp: any) => cp.status === 'DONE').length || 0;
  const totalItems = checkpoints.length || 0;
  const progress = totalItems > 0 ? Math.round((completedItems / totalItems) * 100) : 0;

  return (
    <div>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/checklists')}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Back to Checklists
      </Button>

      <Card>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <CheckSquareOutlined style={{ fontSize: 24, color: '#52c41a' }} />
              <Title level={3} style={{ margin: 0 }}>{checklist.name}</Title>
              <Tag color={statusColors[checklist.status]}>{checklist.status}</Tag>
            </div>
            {checklist.description && (
              <Text type="secondary">{checklist.description}</Text>
            )}
          </Col>
          <Col>
            <Progress type="circle" percent={progress} width={80} />
          </Col>
        </Row>

        <Divider />

        <Descriptions bordered column={{ xs: 1, sm: 2, md: 2 }}>
          <Descriptions.Item label="Status">
            <Tag color={statusColors[checklist.status]}>{checklist.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Priority">
            <Tag color={priorityColors[checklist.priority || 'MEDIUM']}>{checklist.priority || 'MEDIUM'}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Total Checkpoints">{totalItems}</Descriptions.Item>
          <Descriptions.Item label="Completed">{completedItems}</Descriptions.Item>
          <Descriptions.Item label="Created At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(checklist.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(checklist.updatedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
        </Descriptions>

        {checkpoints && checkpoints.length > 0 && (
          <>
            <Divider />
            <Title level={5}>Checkpoints ({checkpoints.length})</Title>
            <List
              size="small"
              bordered
              dataSource={checkpoints}
              renderItem={(checkpoint: any) => (
                <List.Item>
                  <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                    <span>
                      <Tag color={checkpoint.status === 'DONE' ? 'green' : checkpoint.status === 'BLOCKED' ? 'red' : 'default'}>
                        {checkpoint.status}
                      </Tag>
                      {checkpoint.name}
                    </span>
                    <Text type="secondary">{checkpoint.description}</Text>
                  </div>
                </List.Item>
              )}
            />
          </>
        )}
      </Card>
    </div>
  );
};

export default ChecklistDetailsPage;
