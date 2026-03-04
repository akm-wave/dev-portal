import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Space, Spin, message, Tag, Tooltip, Divider } from 'antd';
import { RobotOutlined, CheckOutlined, ReloadOutlined, CopyOutlined } from '@ant-design/icons';
import { aiService } from '../services/aiService';
import { AiSummary, SummaryType } from '../types';

const { Text, Paragraph } = Typography;

interface SmartSummaryProps {
  entityType: string;
  entityId: string;
  summaryType: SummaryType;
  title?: string;
}

const SmartSummary: React.FC<SmartSummaryProps> = ({
  entityType,
  entityId,
  summaryType,
  title = 'AI Summary',
}) => {
  const [summary, setSummary] = useState<AiSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  const fetchExistingSummary = async () => {
    setLoading(true);
    try {
      const summaries = await aiService.getSummaries(entityType, entityId);
      const existing = summaries.find(s => s.summaryType === summaryType);
      setSummary(existing || null);
    } catch (error) {
      console.error('Error fetching summary:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (entityId) {
      fetchExistingSummary();
    }
  }, [entityId, entityType, summaryType]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      const newSummary = await aiService.generateSummary(entityType, entityId, summaryType);
      setSummary(newSummary);
      message.success('Summary generated successfully');
    } catch (error) {
      message.error('Failed to generate summary');
    } finally {
      setGenerating(false);
    }
  };

  const handleApprove = async () => {
    if (!summary) return;
    try {
      const approved = await aiService.approveSummary(summary.id);
      setSummary(approved);
      message.success('Summary approved');
    } catch (error) {
      message.error('Failed to approve summary');
    }
  };

  const handleCopy = () => {
    if (summary?.summaryText) {
      navigator.clipboard.writeText(summary.summaryText);
      message.success('Copied to clipboard');
    }
  };

  if (loading) {
    return (
      <Card size="small">
        <div style={{ textAlign: 'center', padding: 16 }}>
          <Spin size="small" />
        </div>
      </Card>
    );
  }

  return (
    <Card 
      size="small"
      title={
        <Space>
          <RobotOutlined style={{ color: '#8b5cf6' }} />
          <Text strong>{title}</Text>
          {summary?.isApproved && <Tag color="green">Approved</Tag>}
        </Space>
      }
      extra={
        <Space>
          {summary && (
            <>
              <Tooltip title="Copy">
                <Button type="text" size="small" icon={<CopyOutlined />} onClick={handleCopy} />
              </Tooltip>
              {!summary.isApproved && (
                <Tooltip title="Approve">
                  <Button 
                    type="text" 
                    size="small" 
                    icon={<CheckOutlined />} 
                    onClick={handleApprove}
                    style={{ color: '#10b981' }}
                  />
                </Tooltip>
              )}
            </>
          )}
          <Tooltip title={summary ? 'Regenerate' : 'Generate'}>
            <Button 
              type="primary" 
              size="small" 
              icon={<ReloadOutlined spin={generating} />}
              onClick={handleGenerate}
              loading={generating}
            >
              {summary ? 'Regenerate' : 'Generate'}
            </Button>
          </Tooltip>
        </Space>
      }
    >
      {summary ? (
        <div>
          <div 
            style={{ 
              background: '#fafafa', 
              padding: 16, 
              borderRadius: 8,
              maxHeight: 300,
              overflow: 'auto',
            }}
          >
            <pre style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', margin: 0 }}>
              {summary.summaryText}
            </pre>
          </div>
          <Divider style={{ margin: '12px 0' }} />
          <Space split={<Divider type="vertical" />}>
            <Text type="secondary" style={{ fontSize: 12 }}>
              Generated: {new Date(summary.generatedAt).toLocaleString()}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              By: {summary.generatedBy}
            </Text>
            {summary.approvedBy && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                Approved by: {summary.approvedBy.username}
              </Text>
            )}
          </Space>
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: 24 }}>
          <RobotOutlined style={{ fontSize: 32, color: '#d9d9d9', marginBottom: 12 }} />
          <Paragraph type="secondary">
            No summary available. Click "Generate" to create an AI-powered summary.
          </Paragraph>
        </div>
      )}
    </Card>
  );
};

export default SmartSummary;
