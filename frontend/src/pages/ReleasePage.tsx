import React, { useState, useEffect, useCallback } from 'react';
import { 
  Card, 
  Table, 
  Button, 
  Space, 
  Tag, 
  Input, 
  Select, 
  message, 
  Popconfirm,
  Typography,
  Row,
  Col
} from 'antd';
import { 
  PlusOutlined, 
  SearchOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  EyeOutlined,
  RocketOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { Release, ReleaseStatus } from '../types';
import releaseService from '../services/releaseService';
import ReleaseFormModal from '../components/ReleaseFormModal';
import { useAuth } from '../contexts/AuthContext';
import dayjs from 'dayjs';

const { Title } = Typography;
const { Option } = Select;

const statusColors: Record<ReleaseStatus, string> = {
  DRAFT: 'default',
  SCHEDULED: 'processing',
  DEPLOYED: 'success',
  ROLLED_BACK: 'error',
};

const ReleasePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [releases, setReleases] = useState<Release[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRelease, setEditingRelease] = useState<Release | null>(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<ReleaseStatus | undefined>();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const fetchReleases = useCallback(async () => {
    setLoading(true);
    try {
      const response = await releaseService.getAll(
        pagination.current - 1,
        pagination.pageSize,
        'createdAt',
        'desc',
        statusFilter,
        search || undefined
      );
      setReleases(response.content);
      setPagination(prev => ({ ...prev, total: response.totalElements }));
    } catch (error) {
      message.error('Failed to load releases');
    } finally {
      setLoading(false);
    }
  }, [pagination.current, pagination.pageSize, statusFilter, search]);

  useEffect(() => {
    fetchReleases();
  }, [fetchReleases]);

  const handleCreate = () => {
    setEditingRelease(null);
    setModalVisible(true);
  };

  const handleEdit = (release: Release) => {
    setEditingRelease(release);
    setModalVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await releaseService.delete(id);
      message.success('Release deleted successfully');
      fetchReleases();
    } catch (error) {
      message.error('Failed to delete release');
    }
  };

  const handleModalClose = () => {
    setModalVisible(false);
    setEditingRelease(null);
  };

  const handleModalSuccess = () => {
    handleModalClose();
    fetchReleases();
  };

  const columns = [
    {
      title: 'Release Name',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: Release) => (
        <a onClick={() => navigate(`/releases/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: 'Version',
      dataIndex: 'version',
      key: 'version',
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Release Date',
      dataIndex: 'releaseDate',
      key: 'releaseDate',
      render: (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: ReleaseStatus) => (
        <Tag color={statusColors[status]}>{status.replace('_', ' ')}</Tag>
      ),
    },
    {
      title: 'Created By',
      dataIndex: 'createdBy',
      key: 'createdBy',
      render: (createdBy: Release['createdBy']) => createdBy?.username || '-',
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Release) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/releases/${record.id}`)}
          />
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          {isAdmin && (
            <Popconfirm
              title="Delete this release?"
              description="This action cannot be undone."
              onConfirm={() => handleDelete(record.id)}
              okText="Yes"
              cancelText="No"
            >
              <Button type="link" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <Col>
            <Title level={4} style={{ margin: 0 }}>
              <RocketOutlined /> Release Management
            </Title>
          </Col>
          <Col>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              Create Release
            </Button>
          </Col>
        </Row>

        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Input
              placeholder="Search by name or version..."
              prefix={<SearchOutlined />}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onPressEnter={() => fetchReleases()}
              allowClear
            />
          </Col>
          <Col span={6}>
            <Select
              placeholder="Filter by status"
              style={{ width: '100%' }}
              allowClear
              value={statusFilter}
              onChange={(value) => setStatusFilter(value)}
            >
              <Option value="DRAFT">Draft</Option>
              <Option value="SCHEDULED">Scheduled</Option>
              <Option value="DEPLOYED">Deployed</Option>
              <Option value="ROLLED_BACK">Rolled Back</Option>
            </Select>
          </Col>
          <Col>
            <Button onClick={() => fetchReleases()}>Search</Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={releases}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} releases`,
          }}
          onChange={(pag) => setPagination(prev => ({ 
            ...prev, 
            current: pag.current || 1, 
            pageSize: pag.pageSize || 10 
          }))}
        />
      </Card>

      <ReleaseFormModal
        visible={modalVisible}
        release={editingRelease}
        onCancel={handleModalClose}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
};

export default ReleasePage;
