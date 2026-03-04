import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Tag,
  Table,
  Button,
  Space,
  Typography,
  Spin,
  message,
  Tabs,
  Row,
  Col,
  Popconfirm,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  RocketOutlined,
  BranchesOutlined,
  LinkOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Release, ReleaseStatus, ReleaseMicroservice, ReleaseLink, ReleaseLinkType } from '../types';
import releaseService from '../services/releaseService';
import ReleaseFormModal from '../components/ReleaseFormModal';
import SmartSummary from '../components/SmartSummary';
import { useAuth } from '../contexts/AuthContext';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const statusColors: Record<ReleaseStatus, string> = {
  DRAFT: 'default',
  SCHEDULED: 'processing',
  DEPLOYED: 'success',
  ROLLED_BACK: 'error',
};

const linkTypeColors: Record<ReleaseLinkType, string> = {
  FEATURE: 'blue',
  INCIDENT: 'red',
  HOTFIX: 'orange',
  ISSUE: 'purple',
};

const ReleaseDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [release, setRelease] = useState<Release | null>(null);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);

  const fetchRelease = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await releaseService.getById(id);
      setRelease(data);
    } catch (error) {
      message.error('Failed to load release details');
      navigate('/releases');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRelease();
  }, [id]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      await releaseService.delete(id);
      message.success('Release deleted successfully');
      navigate('/releases');
    } catch (error) {
      message.error('Failed to delete release');
    }
  };

  const handleEditSuccess = () => {
    setModalVisible(false);
    fetchRelease();
  };

  const getLinkPath = (link: ReleaseLink): string => {
    switch (link.entityType) {
      case 'FEATURE':
        return `/features/${link.entityId}`;
      case 'INCIDENT':
        return `/incidents/${link.entityId}`;
      case 'HOTFIX':
        return `/hotfixes/${link.entityId}`;
      case 'ISSUE':
        return `/issues/${link.entityId}`;
      default:
        return '#';
    }
  };

  const microserviceColumns = [
    {
      title: 'Microservice',
      dataIndex: 'microserviceName',
      key: 'microserviceName',
      render: (name: string, record: ReleaseMicroservice) => (
        <Link to={`/microservices/${record.microserviceId}`}>{name}</Link>
      ),
    },
    {
      title: 'Branch',
      dataIndex: 'branchName',
      key: 'branchName',
      render: (branch: string) => branch ? <Tag icon={<BranchesOutlined />}>{branch}</Tag> : '-',
    },
    {
      title: 'Build Number',
      dataIndex: 'buildNumber',
      key: 'buildNumber',
      render: (build: string) => build ? <Tag color="geekblue">{build}</Tag> : '-',
    },
    {
      title: 'Release Date',
      dataIndex: 'releaseDate',
      key: 'releaseDate',
      render: (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: 'Notes',
      dataIndex: 'notes',
      key: 'notes',
      ellipsis: true,
    },
  ];

  const linkColumns = [
    {
      title: 'Type',
      dataIndex: 'entityType',
      key: 'entityType',
      render: (type: ReleaseLinkType) => (
        <Tag color={linkTypeColors[type]}>{type}</Tag>
      ),
    },
    {
      title: 'Name',
      dataIndex: 'entityName',
      key: 'entityName',
      render: (name: string, record: ReleaseLink) => (
        <Link to={getLinkPath(record)}>{name}</Link>
      ),
    },
    {
      title: 'Linked At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
  ];

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!release) {
    return null;
  }

  return (
    <div>
      <Card>
        <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
          <Col>
            <Space>
              <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/releases')}>
                Back
              </Button>
              <Title level={4} style={{ margin: 0 }}>
                <RocketOutlined /> {release.name}
              </Title>
              <Tag color="blue">{release.version}</Tag>
              <Tag color={statusColors[release.status]}>{release.status.replace('_', ' ')}</Tag>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button icon={<EditOutlined />} onClick={() => setModalVisible(true)}>
                Edit
              </Button>
              {isAdmin && (
                <Popconfirm
                  title="Delete this release?"
                  description="This action cannot be undone."
                  onConfirm={handleDelete}
                  okText="Yes"
                  cancelText="No"
                >
                  <Button danger icon={<DeleteOutlined />}>
                    Delete
                  </Button>
                </Popconfirm>
              )}
            </Space>
          </Col>
        </Row>

        <Descriptions bordered column={2} style={{ marginBottom: 24 }}>
          <Descriptions.Item label="Release Name">{release.name}</Descriptions.Item>
          <Descriptions.Item label="Version">{release.version}</Descriptions.Item>
          <Descriptions.Item label="Release Date">
            {release.releaseDate ? dayjs(release.releaseDate).format('YYYY-MM-DD HH:mm') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={statusColors[release.status]}>{release.status.replace('_', ' ')}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Old Build Number">
            {release.oldBuildNumber || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Feature Branch">
            {release.featureBranch || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Created By">
            {release.createdBy?.username || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            {dayjs(release.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="Description" span={2}>
            {release.description || '-'}
          </Descriptions.Item>
        </Descriptions>

        <Row gutter={[16, 16]}>
          <Col xs={24} lg={16}>
            <Tabs
              defaultActiveKey="microservices"
              items={[
                {
                  key: 'microservices',
                  label: (
                    <span>
                      <BranchesOutlined /> Microservices ({release.microservices?.length || 0})
                    </span>
                  ),
                  children: (
                    <Table
                      columns={microserviceColumns}
                      dataSource={release.microservices || []}
                      rowKey="id"
                      pagination={false}
                    />
                  ),
                },
                {
                  key: 'links',
                  label: (
                    <span>
                      <LinkOutlined /> Linked Items ({release.links?.length || 0})
                    </span>
                  ),
                  children: (
                    <Table
                      columns={linkColumns}
                      dataSource={release.links || []}
                      rowKey="id"
                      pagination={false}
                    />
                  ),
                },
              ]}
            />
          </Col>
          <Col xs={24} lg={8}>
            <SmartSummary
              entityType="RELEASE"
              entityId={id!}
              summaryType="RELEASE_NOTES"
              title="AI Release Notes"
            />
          </Col>
        </Row>
      </Card>

      <ReleaseFormModal
        visible={modalVisible}
        release={release}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleEditSuccess}
      />
    </div>
  );
};

export default ReleaseDetailsPage;
