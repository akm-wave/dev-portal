import React, { useEffect, useState } from 'react';
import { Table, Card, Select, Tag, Typography, Row, Col, Input, DatePicker, Space, Tooltip, Button, Modal } from 'antd';
import { AuditOutlined, SearchOutlined, ReloadOutlined, EyeOutlined } from '@ant-design/icons';
import { auditService, AuditLog } from '../services/auditService';
import { userService } from '../services/userService';
import { UserSummary } from '../types';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;

const actionColors: Record<string, string> = {
  CREATE: 'green',
  UPDATE: 'blue',
  DELETE: 'red',
  LOGIN: 'cyan',
  LOGOUT: 'default',
  STATUS_CHANGE: 'orange',
  UPLOAD: 'purple',
  DOWNLOAD: 'geekblue',
};

const entityTypeColors: Record<string, string> = {
  FEATURE: 'purple',
  MICROSERVICE: 'blue',
  CHECKLIST: 'cyan',
  INCIDENT: 'red',
  HOTFIX: 'orange',
  ISSUE: 'magenta',
  USER: 'green',
  DOMAIN: 'gold',
};

const AuditPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20, total: 0 });
  const [entityTypes, setEntityTypes] = useState<string[]>([]);
  const [actions, setActions] = useState<string[]>([]);
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [filters, setFilters] = useState<{ entityType?: string; action?: string; userId?: string }>({});
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);

  useEffect(() => {
    loadFilters();
  }, []);

  useEffect(() => {
    loadLogs();
  }, [pagination.current, pagination.pageSize, filters]);

  const loadFilters = async () => {
    try {
      const [entityTypesData, actionsData, usersData] = await Promise.all([
        auditService.getEntityTypes(),
        auditService.getActions(),
        userService.getApprovedUsers(),
      ]);
      setEntityTypes(entityTypesData);
      setActions(actionsData);
      setUsers(usersData);
    } catch (error) {
      console.error('Failed to load filters', error);
    }
  };

  const loadLogs = async () => {
    setLoading(true);
    try {
      const data = await auditService.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
        ...filters,
      });
      setLogs(data.content);
      setPagination(prev => ({ ...prev, total: data.totalElements }));
    } catch (error) {
      console.error('Failed to load audit logs', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: any) => {
    setPagination(prev => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
  };

  const handleFilterChange = (key: string, value: string | undefined) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const handleReset = () => {
    setFilters({});
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const showDetail = (log: AuditLog) => {
    setSelectedLog(log);
    setDetailModalVisible(true);
  };

  const columns: ColumnsType<AuditLog> = [
    {
      title: 'Timestamp',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (date: string) => (
        <Text style={{ fontSize: 12 }}>{dayjs(date).format('YYYY-MM-DD HH:mm:ss')}</Text>
      ),
    },
    {
      title: 'User',
      dataIndex: 'username',
      key: 'username',
      width: 120,
      render: (username: string) => <Tag color="blue">{username || 'System'}</Tag>,
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      width: 120,
      render: (action: string) => <Tag color={actionColors[action] || 'default'}>{action}</Tag>,
    },
    {
      title: 'Entity Type',
      dataIndex: 'entityType',
      key: 'entityType',
      width: 120,
      render: (type: string) => <Tag color={entityTypeColors[type] || 'default'}>{type}</Tag>,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (desc: string) => (
        <Tooltip title={desc}>
          <Text style={{ fontSize: 12 }}>{desc}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'IP Address',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 120,
      render: (ip: string) => <Text type="secondary" style={{ fontSize: 11 }}>{ip || '-'}</Text>,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      render: (_, record) => (
        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => showDetail(record)}>
          View
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Title level={2}>
          <AuditOutlined style={{ marginRight: 12 }} />
          Audit Logs
        </Title>
        <Text type="secondary">View all user actions and system events</Text>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Filter by Entity Type"
              allowClear
              style={{ width: '100%' }}
              value={filters.entityType}
              onChange={(val) => handleFilterChange('entityType', val)}
            >
              {entityTypes.map(type => (
                <Option key={type} value={type}>{type}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Filter by Action"
              allowClear
              style={{ width: '100%' }}
              value={filters.action}
              onChange={(val) => handleFilterChange('action', val)}
            >
              {actions.map(action => (
                <Option key={action} value={action}>{action}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Filter by User"
              allowClear
              showSearch
              optionFilterProp="children"
              style={{ width: '100%' }}
              value={filters.userId}
              onChange={(val) => handleFilterChange('userId', val)}
            >
              {users.map(user => (
                <Option key={user.id} value={user.id}>{user.username}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Space>
              <Button icon={<ReloadOutlined />} onClick={loadLogs}>Refresh</Button>
              <Button onClick={handleReset}>Reset</Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={logs}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} records`,
          }}
          onChange={handleTableChange}
          size="small"
        />
      </Card>

      <Modal
        title="Audit Log Details"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedLog && (
          <div>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Text type="secondary">Timestamp</Text>
                <div>{dayjs(selectedLog.createdAt).format('YYYY-MM-DD HH:mm:ss')}</div>
              </Col>
              <Col span={12}>
                <Text type="secondary">User</Text>
                <div><Tag color="blue">{selectedLog.username || 'System'}</Tag></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Action</Text>
                <div><Tag color={actionColors[selectedLog.action] || 'default'}>{selectedLog.action}</Tag></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Entity Type</Text>
                <div><Tag color={entityTypeColors[selectedLog.entityType] || 'default'}>{selectedLog.entityType}</Tag></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">Entity ID</Text>
                <div><Text code>{selectedLog.entityId || '-'}</Text></div>
              </Col>
              <Col span={12}>
                <Text type="secondary">IP Address</Text>
                <div>{selectedLog.ipAddress || '-'}</div>
              </Col>
              <Col span={24}>
                <Text type="secondary">Description</Text>
                <div>{selectedLog.description}</div>
              </Col>
              {selectedLog.oldValue && (
                <Col span={24}>
                  <Text type="secondary">Old Value</Text>
                  <Card size="small" style={{ background: '#fff2f0', marginTop: 4 }}>
                    <pre style={{ margin: 0, fontSize: 11, whiteSpace: 'pre-wrap' }}>{selectedLog.oldValue}</pre>
                  </Card>
                </Col>
              )}
              {selectedLog.newValue && (
                <Col span={24}>
                  <Text type="secondary">New Value</Text>
                  <Card size="small" style={{ background: '#f6ffed', marginTop: 4 }}>
                    <pre style={{ margin: 0, fontSize: 11, whiteSpace: 'pre-wrap' }}>{selectedLog.newValue}</pre>
                  </Card>
                </Col>
              )}
            </Row>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AuditPage;
