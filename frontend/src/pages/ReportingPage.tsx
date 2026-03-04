import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Select, Table, Progress, Statistic, Typography, Space, Tag, DatePicker, Empty } from 'antd';
import { UserOutlined, CheckCircleOutlined, ClockCircleOutlined, ExclamationCircleOutlined, ProjectOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import api from '../services/api';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
}

interface UserStats {
  userId: string;
  username: string;
  fullName: string;
  featuresOwned: number;
  featuresCompleted: number;
  featuresInProgress: number;
  microservicesOwned: number;
  incidentsResolved: number;
  hotfixesDeployed: number;
  issuesClosed: number;
  completionRate: number;
}

const ReportingPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUsers, setSelectedUsers] = useState<string[]>([]);
  const [userStats, setUserStats] = useState<UserStats[]>([]);
  const [loading, setLoading] = useState(false);
  const { isAdmin } = useAuth();

  useEffect(() => {
    loadUsers();
  }, []);

  useEffect(() => {
    if (selectedUsers.length > 0) {
      loadUserStats();
    } else {
      setUserStats([]);
    }
  }, [selectedUsers]);

  const loadUsers = async () => {
    try {
      const response = await api.get('/users/approved');
      setUsers(response.data.data);
    } catch (error) {
      console.error('Failed to load users');
    }
  };

  const loadUserStats = async () => {
    setLoading(true);
    try {
      // Fetch stats for each selected user
      const statsPromises = selectedUsers.map(async (userId) => {
        const user = users.find(u => u.id === userId);
        
        // Fetch features owned by this user
        const featuresRes = await api.get('/features', { params: { ownerId: userId, size: 1000 } });
        const features = featuresRes.data.data.content || [];
        
        const featuresOwned = features.length;
        const featuresCompleted = features.filter((f: { status: string }) => f.status === 'RELEASED').length;
        const featuresInProgress = features.filter((f: { status: string }) => f.status === 'IN_PROGRESS').length;
        
        return {
          userId,
          username: user?.username || 'Unknown',
          fullName: user?.fullName || user?.username || 'Unknown',
          featuresOwned,
          featuresCompleted,
          featuresInProgress,
          microservicesOwned: 0, // Would need backend support
          incidentsResolved: 0, // Would need backend support
          hotfixesDeployed: 0, // Would need backend support
          issuesClosed: 0, // Would need backend support
          completionRate: featuresOwned > 0 ? Math.round((featuresCompleted / featuresOwned) * 100) : 0,
        };
      });
      
      const stats = await Promise.all(statsPromises);
      setUserStats(stats);
    } catch (error) {
      console.error('Failed to load user stats');
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<UserStats> = [
    {
      title: 'User',
      key: 'user',
      render: (_, record) => (
        <Space>
          <UserOutlined />
          <div>
            <Text strong>{record.fullName || record.username}</Text>
            <br />
            <Text type="secondary" style={{ fontSize: 12 }}>{record.username}</Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Features Owned',
      dataIndex: 'featuresOwned',
      key: 'featuresOwned',
      render: (val) => <Tag color="blue">{val}</Tag>,
    },
    {
      title: 'Completed',
      dataIndex: 'featuresCompleted',
      key: 'featuresCompleted',
      render: (val) => <Tag color="green">{val}</Tag>,
    },
    {
      title: 'In Progress',
      dataIndex: 'featuresInProgress',
      key: 'featuresInProgress',
      render: (val) => <Tag color="orange">{val}</Tag>,
    },
    {
      title: 'Completion Rate',
      dataIndex: 'completionRate',
      key: 'completionRate',
      render: (val) => (
        <Progress 
          percent={val} 
          size="small" 
          status={val >= 80 ? 'success' : val >= 50 ? 'normal' : 'exception'}
        />
      ),
    },
  ];

  const totalStats = userStats.reduce((acc, stat) => ({
    featuresOwned: acc.featuresOwned + stat.featuresOwned,
    featuresCompleted: acc.featuresCompleted + stat.featuresCompleted,
    featuresInProgress: acc.featuresInProgress + stat.featuresInProgress,
  }), { featuresOwned: 0, featuresCompleted: 0, featuresInProgress: 0 });

  const overallCompletionRate = totalStats.featuresOwned > 0 
    ? Math.round((totalStats.featuresCompleted / totalStats.featuresOwned) * 100) 
    : 0;

  if (!isAdmin) {
    return (
      <Card>
        <Title level={4}>Access Denied</Title>
        <Text>You don't have permission to access this page.</Text>
      </Card>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>User Performance Reporting</Title>
        <Text type="secondary">Analyze individual or multiple user performance metrics</Text>
      </div>

      <Card style={{ marginBottom: 24 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Text strong style={{ marginRight: 12 }}>Select Users to Analyze:</Text>
            <Select
              mode="multiple"
              placeholder="Select one or more users"
              style={{ minWidth: 400 }}
              value={selectedUsers}
              onChange={setSelectedUsers}
              optionFilterProp="children"
              allowClear
            >
              {users.map(u => (
                <Option key={u.id} value={u.id}>
                  {u.fullName || u.username} ({u.email})
                </Option>
              ))}
            </Select>
          </Col>
          <Col>
            <RangePicker 
              placeholder={['Start Date', 'End Date']}
              style={{ marginLeft: 16 }}
            />
          </Col>
        </Row>
      </Card>

      {selectedUsers.length > 0 ? (
        <>
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Total Features Owned"
                  value={totalStats.featuresOwned}
                  prefix={<ProjectOutlined style={{ color: '#1890ff' }} />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Features Completed"
                  value={totalStats.featuresCompleted}
                  prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Features In Progress"
                  value={totalStats.featuresInProgress}
                  prefix={<ClockCircleOutlined style={{ color: '#fa8c16' }} />}
                  valueStyle={{ color: '#fa8c16' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic
                  title="Overall Completion Rate"
                  value={overallCompletionRate}
                  suffix="%"
                  prefix={<ExclamationCircleOutlined style={{ color: overallCompletionRate >= 70 ? '#52c41a' : '#fa8c16' }} />}
                  valueStyle={{ color: overallCompletionRate >= 70 ? '#52c41a' : '#fa8c16' }}
                />
              </Card>
            </Col>
          </Row>

          <Card title="User Performance Details">
            <Table
              columns={columns}
              dataSource={userStats}
              rowKey="userId"
              loading={loading}
              pagination={false}
            />
          </Card>
        </>
      ) : (
        <Card>
          <Empty 
            description="Select one or more users to view their performance metrics"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        </Card>
      )}
    </div>
  );
};

export default ReportingPage;
