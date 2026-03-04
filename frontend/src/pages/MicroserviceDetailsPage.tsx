import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Typography, Row, Col, Tag, Descriptions, Button, Empty, Divider, List } from 'antd';
import { ArrowLeftOutlined, ApiOutlined, UserOutlined, CalendarOutlined } from '@ant-design/icons';
import { microserviceService } from '../services/microserviceService';
import { Microservice } from '../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const statusColors: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'default',
  DEPRECATED: 'red',
  DEVELOPMENT: 'blue',
  MAINTENANCE: 'orange',
};

const MicroserviceDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [microservice, setMicroservice] = useState<Microservice | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadMicroservice = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await microserviceService.getById(id);
        setMicroservice(data);
      } catch (error) {
        console.error('Failed to load microservice', error);
      } finally {
        setLoading(false);
      }
    };
    loadMicroservice();
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!microservice) {
    return <Empty description="Microservice not found" />;
  }

  return (
    <div>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/microservices')}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Back to Microservices
      </Button>

      <Card>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <ApiOutlined style={{ fontSize: 24, color: '#1890ff' }} />
              <Title level={3} style={{ margin: 0 }}>{microservice.name}</Title>
              <Tag color={statusColors[microservice.status]}>{microservice.status}</Tag>
              {microservice.highRisk && <Tag color="red">HIGH RISK</Tag>}
            </div>
            {microservice.description && (
              <Text type="secondary">{microservice.description}</Text>
            )}
          </Col>
        </Row>

        <Divider />

        <Descriptions bordered column={{ xs: 1, sm: 2, md: 2 }}>
          <Descriptions.Item label="Version">{microservice.version || '-'}</Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={statusColors[microservice.status]}>{microservice.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Owner">
            {microservice.owner ? (
              <span><UserOutlined style={{ marginRight: 4 }} />{typeof microservice.owner === 'string' ? microservice.owner : microservice.owner.username}</span>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="High Risk">
            {microservice.highRisk ? <Tag color="red">Yes</Tag> : <Tag>No</Tag>}
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(microservice.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            <CalendarOutlined style={{ marginRight: 4 }} />
            {dayjs(microservice.updatedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
        </Descriptions>

        {(microservice as any).features && (microservice as any).features.length > 0 && (
          <>
            <Divider />
            <Title level={5}>Connected Features ({(microservice as any).features.length})</Title>
            <List
              size="small"
              bordered
              dataSource={(microservice as any).features}
              renderItem={(feature: any) => (
                <List.Item>
                  <Button type="link" onClick={() => navigate(`/features/${feature.id}`)}>
                    {feature.name}
                  </Button>
                  <Tag>{feature.domain}</Tag>
                </List.Item>
              )}
            />
          </>
        )}
      </Card>
    </div>
  );
};

export default MicroserviceDetailsPage;
