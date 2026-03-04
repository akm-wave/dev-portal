import React, { useState, useEffect } from 'react';
import { Card, Timeline, Button, Typography, Space, Tag, Modal, Spin, message, Tooltip, Popconfirm } from 'antd';
import { HistoryOutlined, EyeOutlined, RollbackOutlined, DiffOutlined, UserOutlined } from '@ant-design/icons';
import { utilityVersionService } from '../services/utilityVersionService';
import { UtilityVersion } from '../types';
import dayjs from 'dayjs';

const { Text, Paragraph } = Typography;

interface VersionHistoryProps {
  utilityId: string;
  onRevert?: () => void;
}

const VersionHistory: React.FC<VersionHistoryProps> = ({ utilityId, onRevert }) => {
  const [versions, setVersions] = useState<UtilityVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedVersion, setSelectedVersion] = useState<UtilityVersion | null>(null);
  const [compareModal, setCompareModal] = useState(false);
  const [compareVersions, setCompareVersions] = useState<{ v1: number; v2: number } | null>(null);
  const [diffResult, setDiffResult] = useState<string>('');
  const [diffLoading, setDiffLoading] = useState(false);

  const fetchVersions = async () => {
    setLoading(true);
    try {
      const data = await utilityVersionService.getHistory(utilityId);
      setVersions(data);
    } catch (error) {
      console.error('Error fetching versions:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (utilityId) {
      fetchVersions();
    }
  }, [utilityId]);

  const handleRevert = async (versionNumber: number) => {
    try {
      await utilityVersionService.revertToVersion(utilityId, versionNumber);
      message.success(`Reverted to version ${versionNumber}`);
      fetchVersions();
      onRevert?.();
    } catch (error) {
      message.error('Failed to revert version');
    }
  };

  const handleCompare = async (v1: number, v2: number) => {
    setCompareVersions({ v1, v2 });
    setCompareModal(true);
    setDiffLoading(true);
    try {
      const diff = await utilityVersionService.compareVersions(utilityId, v1, v2);
      setDiffResult(diff);
    } catch (error) {
      message.error('Failed to compare versions');
    } finally {
      setDiffLoading(false);
    }
  };

  if (loading) {
    return (
      <Card size="small">
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Spin />
        </div>
      </Card>
    );
  }

  return (
    <>
      <Card
        size="small"
        title={
          <Space>
            <HistoryOutlined style={{ color: '#5b6cf0' }} />
            <Text strong>Version History</Text>
            <Tag>{versions.length} versions</Tag>
          </Space>
        }
      >
        {versions.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 24 }}>
            <Text type="secondary">No version history available</Text>
          </div>
        ) : (
          <Timeline
            items={versions.map((version, index) => ({
              color: version.isCurrent ? 'green' : 'gray',
              children: (
                <div 
                  style={{ 
                    padding: '8px 12px', 
                    background: version.isCurrent ? '#f6ffed' : '#fafafa',
                    borderRadius: 8,
                    marginBottom: 8,
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <Space>
                        <Text strong>Version {version.versionNumber}</Text>
                        {version.isCurrent && <Tag color="green">Current</Tag>}
                      </Space>
                      <div style={{ marginTop: 4 }}>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          <UserOutlined style={{ marginRight: 4 }} />
                          {version.createdBy?.username || 'Unknown'} • {dayjs(version.createdAt).format('MMM DD, YYYY HH:mm')}
                        </Text>
                      </div>
                      {version.changeSummary && (
                        <Paragraph 
                          type="secondary" 
                          style={{ fontSize: 12, marginTop: 4, marginBottom: 0 }}
                          ellipsis={{ rows: 2 }}
                        >
                          {version.changeSummary}
                        </Paragraph>
                      )}
                    </div>
                    <Space>
                      <Tooltip title="View">
                        <Button 
                          type="text" 
                          size="small" 
                          icon={<EyeOutlined />}
                          onClick={() => setSelectedVersion(version)}
                        />
                      </Tooltip>
                      {index < versions.length - 1 && (
                        <Tooltip title="Compare with previous">
                          <Button 
                            type="text" 
                            size="small" 
                            icon={<DiffOutlined />}
                            onClick={() => handleCompare(version.versionNumber, versions[index + 1].versionNumber)}
                          />
                        </Tooltip>
                      )}
                      {!version.isCurrent && (
                        <Popconfirm
                          title="Revert to this version?"
                          description="This will create a new version with the content from this version."
                          onConfirm={() => handleRevert(version.versionNumber)}
                          okText="Revert"
                          cancelText="Cancel"
                        >
                          <Tooltip title="Revert">
                            <Button 
                              type="text" 
                              size="small" 
                              icon={<RollbackOutlined />}
                              style={{ color: '#faad14' }}
                            />
                          </Tooltip>
                        </Popconfirm>
                      )}
                    </Space>
                  </div>
                </div>
              ),
            }))}
          />
        )}
      </Card>

      {/* View Version Modal */}
      <Modal
        title={`Version ${selectedVersion?.versionNumber}`}
        open={!!selectedVersion}
        onCancel={() => setSelectedVersion(null)}
        footer={null}
        width={800}
      >
        {selectedVersion && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <Text strong>Title:</Text> {selectedVersion.title}
            </div>
            {selectedVersion.description && (
              <div style={{ marginBottom: 16 }}>
                <Text strong>Description:</Text>
                <Paragraph>{selectedVersion.description}</Paragraph>
              </div>
            )}
            {selectedVersion.content && (
              <div>
                <Text strong>Content:</Text>
                <div 
                  style={{ 
                    background: '#fafafa', 
                    padding: 16, 
                    borderRadius: 8, 
                    marginTop: 8,
                    maxHeight: 400,
                    overflow: 'auto',
                  }}
                >
                  <pre style={{ whiteSpace: 'pre-wrap', margin: 0 }}>
                    {selectedVersion.content}
                  </pre>
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Compare Modal */}
      <Modal
        title={`Compare Version ${compareVersions?.v1} vs ${compareVersions?.v2}`}
        open={compareModal}
        onCancel={() => {
          setCompareModal(false);
          setDiffResult('');
        }}
        footer={null}
        width={800}
      >
        {diffLoading ? (
          <div style={{ textAlign: 'center', padding: 24 }}>
            <Spin />
          </div>
        ) : (
          <div 
            style={{ 
              background: '#fafafa', 
              padding: 16, 
              borderRadius: 8,
              maxHeight: 500,
              overflow: 'auto',
            }}
          >
            <pre style={{ whiteSpace: 'pre-wrap', margin: 0, fontFamily: 'monospace' }}>
              {diffResult}
            </pre>
          </div>
        )}
      </Modal>
    </>
  );
};

export default VersionHistory;
