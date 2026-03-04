import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Progress, List, Tag, Typography, Spin, Badge, Divider } from 'antd';
import {
  AppstoreOutlined,
  ApiOutlined,
  ClockCircleOutlined,
  WarningOutlined,
  BugOutlined,
  RiseOutlined,
  ThunderboltOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  RocketOutlined,
  FireOutlined,
  SafetyCertificateOutlined,
  LineChartOutlined,
} from '@ant-design/icons';
import { dashboardService } from '../services/dashboardService';
import { DashboardStats } from '../types';
import MyOverviewWidget from '../components/MyOverviewWidget';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const { Title, Text } = Typography;

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const data = await dashboardService.getStats();
      setStats(data);
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!stats) {
    return <div>Failed to load dashboard data</div>;
  }

  const statusColors: Record<string, string> = {
    PLANNED: 'blue',
    IN_PROGRESS: 'orange',
    RELEASED: 'green',
    NOT_STARTED: 'default',
    COMPLETED: 'green',
    PENDING: 'default',
    DONE: 'green',
    BLOCKED: 'red',
  };

  // Calculate derived metrics
  const featuresInProgress = stats.featuresByStatus['IN_PROGRESS'] || 0;
  const featuresCompleted = stats.featuresByStatus['RELEASED'] || 0;
  const checklistsDone = stats.checklistsByStatus['DONE'] || 0;
  const checklistsBlocked = stats.checklistsByStatus['BLOCKED'] || 0;
  const checklistsPending = stats.checklistsByStatus['PENDING'] || 0;
  const checklistsInProgress = stats.checklistsByStatus['IN_PROGRESS'] || 0;

  const completionRate = stats.totalFeatures > 0 
    ? Math.round((featuresCompleted / stats.totalFeatures) * 100) 
    : 0;

  return (
    <div style={{ background: '#f8fafc', margin: -24, padding: 24 }}>
      {/* Header Section */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: 28,
        padding: '20px 24px',
        background: 'linear-gradient(135deg, #5b6cf0 0%, #8b5cf6 100%)',
        borderRadius: 16,
        boxShadow: '0 4px 20px rgba(91, 108, 240, 0.25)',
      }}>
        <div>
          <Title level={3} style={{ margin: 0, color: '#fff', fontWeight: 600 }}>
            <RocketOutlined style={{ marginRight: 12 }} />
            Dashboard Overview
          </Title>
          <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: 13 }}>
            Welcome back! Here's what's happening with your projects.
          </Text>
        </div>
        <div style={{ 
          background: 'rgba(255,255,255,0.15)', 
          padding: '8px 16px', 
          borderRadius: 8,
          backdropFilter: 'blur(10px)',
        }}>
          <Text style={{ color: '#fff', fontSize: 12 }}>
            <ClockCircleOutlined style={{ marginRight: 6 }} />
            {dayjs().format('ddd, MMM DD • HH:mm')}
          </Text>
        </div>
      </div>
      
      {/* Quick Stats Cards */}
      <Row gutter={[20, 20]}>
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              border: 'none', 
              borderRadius: 14,
              boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
            }}
            styles={{ body: { padding: '20px 24px' } }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
              <div>
                <Text type="secondary" style={{ fontSize: 13, fontWeight: 500 }}>Total Features</Text>
                <div style={{ fontSize: 32, fontWeight: 700, color: '#1e293b', marginTop: 4 }}>
                  {stats.totalFeatures}
                </div>
                <div style={{ marginTop: 8 }}>
                  <Tag color="blue" style={{ borderRadius: 6 }}>{featuresInProgress} active</Tag>
                  <Tag color="green" style={{ borderRadius: 6 }}>{featuresCompleted} done</Tag>
                </div>
              </div>
              <div style={{ 
                width: 48, height: 48, borderRadius: 12, 
                background: 'linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <AppstoreOutlined style={{ fontSize: 22, color: '#5b6cf0' }} />
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              border: 'none', 
              borderRadius: 14,
              boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
            }}
            styles={{ body: { padding: '20px 24px' } }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
              <div>
                <Text type="secondary" style={{ fontSize: 13, fontWeight: 500 }}>Microservices</Text>
                <div style={{ fontSize: 32, fontWeight: 700, color: '#1e293b', marginTop: 4 }}>
                  {stats.totalMicroservices}
                </div>
                <div style={{ marginTop: 8 }}>
                  <Tag color="cyan" style={{ borderRadius: 6 }}>{stats.microservicesByStatus['IN_PROGRESS'] || 0} in progress</Tag>
                </div>
              </div>
              <div style={{ 
                width: 48, height: 48, borderRadius: 12, 
                background: 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <ApiOutlined style={{ fontSize: 22, color: '#10b981' }} />
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              border: 'none', 
              borderRadius: 14,
              boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
            }}
            styles={{ body: { padding: '20px 24px' } }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
              <div>
                <Text type="secondary" style={{ fontSize: 13, fontWeight: 500 }}>Completion Rate</Text>
                <div style={{ fontSize: 32, fontWeight: 700, color: '#1e293b', marginTop: 4 }}>
                  {completionRate}%
                </div>
                <Progress 
                  percent={completionRate} 
                  showInfo={false} 
                  strokeColor={{ from: '#5b6cf0', to: '#8b5cf6' }}
                  style={{ marginTop: 8, width: 120 }}
                  size="small"
                />
              </div>
              <div style={{ 
                width: 48, height: 48, borderRadius: 12, 
                background: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <RiseOutlined style={{ fontSize: 22, color: '#f59e0b' }} />
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card 
            style={{ 
              border: 'none', 
              borderRadius: 14,
              boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
              background: checklistsBlocked > 0 ? '#fef2f2' : '#f0fdf4',
            }}
            styles={{ body: { padding: '20px 24px' } }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
              <div>
                <Text type="secondary" style={{ fontSize: 13, fontWeight: 500 }}>Blocked Items</Text>
                <div style={{ fontSize: 32, fontWeight: 700, color: checklistsBlocked > 0 ? '#ef4444' : '#10b981', marginTop: 4 }}>
                  {checklistsBlocked}
                </div>
                <div style={{ marginTop: 8 }}>
                  {checklistsBlocked > 0 ? (
                    <Tag color="red" style={{ borderRadius: 6 }}>Needs attention</Tag>
                  ) : (
                    <Tag color="green" style={{ borderRadius: 6 }}>All clear</Tag>
                  )}
                </div>
              </div>
              <div style={{ 
                width: 48, height: 48, borderRadius: 12, 
                background: checklistsBlocked > 0 
                  ? 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)'
                  : 'linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                {checklistsBlocked > 0 ? (
                  <ExclamationCircleOutlined style={{ fontSize: 22, color: '#ef4444' }} />
                ) : (
                  <SafetyCertificateOutlined style={{ fontSize: 22, color: '#10b981' }} />
                )}
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* Progress Overview */}
      <Row gutter={[20, 20]} style={{ marginTop: 20 }}>
        <Col xs={24} lg={16}>
          <Card 
            title={
              <span style={{ fontWeight: 600, color: '#1e293b' }}>
                <LineChartOutlined style={{ marginRight: 8, color: '#5b6cf0' }} />
                Project Progress
              </span>
            }
            style={{ border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
          >
            <Row gutter={24}>
              <Col span={8}>
                <div style={{ textAlign: 'center' }}>
                  <Progress 
                    type="dashboard" 
                    percent={stats.overallProgress} 
                    strokeColor={{ '0%': '#5b6cf0', '100%': '#10b981' }}
                    strokeWidth={10}
                    format={(percent) => (
                      <div>
                        <div style={{ fontSize: 28, fontWeight: 700, color: '#1e293b' }}>{percent}%</div>
                        <div style={{ fontSize: 12, color: '#64748b' }}>Overall</div>
                      </div>
                    )}
                  />
                </div>
              </Col>
              <Col span={16}>
                <div style={{ padding: '8px 0' }}>
                  <div style={{ marginBottom: 20 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                      <Text style={{ color: '#64748b' }}><CheckCircleOutlined style={{ color: '#10b981', marginRight: 8 }} />Completed</Text>
                      <Text strong style={{ color: '#1e293b' }}>{checklistsDone}</Text>
                    </div>
                    <Progress percent={stats.totalChecklists > 0 ? Math.round((checklistsDone / stats.totalChecklists) * 100) : 0} 
                      strokeColor="#10b981" showInfo={false} size="small" trailColor="#e2e8f0" />
                  </div>
                  <div style={{ marginBottom: 20 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                      <Text style={{ color: '#64748b' }}><SyncOutlined spin style={{ color: '#5b6cf0', marginRight: 8 }} />In Progress</Text>
                      <Text strong style={{ color: '#1e293b' }}>{checklistsInProgress}</Text>
                    </div>
                    <Progress percent={stats.totalChecklists > 0 ? Math.round((checklistsInProgress / stats.totalChecklists) * 100) : 0} 
                      strokeColor="#5b6cf0" showInfo={false} size="small" trailColor="#e2e8f0" />
                  </div>
                  <div style={{ marginBottom: 20 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                      <Text style={{ color: '#64748b' }}><ClockCircleOutlined style={{ color: '#f59e0b', marginRight: 8 }} />Pending</Text>
                      <Text strong style={{ color: '#1e293b' }}>{checklistsPending}</Text>
                    </div>
                    <Progress percent={stats.totalChecklists > 0 ? Math.round((checklistsPending / stats.totalChecklists) * 100) : 0} 
                      strokeColor="#f59e0b" showInfo={false} size="small" trailColor="#e2e8f0" />
                  </div>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                      <Text style={{ color: '#64748b' }}><ExclamationCircleOutlined style={{ color: '#ef4444', marginRight: 8 }} />Blocked</Text>
                      <Text strong style={{ color: checklistsBlocked > 0 ? '#ef4444' : '#1e293b' }}>{checklistsBlocked}</Text>
                    </div>
                    <Progress percent={stats.totalChecklists > 0 ? Math.round((checklistsBlocked / stats.totalChecklists) * 100) : 0} 
                      strokeColor="#ef4444" showInfo={false} size="small" trailColor="#e2e8f0" />
                  </div>
                </div>
              </Col>
            </Row>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card 
            title={
              <span style={{ fontWeight: 600, color: '#1e293b' }}>
                <FireOutlined style={{ marginRight: 8, color: '#f59e0b' }} />
                Feature Status
              </span>
            }
            style={{ height: '100%', border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
          >
            {Object.entries(stats.featuresByStatus).map(([status, count]) => {
              const percent = stats.totalFeatures > 0 ? Math.round((count / stats.totalFeatures) * 100) : 0;
              return (
                <div key={status} style={{ marginBottom: 16 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                    <Tag color={statusColors[status]} style={{ borderRadius: 6 }}>{status.replace('_', ' ')}</Tag>
                    <Text strong style={{ color: '#1e293b' }}>{count} ({percent}%)</Text>
                  </div>
                  <Progress percent={percent} showInfo={false} size="small" trailColor="#e2e8f0"
                    strokeColor={status === 'RELEASED' ? '#10b981' : status === 'IN_PROGRESS' ? '#f59e0b' : '#5b6cf0'} />
                </div>
              );
            })}
          </Card>
        </Col>
      </Row>

      {/* Risk & Activity Section */}
      <Row gutter={[20, 20]} style={{ marginTop: 20 }}>
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span style={{ fontWeight: 600, color: '#1e293b' }}>
                <WarningOutlined style={{ color: '#ef4444', marginRight: 8 }} />
                High Impact Services
                <Badge count={stats.highImpactServices?.length || 0} style={{ marginLeft: 8, backgroundColor: '#ef4444' }} />
              </span>
            }
            style={{ height: 340, border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
            styles={{ body: { height: 'calc(100% - 57px)', overflow: 'auto' } }}
          >
            {stats.highImpactServices && stats.highImpactServices.length > 0 ? (
              <List
                size="small"
                dataSource={stats.highImpactServices}
                renderItem={(service) => (
                  <List.Item style={{ padding: '12px 0', borderBottom: '1px solid #f1f5f9' }}>
                    <List.Item.Meta
                      title={
                        <span style={{ fontWeight: 500, color: '#1e293b' }}>
                          {service.name}
                          <Tag color="red" style={{ marginLeft: 8, fontSize: 10, borderRadius: 6 }}>HIGH IMPACT</Tag>
                        </span>
                      }
                      description={
                        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginTop: 4 }}>
                          <Text style={{ fontSize: 12, color: '#64748b' }}>
                            {service.featureCount} features
                          </Text>
                          <Progress 
                            percent={service.progressPercentage} 
                            size="small" 
                            style={{ width: 100 }}
                            strokeColor={{ from: '#5b6cf0', to: '#10b981' }}
                            trailColor="#e2e8f0"
                          />
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: 50 }}>
                <CheckCircleOutlined style={{ fontSize: 48, color: '#10b981' }} />
                <div style={{ marginTop: 16 }}>
                  <Text style={{ color: '#64748b' }}>No high impact services requiring attention</Text>
                </div>
              </div>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card 
            title={
              <span style={{ fontWeight: 600, color: '#1e293b' }}>
                <BugOutlined style={{ color: '#f59e0b', marginRight: 8 }} />
                Technical Debt Analysis
              </span>
            }
            style={{ height: 340, border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
            styles={{ body: { height: 'calc(100% - 57px)', overflow: 'auto' } }}
          >
            <div style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <div style={{ background: '#fef3c7', padding: '12px 16px', borderRadius: 10 }}>
                    <Text style={{ fontSize: 12, color: '#92400e' }}>Total Tech Debt</Text>
                    <div style={{ fontSize: 24, fontWeight: 700, color: '#f59e0b' }}>{stats.totalTechDebtIssues || 0}</div>
                  </div>
                </Col>
                <Col span={12}>
                  <div style={{ background: stats.openTechDebtIssues && stats.openTechDebtIssues > 0 ? '#fef2f2' : '#f0fdf4', padding: '12px 16px', borderRadius: 10 }}>
                    <Text style={{ fontSize: 12, color: stats.openTechDebtIssues && stats.openTechDebtIssues > 0 ? '#991b1b' : '#166534' }}>Open Issues</Text>
                    <div style={{ fontSize: 24, fontWeight: 700, color: stats.openTechDebtIssues && stats.openTechDebtIssues > 0 ? '#ef4444' : '#10b981' }}>{stats.openTechDebtIssues || 0}</div>
                  </div>
                </Col>
              </Row>
            </div>
            <Divider style={{ margin: '16px 0', borderColor: '#f1f5f9' }} />
            <Text strong style={{ display: 'block', marginBottom: 10, color: '#1e293b', fontSize: 13 }}>Issues by Category</Text>
            {stats.issuesByCategory && Object.keys(stats.issuesByCategory).length > 0 ? (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {Object.entries(stats.issuesByCategory).map(([category, count]) => (
                  <Tag 
                    key={category} 
                    color={category === 'TECH_DEBT' ? 'volcano' : category === 'PROD_ISSUE' ? 'red' : category === 'BUG' ? 'magenta' : 'default'}
                    style={{ fontSize: 11, borderRadius: 6, padding: '2px 10px' }}
                  >
                    {category.replace('_', ' ')}: {count}
                  </Tag>
                ))}
              </div>
            ) : (
              <Text style={{ color: '#64748b', fontSize: 13 }}>No issues categorized yet</Text>
            )}
          </Card>
        </Col>
      </Row>

      {/* Recent Activity & My Workspace */}
      <Row gutter={[20, 20]} style={{ marginTop: 20 }}>
        <Col xs={24} lg={16}>
          <Card 
            title={
              <span style={{ fontWeight: 600, color: '#1e293b' }}>
                <ThunderboltOutlined style={{ color: '#8b5cf6', marginRight: 8 }} />
                Recent Activity
              </span>
            }
            style={{ border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
          >
            <List
              size="small"
              dataSource={stats.recentActivities?.slice(0, 5) || []}
              renderItem={(activity) => (
                <List.Item style={{ padding: '14px 0', borderBottom: '1px solid #f1f5f9' }}>
                  <List.Item.Meta
                    avatar={
                      <div style={{ 
                        width: 36, height: 36, borderRadius: 10, 
                        background: 'linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                      }}>
                        <ThunderboltOutlined style={{ color: '#8b5cf6', fontSize: 16 }} />
                      </div>
                    }
                    title={<span style={{ fontWeight: 500, color: '#1e293b' }}>{activity.description}</span>}
                    description={
                      <div style={{ marginTop: 4 }}>
                        <Tag color="purple" style={{ fontSize: 10, borderRadius: 6 }}>{activity.action}</Tag>
                        <Text style={{ fontSize: 12, color: '#94a3b8', marginLeft: 8 }}>{dayjs(activity.createdAt).fromNow()}</Text>
                      </div>
                    }
                  />
                </List.Item>
              )}
              locale={{ emptyText: <Text style={{ color: '#94a3b8' }}>No recent activity</Text> }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <MyOverviewWidget />
        </Col>
      </Row>
    </div>
  );
};

export default DashboardPage;
