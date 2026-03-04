import React, { useState, useEffect, useRef } from 'react';
import { Card, List, Tag, Typography, Button, Space, Alert, Spin, Progress } from 'antd';
import { WarningOutlined, LinkOutlined, CloseOutlined } from '@ant-design/icons';
import { aiService } from '../services/aiService';
import { SimilaritySuggestion } from '../types';

const { Text, Paragraph } = Typography;

interface DuplicateDetectionProps {
  entityType: 'ISSUE' | 'FEATURE';
  title: string;
  description?: string;
  onSelectSimilar?: (item: SimilaritySuggestion) => void;
}

const DuplicateDetection: React.FC<DuplicateDetectionProps> = ({
  entityType,
  title,
  description,
  onSelectSimilar,
}) => {
  const [suggestions, setSuggestions] = useState<SimilaritySuggestion[]>([]);
  const [loading, setLoading] = useState(false);
  const [dismissed, setDismissed] = useState<Set<string>>(new Set());
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  const handleViewSimilar = (item: SimilaritySuggestion) => {
    const path = entityType === 'ISSUE' ? `/issues/${item.similarEntityId}` : `/features/${item.similarEntityId}`;
    window.open(path, '_blank');
    onSelectSimilar?.(item);
  };

  useEffect(() => {
    if (!title || title.length < 5) {
      setSuggestions([]);
      return;
    }

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(async () => {
      setLoading(true);
      try {
        const results = await aiService.findSimilarItems(entityType, title, description);
        setSuggestions(results.filter(s => !dismissed.has(s.similarEntityId)));
      } catch (error) {
        console.error('Error checking for duplicates:', error);
        setSuggestions([]);
      } finally {
        setLoading(false);
      }
    }, 500);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [title, description, entityType, dismissed]);

  const handleDismiss = (id: string) => {
    setDismissed(prev => new Set([...prev, id]));
    setSuggestions(prev => prev.filter(s => s.similarEntityId !== id));
  };

  if (!title || title.length < 5) {
    return null;
  }

  if (loading) {
    return (
      <Card size="small" style={{ marginBottom: 16, background: '#fafafa' }}>
        <Space>
          <Spin size="small" />
          <Text type="secondary">Checking for similar items...</Text>
        </Space>
      </Card>
    );
  }

  if (suggestions.length === 0) {
    return null;
  }

  return (
    <Alert
      type="warning"
      showIcon
      icon={<WarningOutlined />}
      message={
        <Text strong>
          {suggestions.length} similar {entityType.toLowerCase()}(s) found
        </Text>
      }
      description={
        <div style={{ marginTop: 12 }}>
          <Paragraph type="secondary" style={{ marginBottom: 12 }}>
            These existing items might be related to what you're creating. Consider reviewing them before proceeding.
          </Paragraph>
          <List
            size="small"
            dataSource={suggestions}
            renderItem={(item) => (
              <List.Item
                style={{ 
                  background: '#fff', 
                  padding: '12px', 
                  marginBottom: 8, 
                  borderRadius: 8,
                  border: '1px solid #f0f0f0'
                }}
                actions={[
                  <Button
                    key="view"
                    type="link"
                    size="small"
                    icon={<LinkOutlined />}
                    onClick={() => handleViewSimilar(item)}
                  >
                    View
                  </Button>,
                  <Button
                    key="dismiss"
                    type="text"
                    size="small"
                    icon={<CloseOutlined />}
                    onClick={() => handleDismiss(item.similarEntityId)}
                  />,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{item.similarEntityName}</Text>
                      <Tag color="orange">
                        {Math.round(item.similarityScore * 100)}% match
                      </Tag>
                    </Space>
                  }
                  description={
                    <div>
                      {item.similarEntityDescription && (
                        <Paragraph 
                          type="secondary" 
                          ellipsis={{ rows: 2 }}
                          style={{ marginBottom: 4 }}
                        >
                          {item.similarEntityDescription}
                        </Paragraph>
                      )}
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {item.suggestionReason}
                      </Text>
                    </div>
                  }
                />
                <Progress 
                  percent={Math.round(item.similarityScore * 100)} 
                  size="small" 
                  showInfo={false}
                  strokeColor={item.similarityScore > 0.7 ? '#ff4d4f' : item.similarityScore > 0.5 ? '#faad14' : '#52c41a'}
                  style={{ width: 60 }}
                />
              </List.Item>
            )}
          />
        </div>
      }
      style={{ marginBottom: 16 }}
    />
  );
};

export default DuplicateDetection;
