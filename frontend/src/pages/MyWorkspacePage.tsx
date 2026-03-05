import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Tabs, 
  Button, 
  Input, 
  List, 
  Tag, 
  Space, 
  Modal, 
  Form, 
  Select, 
  DatePicker, 
  message, 
  Popconfirm, 
  Badge, 
  Typography, 
  Empty,
  Spin,
  Tooltip,
  Row,
  Col,
  Statistic
} from 'antd';
import { 
  PlusOutlined, 
  PushpinOutlined, 
  PushpinFilled, 
  DeleteOutlined, 
  EditOutlined, 
  SearchOutlined,
  BellOutlined,
  CheckOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  InboxOutlined,
  LinkOutlined,
  BarChartOutlined,
  TrophyOutlined,
  FireOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { noteService, reminderService, productivityService } from '../services/workspaceService';
import { 
  UserNote, 
  UserNoteRequest, 
  UserReminder, 
  UserReminderRequest, 
  ReminderPriority, 
  ReminderStatus,
  ModuleType,
  ReminderCounts,
  WorkspaceProductivityDTO
} from '../types';

dayjs.extend(relativeTime);

const { TextArea } = Input;
const { Text, Paragraph } = Typography;

const MODULE_TYPES: { value: ModuleType; label: string }[] = [
  { value: 'ISSUE', label: 'Issue' },
  { value: 'FEATURE', label: 'Feature' },
  { value: 'MICROSERVICE', label: 'Microservice' },
  { value: 'RELEASE', label: 'Release' },
  { value: 'UTILITY', label: 'Utility' },
  { value: 'INCIDENT', label: 'Incident' },
  { value: 'HOTFIX', label: 'Hotfix' },
];

const PRIORITY_COLORS: Record<ReminderPriority, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
};

const STATUS_COLORS: Record<ReminderStatus, string> = {
  PENDING: 'blue',
  COMPLETED: 'green',
  OVERDUE: 'red',
  SNOOZED: 'purple',
};

const MyWorkspacePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('notes');
  
  // Notes state
  const [notes, setNotes] = useState<UserNote[]>([]);
  const [notesLoading, setNotesLoading] = useState(false);
  const [noteSearch, setNoteSearch] = useState('');
  const [showArchived, setShowArchived] = useState(false);
  const [noteModalVisible, setNoteModalVisible] = useState(false);
  const [editingNote, setEditingNote] = useState<UserNote | null>(null);
  const [noteForm] = Form.useForm();
  
  // Reminders state
  const [reminders, setReminders] = useState<UserReminder[]>([]);
  const [remindersLoading, setRemindersLoading] = useState(false);
  const [reminderFilter, setReminderFilter] = useState<ReminderStatus | undefined>();
  const [reminderModalVisible, setReminderModalVisible] = useState(false);
  const [editingReminder, setEditingReminder] = useState<UserReminder | null>(null);
  const [reminderForm] = Form.useForm();
  const [reminderCounts, setReminderCounts] = useState<ReminderCounts>({ overdue: 0, pending: 0 });
  const [productivityData, setProductivityData] = useState<WorkspaceProductivityDTO | null>(null);
  const [productivityLoading, setProductivityLoading] = useState(false);
  const [dateRange, setDateRange] = useState('this_month');

  // Load notes
  const loadNotes = async () => {
    setNotesLoading(true);
    try {
      const response = await noteService.getMyNotes(0, 100, noteSearch, showArchived);
      setNotes(response.content);
    } catch (error) {
      message.error('Failed to load notes');
    } finally {
      setNotesLoading(false);
    }
  };

  // Load reminders
  const loadReminders = async () => {
    setRemindersLoading(true);
    try {
      const [response, counts] = await Promise.all([
        reminderService.getMyReminders(0, 100, reminderFilter),
        reminderService.getReminderCounts()
      ]);
      setReminders(response.content);
      setReminderCounts(counts);
    } catch (error) {
      message.error('Failed to load reminders');
    } finally {
      setRemindersLoading(false);
    }
  };

  // Load productivity data
  const loadProductivityData = async () => {
    setProductivityLoading(true);
    try {
      console.log('[MyWorkspace] Loading productivity data with dateRange:', dateRange);
      const data = await productivityService.getMyProductivityDashboard(dateRange);
      console.log('[MyWorkspace] Productivity data received:', data);
      setProductivityData(data);
    } catch (error) {
      message.error('Failed to load productivity data');
      console.error('Error loading productivity data:', error);
    } finally {
      setProductivityLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'notes') {
      loadNotes();
    } else if (activeTab === 'reminders') {
      loadReminders();
    } else if (activeTab === 'productivity') {
      loadProductivityData();
    }
  }, [activeTab, noteSearch, showArchived, reminderFilter, dateRange]);

  // Note handlers
  const handleCreateNote = () => {
    setEditingNote(null);
    noteForm.resetFields();
    setNoteModalVisible(true);
  };

  const handleEditNote = (note: UserNote) => {
    setEditingNote(note);
    noteForm.setFieldsValue({
      title: note.title,
      description: note.description,
      tags: note.tags,
      moduleType: note.moduleType,
      moduleId: note.moduleId,
    });
    setNoteModalVisible(true);
  };

  const handleSaveNote = async () => {
    try {
      const values = await noteForm.validateFields();
      const request: UserNoteRequest = {
        title: values.title,
        description: values.description,
        tags: values.tags,
        moduleType: values.moduleType,
        moduleId: values.moduleId,
      };

      if (editingNote) {
        await noteService.updateNote(editingNote.id, request);
        message.success('Note updated');
      } else {
        await noteService.createNote(request);
        message.success('Note created');
      }
      setNoteModalVisible(false);
      loadNotes();
    } catch (error) {
      message.error('Failed to save note');
    }
  };

  const handleDeleteNote = async (id: string) => {
    try {
      await noteService.deleteNote(id);
      message.success('Note deleted');
      loadNotes();
    } catch (error) {
      message.error('Failed to delete note');
    }
  };

  const handleTogglePin = async (id: string) => {
    try {
      await noteService.togglePin(id);
      loadNotes();
    } catch (error) {
      message.error('Failed to toggle pin');
    }
  };

  const handleToggleArchive = async (id: string) => {
    try {
      await noteService.toggleArchive(id);
      message.success(showArchived ? 'Note restored' : 'Note archived');
      loadNotes();
    } catch (error) {
      message.error('Failed to archive note');
    }
  };

  // Reminder handlers
  const handleCreateReminder = () => {
    setEditingReminder(null);
    reminderForm.resetFields();
    reminderForm.setFieldsValue({ priority: 'MEDIUM' });
    setReminderModalVisible(true);
  };

  const handleEditReminder = (reminder: UserReminder) => {
    setEditingReminder(reminder);
    reminderForm.setFieldsValue({
      title: reminder.title,
      description: reminder.description,
      reminderDatetime: dayjs(reminder.reminderDatetime),
      priority: reminder.priority,
      moduleType: reminder.moduleType,
      moduleId: reminder.moduleId,
    });
    setReminderModalVisible(true);
  };

  const handleSaveReminder = async () => {
    try {
      const values = await reminderForm.validateFields();
      const request: UserReminderRequest = {
        title: values.title,
        description: values.description,
        reminderDatetime: values.reminderDatetime.toISOString(),
        priority: values.priority,
        moduleType: values.moduleType,
        moduleId: values.moduleId,
      };

      if (editingReminder) {
        await reminderService.updateReminder(editingReminder.id, request);
        message.success('Reminder updated');
      } else {
        await reminderService.createReminder(request);
        message.success('Reminder created');
      }
      setReminderModalVisible(false);
      loadReminders();
    } catch (error) {
      message.error('Failed to save reminder');
    }
  };

  const handleDeleteReminder = async (id: string) => {
    try {
      await reminderService.deleteReminder(id);
      message.success('Reminder deleted');
      loadReminders();
    } catch (error) {
      message.error('Failed to delete reminder');
    }
  };

  const handleCompleteReminder = async (id: string) => {
    try {
      await reminderService.markAsCompleted(id);
      message.success('Reminder completed');
      loadReminders();
    } catch (error) {
      message.error('Failed to complete reminder');
    }
  };

  const handleSnoozeReminder = async (id: string, minutes: number) => {
    try {
      await reminderService.snoozeReminder(id, minutes);
      message.success(`Reminder snoozed for ${minutes} minutes`);
      loadReminders();
    } catch (error) {
      message.error('Failed to snooze reminder');
    }
  };

  // Render notes tab
  const renderNotesTab = () => (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <Input
          placeholder="Search notes..."
          prefix={<SearchOutlined />}
          value={noteSearch}
          onChange={(e) => setNoteSearch(e.target.value)}
          style={{ width: 250 }}
          allowClear
        />
        <Button 
          type={showArchived ? 'primary' : 'default'}
          icon={<InboxOutlined />}
          onClick={() => setShowArchived(!showArchived)}
        >
          {showArchived ? 'Show Active' : 'Show Archived'}
        </Button>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateNote}>
          New Note
        </Button>
      </div>

      <Spin spinning={notesLoading}>
        {notes.length === 0 ? (
          <Empty description={showArchived ? 'No archived notes' : 'No notes yet. Create your first note!'} />
        ) : (
          <List
            grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2, xl: 3, xxl: 3 }}
            dataSource={notes}
            renderItem={(note) => (
              <List.Item>
                <Card
                  size="small"
                  title={
                    <Space>
                      {note.isPinned && <PushpinFilled style={{ color: '#1890ff' }} />}
                      <Text strong ellipsis style={{ maxWidth: 200 }}>{note.title}</Text>
                    </Space>
                  }
                  extra={
                    <Space>
                      <Tooltip title={note.isPinned ? 'Unpin' : 'Pin'}>
                        <Button 
                          type="text" 
                          size="small" 
                          icon={note.isPinned ? <PushpinFilled /> : <PushpinOutlined />}
                          onClick={() => handleTogglePin(note.id)}
                        />
                      </Tooltip>
                      <Tooltip title="Edit">
                        <Button 
                          type="text" 
                          size="small" 
                          icon={<EditOutlined />}
                          onClick={() => handleEditNote(note)}
                        />
                      </Tooltip>
                      <Tooltip title={showArchived ? 'Restore' : 'Archive'}>
                        <Button 
                          type="text" 
                          size="small" 
                          icon={<InboxOutlined />}
                          onClick={() => handleToggleArchive(note.id)}
                        />
                      </Tooltip>
                      <Popconfirm
                        title="Delete this note?"
                        onConfirm={() => handleDeleteNote(note.id)}
                      >
                        <Button type="text" size="small" danger icon={<DeleteOutlined />} />
                      </Popconfirm>
                    </Space>
                  }
                  style={{ height: '100%' }}
                >
                  <Paragraph ellipsis={{ rows: 3 }} style={{ marginBottom: 8 }}>
                    {note.description || <Text type="secondary">No description</Text>}
                  </Paragraph>
                  {note.tags && note.tags.length > 0 && (
                    <div style={{ marginBottom: 8 }}>
                      {note.tags.map((tag) => (
                        <Tag key={tag} color="blue">{tag}</Tag>
                      ))}
                    </div>
                  )}
                  {note.moduleName && (
                    <div style={{ marginBottom: 8 }}>
                      <Tag icon={<LinkOutlined />} color="purple">
                        {note.moduleType}: {note.moduleName}
                      </Tag>
                    </div>
                  )}
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {dayjs(note.updatedAt).fromNow()}
                  </Text>
                </Card>
              </List.Item>
            )}
          />
        )}
      </Spin>
    </div>
  );

  // Render reminders tab
  const renderRemindersTab = () => (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <Statistic 
              title="Overdue" 
              value={reminderCounts.overdue} 
              valueStyle={{ color: reminderCounts.overdue > 0 ? '#cf1322' : undefined }}
              prefix={<ExclamationCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic 
              title="Pending" 
              value={reminderCounts.pending} 
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <div style={{ marginBottom: 16, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <Select
          placeholder="Filter by status"
          value={reminderFilter}
          onChange={setReminderFilter}
          allowClear
          style={{ width: 150 }}
        >
          <Select.Option value="PENDING">Pending</Select.Option>
          <Select.Option value="OVERDUE">Overdue</Select.Option>
          <Select.Option value="COMPLETED">Completed</Select.Option>
          <Select.Option value="SNOOZED">Snoozed</Select.Option>
        </Select>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateReminder}>
          New Reminder
        </Button>
      </div>

      <Spin spinning={remindersLoading}>
        {reminders.length === 0 ? (
          <Empty description="No reminders. Create your first reminder!" />
        ) : (
          <List
            dataSource={reminders}
            renderItem={(reminder) => (
              <List.Item
                style={{ 
                  background: reminder.status === 'OVERDUE' ? '#fff2f0' : undefined,
                  padding: '12px 16px',
                  marginBottom: 8,
                  borderRadius: 8,
                  border: '1px solid #f0f0f0'
                }}
                actions={[
                  reminder.status !== 'COMPLETED' && (
                    <Tooltip title="Mark as completed" key="complete">
                      <Button 
                        type="text" 
                        icon={<CheckOutlined />}
                        onClick={() => handleCompleteReminder(reminder.id)}
                      />
                    </Tooltip>
                  ),
                  reminder.status !== 'COMPLETED' && (
                    <Tooltip title="Snooze 15 min" key="snooze">
                      <Button 
                        type="text" 
                        icon={<ClockCircleOutlined />}
                        onClick={() => handleSnoozeReminder(reminder.id, 15)}
                      />
                    </Tooltip>
                  ),
                  <Tooltip title="Edit" key="edit">
                    <Button 
                      type="text" 
                      icon={<EditOutlined />}
                      onClick={() => handleEditReminder(reminder)}
                    />
                  </Tooltip>,
                  <Popconfirm
                    title="Delete this reminder?"
                    onConfirm={() => handleDeleteReminder(reminder.id)}
                    key="delete"
                  >
                    <Button type="text" danger icon={<DeleteOutlined />} />
                  </Popconfirm>,
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  avatar={
                    <Badge 
                      status={reminder.status === 'OVERDUE' ? 'error' : reminder.status === 'COMPLETED' ? 'success' : 'processing'} 
                    />
                  }
                  title={
                    <Space>
                      <Text strong style={{ textDecoration: reminder.status === 'COMPLETED' ? 'line-through' : undefined }}>
                        {reminder.title}
                      </Text>
                      <Tag color={PRIORITY_COLORS[reminder.priority]}>{reminder.priority}</Tag>
                      <Tag color={STATUS_COLORS[reminder.status]}>{reminder.status}</Tag>
                      {reminder.isSystemGenerated && <Tag>System</Tag>}
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={4}>
                      {reminder.description && <Text type="secondary">{reminder.description}</Text>}
                      <Space>
                        <BellOutlined />
                        <Text type="secondary">
                          {dayjs(reminder.reminderDatetime).format('MMM D, YYYY h:mm A')}
                          {' '}({dayjs(reminder.reminderDatetime).fromNow()})
                        </Text>
                      </Space>
                      {reminder.moduleName && (
                        <Tag icon={<LinkOutlined />} color="purple">
                          {reminder.moduleType}: {reminder.moduleName}
                        </Tag>
                      )}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Spin>
    </div>
  );

  // Render productivity tab
  const renderProductivityTab = () => (
    <Spin spinning={productivityLoading}>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Select
            value={dateRange}
            onChange={setDateRange}
            style={{ width: 150 }}
          >
            <Select.Option value="today">Today</Select.Option>
            <Select.Option value="this_week">This Week</Select.Option>
            <Select.Option value="this_month">This Month</Select.Option>
          </Select>
          {productivityData && (
            <Text type="secondary">
              Generated: {dayjs(productivityData.generatedAt).format('MMM DD, YYYY HH:mm')}
            </Text>
          )}
        </Space>
      </div>

      {productivityData ? (
        <Row gutter={[16, 16]}>
          {/* SECTION A - My Active Work */}
          <Col span={24}>
            <Card title=" My Active Work" size="small">
              <Row gutter={16}>
                <Col span={4}>
                  <Statistic title="Active Features" value={productivityData.activeFeatures} />
                </Col>
                <Col span={4}>
                  <Statistic title="Active Incidents" value={productivityData.activeIncidents} />
                </Col>
                <Col span={4}>
                  <Statistic title="Active Hotfixes" value={productivityData.activeHotfixes} />
                </Col>
                <Col span={4}>
                  <Statistic title="Active Issues" value={productivityData.activeIssues} />
                </Col>
                <Col span={4}>
                  <Statistic title="Active Microservices" value={productivityData.activeMicroservices} />
                </Col>
                <Col span={4}>
                  <Statistic 
                    title="Overdue Tasks" 
                    value={productivityData.overdueTasks}
                    valueStyle={{ color: productivityData.overdueTasks > 0 ? '#cf1322' : '#3f8600' }}
                  />
                </Col>
              </Row>
            </Card>
          </Col>

          {/* SECTION B - My Productivity Metrics */}
          <Col span={24}>
            <Card title=" My Productivity Metrics" size="small">
              <Row gutter={16}>
                <Col span={6}>
                  <Statistic
                    title="Completion Rate"
                    value={productivityData.completionRate}
                    precision={1}
                    suffix="%"
                    valueStyle={{ color: productivityData.completionRate > 70 ? '#3f8600' : '#cf1322' }}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="On-Time Delivery"
                    value={productivityData.onTimeRate}
                    precision={1}
                    suffix="%"
                    valueStyle={{ color: productivityData.onTimeRate > 80 ? '#3f8600' : '#cf1322' }}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="Avg Resolution Time"
                    value={productivityData.avgResolutionTime}
                    precision={1}
                    suffix="h"
                  />
                </Col>
                <Col span={6}>
                  <Tooltip title={
                    <div>
                      <div><strong>Score Calculation:</strong></div>
                      <div>• Completed Feature: 10 points</div>
                      <div>• Resolved Incident: 5 points</div>
                      <div>• Deployed Hotfix: 5 points</div>
                      <div>• Resolved Issue: 3 points</div>
                      <div>• Completed Checkpoint: 1 point</div>
                    </div>
                  }>
                    <Statistic
                      title={<span>Productivity Score <InfoCircleOutlined style={{ fontSize: 12, color: '#999' }} /></span>}
                      value={productivityData.productivityScore}
                      valueStyle={{ 
                        color: productivityData.productivityScore > 50 ? '#3f8600' : 
                               productivityData.productivityScore > 20 ? '#faad14' : '#cf1322' 
                      }}
                    />
                  </Tooltip>
                  {productivityData.trendPercentage !== 0 && (
                    <div style={{ fontSize: 12, marginTop: 4 }}>
                      <span style={{ 
                        color: productivityData.trendPercentage > 0 ? '#3f8600' : '#cf1322' 
                      }}>
                        {productivityData.trendPercentage > 0 ? '↑' : '↓'} {Math.abs(productivityData.trendPercentage).toFixed(1)}%
                      </span>
                    </div>
                  )}
                </Col>
              </Row>
            </Card>
          </Col>

          {/* SECTION C - Accountability */}
          <Col span={12}>
            <Card title=" Overdue Items" size="small">
              {productivityData.overdueItems.length > 0 ? (
                <List
                  size="small"
                  dataSource={productivityData.overdueItems.slice(0, 5)}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<ExclamationCircleOutlined style={{ color: '#cf1322' }} />}
                        title={
                          <Space>
                            <span>{item.title}</span>
                            <Tag color={item.priority === 'HIGH' ? 'red' : 'orange'}>{item.type}</Tag>
                          </Space>
                        }
                        description={
                          <Space direction="vertical" size={0}>
                            <Text type="secondary">{item.daysOverdue} days overdue</Text>
                            <Text type="secondary">Due: {dayjs(item.dueDate).format('MMM DD')}</Text>
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                />
              ) : (
                <Empty description="No overdue items! " image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Col>

          <Col span={12}>
            <Card title=" Recent Activities" size="small">
              {productivityData.recentActivities.length > 0 ? (
                <List
                  size="small"
                  dataSource={productivityData.recentActivities.slice(0, 5)}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={
                          item.onTime ? 
                            <CheckCircleOutlined style={{ color: '#3f8600' }} /> : 
                            <ClockCircleOutlined style={{ color: '#faad14' }} />
                        }
                        title={
                          <Space>
                            <span>{item.title}</span>
                            <Tag color="blue">{item.points} pts</Tag>
                          </Space>
                        }
                        description={
                          <Space direction="vertical" size={0}>
                            <Text type="secondary">{item.type.replace('_', ' ')}</Text>
                            <Text type="secondary">
                              {dayjs(item.completedAt).fromNow()}
                              {item.onTime ? ' (On time)' : ' (Late)'}
                            </Text>
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                />
              ) : (
                <Empty description="No recent activities" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </Card>
          </Col>
        </Row>
      ) : (
        <Empty description="No productivity data available" />
      )}
    </Spin>
  );

  return (
    <div style={{ padding: 24 }}>
      <Card 
        title={
          <Space>
            <span>My Workspace</span>
            {reminderCounts.overdue > 0 && (
              <Badge count={reminderCounts.overdue} style={{ backgroundColor: '#cf1322' }} />
            )}
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'notes',
              label: (
                <span>
                  <EditOutlined /> My Notes
                </span>
              ),
              children: renderNotesTab(),
            },
            {
              key: 'reminders',
              label: (
                <span>
                  <BellOutlined /> My Reminders
                  {reminderCounts.overdue > 0 && (
                    <Badge count={reminderCounts.overdue} size="small" style={{ marginLeft: 8 }} />
                  )}
                </span>
              ),
              children: renderRemindersTab(),
            },
            {
              key: 'productivity',
              label: (
                <span>
                  <TrophyOutlined /> My Productivity
                </span>
              ),
              children: renderProductivityTab(),
            },
          ]}
        />
      </Card>

      {/* Note Modal */}
      <Modal
        title={editingNote ? 'Edit Note' : 'Create Note'}
        open={noteModalVisible}
        onOk={handleSaveNote}
        onCancel={() => setNoteModalVisible(false)}
        width={600}
      >
        <Form form={noteForm} layout="vertical">
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: 'Title is required' }]}
          >
            <Input placeholder="Note title" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <TextArea rows={4} placeholder="Note description..." />
          </Form.Item>
          <Form.Item name="tags" label="Tags">
            <Select mode="tags" placeholder="Add tags (e.g., KYC, Payment, Bug)">
              <Select.Option value="KYC">KYC</Select.Option>
              <Select.Option value="Payment">Payment</Select.Option>
              <Select.Option value="Release">Release</Select.Option>
              <Select.Option value="Bug">Bug</Select.Option>
              <Select.Option value="Feature">Feature</Select.Option>
              <Select.Option value="Documentation">Documentation</Select.Option>
            </Select>
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="moduleType" label="Link to Module">
                <Select placeholder="Select module type" allowClear>
                  {MODULE_TYPES.map((type) => (
                    <Select.Option key={type.value} value={type.value}>
                      {type.label}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="moduleId" label="Module ID">
                <Input placeholder="Module ID (UUID)" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* Reminder Modal */}
      <Modal
        title={editingReminder ? 'Edit Reminder' : 'Create Reminder'}
        open={reminderModalVisible}
        onOk={handleSaveReminder}
        onCancel={() => setReminderModalVisible(false)}
        width={600}
      >
        <Form form={reminderForm} layout="vertical">
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: 'Title is required' }]}
          >
            <Input placeholder="Reminder title" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <TextArea rows={3} placeholder="Reminder description..." />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="reminderDatetime"
                label="Reminder Date & Time"
                rules={[{ required: true, message: 'Date/time is required' }]}
              >
                <DatePicker 
                  showTime 
                  style={{ width: '100%' }} 
                  format="YYYY-MM-DD HH:mm"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="priority" label="Priority">
                <Select>
                  <Select.Option value="LOW">Low</Select.Option>
                  <Select.Option value="MEDIUM">Medium</Select.Option>
                  <Select.Option value="HIGH">High</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="moduleType" label="Link to Module">
                <Select placeholder="Select module type" allowClear>
                  {MODULE_TYPES.map((type) => (
                    <Select.Option key={type.value} value={type.value}>
                      {type.label}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="moduleId" label="Module ID">
                <Input placeholder="Module ID (UUID)" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default MyWorkspacePage;
