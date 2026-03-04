import React, { useState, useEffect } from 'react';
import { Card, List, Tag, Typography, Space, Badge, Empty, Spin, Button, Tooltip } from 'antd';
import { 
  BellOutlined, 
  PushpinFilled, 
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  EditOutlined,
  CheckOutlined,
  RightOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { noteService, reminderService } from '../services/workspaceService';
import { UserNote, UserReminder, ReminderCounts } from '../types';

dayjs.extend(relativeTime);

const { Text } = Typography;

const PRIORITY_COLORS: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
};

const MyOverviewWidget: React.FC = () => {
  const navigate = useNavigate();
  const [pinnedNotes, setPinnedNotes] = useState<UserNote[]>([]);
  const [overdueReminders, setOverdueReminders] = useState<UserReminder[]>([]);
  const [todayReminders, setTodayReminders] = useState<UserReminder[]>([]);
  const [upcomingReminders, setUpcomingReminders] = useState<UserReminder[]>([]);
  const [counts, setCounts] = useState<ReminderCounts>({ overdue: 0, pending: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [pinned, overdue, today, upcoming, reminderCounts] = await Promise.all([
        noteService.getPinnedNotes(),
        reminderService.getOverdueReminders(),
        reminderService.getTodayReminders(),
        reminderService.getUpcomingReminders(),
        reminderService.getReminderCounts(),
      ]);
      setPinnedNotes(pinned.slice(0, 3));
      setOverdueReminders(overdue.slice(0, 3));
      setTodayReminders(today.slice(0, 3));
      setUpcomingReminders(upcoming.slice(0, 3));
      setCounts(reminderCounts);
    } catch (error) {
      console.error('Failed to load workspace data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteReminder = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await reminderService.markAsCompleted(id);
      loadData();
    } catch (error) {
      console.error('Failed to complete reminder:', error);
    }
  };

  if (loading) {
    return (
      <Card 
        title={<span><EditOutlined style={{ marginRight: 8 }} />My Workspace</span>}
        style={{ border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
      >
        <div style={{ display: 'flex', justifyContent: 'center', padding: 40 }}>
          <Spin />
        </div>
      </Card>
    );
  }

  const hasOverdue = counts.overdue > 0;
  const allReminders = [...overdueReminders, ...todayReminders, ...upcomingReminders].slice(0, 5);

  return (
    <Card 
      title={
        <Space>
          <EditOutlined style={{ color: '#5b6cf0' }} />
          <span style={{ fontWeight: 600, color: '#1e293b' }}>My Workspace</span>
          {hasOverdue && (
            <Badge count={counts.overdue} style={{ backgroundColor: '#ef4444' }} />
          )}
        </Space>
      }
      extra={
        <Button type="link" size="small" onClick={() => navigate('/workspace')}>
          View All <RightOutlined />
        </Button>
      }
      style={{ border: 'none', borderRadius: 14, boxShadow: '0 2px 12px rgba(0,0,0,0.04)' }}
    >
      {/* Overdue Alert */}
      {hasOverdue && (
        <div style={{ 
          background: '#fef2f2', 
          padding: '12px 16px', 
          borderRadius: 10, 
          marginBottom: 16,
          border: '1px solid #fecaca'
        }}>
          <Space>
            <ExclamationCircleOutlined style={{ color: '#ef4444', fontSize: 18 }} />
            <Text strong style={{ color: '#dc2626' }}>
              {counts.overdue} overdue reminder{counts.overdue > 1 ? 's' : ''}
            </Text>
          </Space>
        </div>
      )}

      {/* Pinned Notes Section */}
      {pinnedNotes.length > 0 && (
        <div style={{ marginBottom: 16 }}>
          <Text strong style={{ fontSize: 13, color: '#64748b', display: 'block', marginBottom: 8 }}>
            <PushpinFilled style={{ color: '#5b6cf0', marginRight: 6 }} />
            Pinned Notes
          </Text>
          <List
            size="small"
            dataSource={pinnedNotes}
            renderItem={(note) => (
              <List.Item 
                style={{ 
                  padding: '8px 12px', 
                  background: '#f8fafc', 
                  borderRadius: 8, 
                  marginBottom: 6,
                  cursor: 'pointer',
                  border: '1px solid #e2e8f0'
                }}
                onClick={() => navigate('/workspace')}
              >
                <List.Item.Meta
                  title={
                    <Text ellipsis style={{ maxWidth: 200, fontWeight: 500 }}>
                      {note.title}
                    </Text>
                  }
                  description={
                    <Space size={4}>
                      {note.tags?.slice(0, 2).map((tag) => (
                        <Tag key={tag} color="blue" style={{ fontSize: 10, margin: 0 }}>{tag}</Tag>
                      ))}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </div>
      )}

      {/* Reminders Section */}
      <div>
        <Text strong style={{ fontSize: 13, color: '#64748b', display: 'block', marginBottom: 8 }}>
          <BellOutlined style={{ color: '#f59e0b', marginRight: 6 }} />
          Reminders
          {counts.pending > 0 && (
            <Tag color="blue" style={{ marginLeft: 8, fontSize: 10 }}>{counts.pending} pending</Tag>
          )}
        </Text>
        
        {allReminders.length === 0 ? (
          <Empty 
            image={Empty.PRESENTED_IMAGE_SIMPLE} 
            description="No reminders" 
            style={{ margin: '16px 0' }}
          />
        ) : (
          <List
            size="small"
            dataSource={allReminders}
            renderItem={(reminder) => {
              const isOverdue = reminder.status === 'OVERDUE';
              const isToday = dayjs(reminder.reminderDatetime).isSame(dayjs(), 'day');
              
              return (
                <List.Item 
                  style={{ 
                    padding: '8px 12px', 
                    background: isOverdue ? '#fef2f2' : isToday ? '#fffbeb' : '#f8fafc', 
                    borderRadius: 8, 
                    marginBottom: 6,
                    border: `1px solid ${isOverdue ? '#fecaca' : isToday ? '#fde68a' : '#e2e8f0'}`
                  }}
                  actions={[
                    reminder.status !== 'COMPLETED' && (
                      <Tooltip title="Mark complete" key="complete">
                        <Button 
                          type="text" 
                          size="small" 
                          icon={<CheckOutlined />}
                          onClick={(e) => handleCompleteReminder(reminder.id, e)}
                        />
                      </Tooltip>
                    )
                  ].filter(Boolean)}
                >
                  <List.Item.Meta
                    avatar={
                      <Badge 
                        status={isOverdue ? 'error' : isToday ? 'warning' : 'processing'} 
                      />
                    }
                    title={
                      <Space size={4}>
                        <Text ellipsis style={{ maxWidth: 150, fontWeight: 500 }}>
                          {reminder.title}
                        </Text>
                        <Tag color={PRIORITY_COLORS[reminder.priority]} style={{ fontSize: 10, margin: 0 }}>
                          {reminder.priority}
                        </Tag>
                      </Space>
                    }
                    description={
                      <Space size={4}>
                        <ClockCircleOutlined style={{ fontSize: 11 }} />
                        <Text style={{ fontSize: 11, color: isOverdue ? '#dc2626' : '#64748b' }}>
                          {dayjs(reminder.reminderDatetime).fromNow()}
                        </Text>
                      </Space>
                    }
                  />
                </List.Item>
              );
            }}
          />
        )}
      </div>
    </Card>
  );
};

export default MyOverviewWidget;
