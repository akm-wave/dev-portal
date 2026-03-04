import React, { useState, useEffect } from 'react';
import { Card, List, Tag, Typography, Button, Space, Spin, Empty, message, Tooltip } from 'antd';
import { BulbOutlined, CheckOutlined, CloseOutlined, ApiOutlined, BugOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { aiService } from '../services/aiService';
import { ReleaseRecommendation } from '../types';

const { Text, Paragraph } = Typography;

interface ReleaseRecommendationsProps {
  releaseId: string;
  onAccept?: (recommendation: ReleaseRecommendation) => void;
}

const ReleaseRecommendations: React.FC<ReleaseRecommendationsProps> = ({
  releaseId,
  onAccept,
}) => {
  const [recommendations, setRecommendations] = useState<ReleaseRecommendation[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchRecommendations = async () => {
    setLoading(true);
    try {
      const data = await aiService.getRecommendationsForRelease(releaseId);
      setRecommendations(data);
    } catch (error) {
      console.error('Error fetching recommendations:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (releaseId) {
      fetchRecommendations();
    }
  }, [releaseId]);

  const handleAccept = async (recommendation: ReleaseRecommendation) => {
    try {
      await aiService.acceptRecommendation(recommendation.id);
      setRecommendations(prev => prev.filter(r => r.id !== recommendation.id));
      message.success('Recommendation accepted');
      onAccept?.(recommendation);
    } catch (error) {
      message.error('Failed to accept recommendation');
    }
  };

  const handleDismiss = async (recommendationId: string) => {
    try {
      await aiService.dismissRecommendation(recommendationId);
      setRecommendations(prev => prev.filter(r => r.id !== recommendationId));
      message.info('Recommendation dismissed');
    } catch (error) {
      message.error('Failed to dismiss recommendation');
    }
  };

  const getEntityIcon = (entityType: string) => {
    switch (entityType) {
      case 'MICROSERVICE':
        return <ApiOutlined style={{ color: '#5b6cf0' }} />;
      case 'ISSUE':
        return <BugOutlined style={{ color: '#f59e0b' }} />;
      case 'HOTFIX':
        return <ThunderboltOutlined style={{ color: '#ef4444' }} />;
      default:
        return <BulbOutlined style={{ color: '#10b981' }} />;
    }
  };

  const getEntityColor = (entityType: string) => {
    switch (entityType) {
      case 'MICROSERVICE':
        return 'blue';
      case 'ISSUE':
        return 'orange';
      case 'HOTFIX':
        return 'red';
      default:
        return 'green';
    }
  };

  if (loading) {
    return (
      <Card 
        size="small" 
        title={
          <Space>
            <BulbOutlined style={{ color: '#f59e0b' }} />
            <Text strong>AI Recommendations</Text>
          </Space>
        }
      >
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Spin />
          <div style={{ marginTop: 8 }}>
            <Text type="secondary">Analyzing release patterns...</Text>
          </div>
        </div>
      </Card>
    );
  }

  if (recommendations.length === 0) {
    return (
      <Card 
        size="small" 
        title={
          <Space>
            <BulbOutlined style={{ color: '#f59e0b' }} />
            <Text strong>AI Recommendations</Text>
          </Space>
        }
      >
        <Empty 
          description="No recommendations at this time" 
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      </Card>
    );
  }

  return (
    <Card 
      size="small" 
      title={
        <Space>
          <BulbOutlined style={{ color: '#f59e0b' }} />
          <Text strong>AI Recommendations</Text>
          <Tag color="blue">{recommendations.length}</Tag>
        </Space>
      }
      extra={
        <Button type="link" size="small" onClick={fetchRecommendations}>
          Refresh
        </Button>
      }
    >
      <List
        size="small"
        dataSource={recommendations}
        renderItem={(item) => (
          <List.Item
            style={{ 
              padding: '12px', 
              marginBottom: 8, 
              background: '#fafafa',
              borderRadius: 8,
            }}
            actions={[
              <Tooltip key="accept" title="Add to release">
                <Button
                  type="primary"
                  size="small"
                  icon={<CheckOutlined />}
                  onClick={() => handleAccept(item)}
                >
                  Add
                </Button>
              </Tooltip>,
              <Tooltip key="dismiss" title="Dismiss">
                <Button
                  type="text"
                  size="small"
                  icon={<CloseOutlined />}
                  onClick={() => handleDismiss(item.id)}
                />
              </Tooltip>,
            ]}
          >
            <List.Item.Meta
              avatar={getEntityIcon(item.recommendedEntityType)}
              title={
                <Space>
                  <Text strong>{item.recommendedEntityName}</Text>
                  <Tag color={getEntityColor(item.recommendedEntityType)}>
                    {item.recommendedEntityType}
                  </Tag>
                  <Tag color="purple">
                    {Math.round(item.recommendationScore * 100)}% confidence
                  </Tag>
                </Space>
              }
              description={
                <div>
                  {item.recommendedEntityDescription && (
                    <Paragraph 
                      type="secondary" 
                      ellipsis={{ rows: 1 }}
                      style={{ marginBottom: 4 }}
                    >
                      {item.recommendedEntityDescription}
                    </Paragraph>
                  )}
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {item.recommendationReason}
                  </Text>
                </div>
              }
            />
          </List.Item>
        )}
      />
    </Card>
  );
};

export default ReleaseRecommendations;
