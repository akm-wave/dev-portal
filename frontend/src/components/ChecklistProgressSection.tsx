import React, { useState } from 'react';
import { Card, Table, Tag, Select, Input, Button, Space, Upload, message, Modal, Progress, Popconfirm, Typography } from 'antd';
import { UploadOutlined, DownloadOutlined, DeleteOutlined, PaperClipOutlined, EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { ChecklistProgress, ChecklistStatus, ChecklistProgressUpdateRequest } from '../types';
import type { ColumnsType } from 'antd/es/table';
import type { UploadFile } from 'antd/es/upload/interface';

const { TextArea } = Input;
const { Text } = Typography;

interface ChecklistProgressSectionProps {
  checklistProgress: ChecklistProgress[];
  loading: boolean;
  onUpdateStatus: (checklistId: string, data: ChecklistProgressUpdateRequest) => Promise<void>;
  onUploadAttachment: (checklistId: string, file: File) => Promise<void>;
  onDownloadAttachment: (checklistId: string, filename: string) => Promise<void>;
  onDeleteAttachment: (checklistId: string) => Promise<void>;
}

const statusColors: Record<ChecklistStatus, string> = {
  PENDING: 'default',
  IN_PROGRESS: 'processing',
  DONE: 'success',
  BLOCKED: 'error',
};

const ChecklistProgressSection: React.FC<ChecklistProgressSectionProps> = ({
  checklistProgress,
  loading,
  onUpdateStatus,
  onUploadAttachment,
  onDownloadAttachment,
  onDeleteAttachment,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editStatus, setEditStatus] = useState<ChecklistStatus>('PENDING');
  const [editRemark, setEditRemark] = useState('');
  const [saving, setSaving] = useState(false);

  const completedCount = checklistProgress.filter(p => p.status === 'DONE').length;
  const totalCount = checklistProgress.length;
  const progressPercent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

  const handleEdit = (record: ChecklistProgress) => {
    setEditingId(record.checklistId);
    setEditStatus(record.status);
    setEditRemark(record.remark || '');
  };

  const handleSave = async (checklistId: string) => {
    setSaving(true);
    try {
      await onUpdateStatus(checklistId, { status: editStatus, remark: editRemark });
      setEditingId(null);
      message.success('Status updated successfully');
    } catch (error) {
      message.error('Failed to update status');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditRemark('');
  };

  const handleUpload = async (checklistId: string, file: UploadFile) => {
    if (file.originFileObj) {
      try {
        await onUploadAttachment(checklistId, file.originFileObj);
        message.success('Attachment uploaded successfully');
      } catch (error) {
        message.error('Failed to upload attachment');
      }
    }
    return false;
  };

  const columns: ColumnsType<ChecklistProgress> = [
    {
      title: 'Checklist',
      dataIndex: 'checklistName',
      key: 'checklistName',
      render: (name: string, record: ChecklistProgress) => (
        <div>
          <Text strong>{name}</Text>
          {record.checklistDescription && (
            <div><Text type="secondary" style={{ fontSize: 12 }}>{record.checklistDescription}</Text></div>
          )}
        </div>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 150,
      render: (status: ChecklistStatus, record: ChecklistProgress) => {
        if (editingId === record.checklistId) {
          return (
            <Select
              value={editStatus}
              onChange={setEditStatus}
              style={{ width: 130 }}
              size="small"
            >
              <Select.Option value="PENDING">Pending</Select.Option>
              <Select.Option value="IN_PROGRESS">In Progress</Select.Option>
              <Select.Option value="DONE">Done</Select.Option>
              <Select.Option value="BLOCKED">Blocked</Select.Option>
            </Select>
          );
        }
        return <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>;
      },
    },
    {
      title: 'Remark',
      dataIndex: 'remark',
      key: 'remark',
      width: 200,
      render: (remark: string, record: ChecklistProgress) => {
        if (editingId === record.checklistId) {
          return (
            <TextArea
              value={editRemark}
              onChange={(e) => setEditRemark(e.target.value)}
              rows={2}
              placeholder="Add remark..."
              style={{ width: '100%' }}
            />
          );
        }
        return remark || '-';
      },
    },
    {
      title: 'Attachment',
      key: 'attachment',
      width: 180,
      render: (_, record: ChecklistProgress) => (
        <Space>
          {record.mongoFileId ? (
            <>
              <Button
                type="link"
                size="small"
                icon={<DownloadOutlined />}
                onClick={() => onDownloadAttachment(record.checklistId, record.attachmentFilename)}
              >
                {record.attachmentFilename?.substring(0, 15)}...
              </Button>
              <Popconfirm
                title="Delete attachment?"
                onConfirm={() => onDeleteAttachment(record.checklistId)}
              >
                <Button type="link" size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </>
          ) : (
            <Upload
              beforeUpload={(file) => {
                handleUpload(record.checklistId, { originFileObj: file } as UploadFile);
                return false;
              }}
              showUploadList={false}
            >
              <Button type="link" size="small" icon={<UploadOutlined />}>
                Upload
              </Button>
            </Upload>
          )}
        </Space>
      ),
    },
    {
      title: 'Updated By',
      dataIndex: 'updatedBy',
      key: 'updatedBy',
      width: 120,
      render: (updatedBy: string) => updatedBy || '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 100,
      render: (_, record: ChecklistProgress) => {
        if (editingId === record.checklistId) {
          return (
            <Space>
              <Button
                type="primary"
                size="small"
                icon={<CheckOutlined />}
                loading={saving}
                onClick={() => handleSave(record.checklistId)}
              />
              <Button
                size="small"
                icon={<CloseOutlined />}
                onClick={handleCancel}
              />
            </Space>
          );
        }
        return (
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
        );
      },
    },
  ];

  return (
    <Card 
      title={
        <Space>
          <span>Checklist Progress</span>
          <Tag color={progressPercent === 100 ? 'success' : 'processing'}>
            {completedCount}/{totalCount}
          </Tag>
        </Space>
      }
      extra={<Progress percent={progressPercent} size="small" style={{ width: 150 }} />}
    >
      <Table
        columns={columns}
        dataSource={checklistProgress}
        rowKey="checklistId"
        loading={loading}
        pagination={false}
        size="small"
      />
    </Card>
  );
};

export default ChecklistProgressSection;
