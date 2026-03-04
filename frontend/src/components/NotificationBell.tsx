import React, { useState, useEffect } from 'react';
import { Badge, Dropdown, List, Typography, Button, Space, Empty, Tag, message } from 'antd';
import { 
  BellOutlined, 
  CheckOutlined, 
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  RightOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { reminderService } from '../services/workspaceService';
import { UserReminder, ReminderCounts } from '../types';

dayjs.extend(relativeTime);

const { Text } = Typography;

const PRIORITY_COLORS: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
};

const NotificationBell: React.FC = () => {
  const navigate = useNavigate();
  const [reminders, setReminders] = useState<UserReminder[]>([]);
  const [counts, setCounts] = useState<ReminderCounts>({ overdue: 0, pending: 0 });
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const loadReminders = async () => {
    setLoading(true);
    try {
      const [overdue, today, upcoming, reminderCounts] = await Promise.all([
        reminderService.getOverdueReminders(),
        reminderService.getTodayReminders(),
        reminderService.getUpcomingReminders(),
        reminderService.getReminderCounts(),
      ]);
      setReminders([...overdue, ...today, ...upcoming].slice(0, 8));
      setCounts(reminderCounts);
    } catch (error) {
      console.error('Failed to load reminders:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReminders();
    // Poll for updates every 60 seconds
    const interval = setInterval(loadReminders, 60000);
    return () => clearInterval(interval);
  }, []);

  const handleComplete = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await reminderService.markAsCompleted(id);
      message.success('Reminder completed');
      loadReminders();
    } catch (error) {
      message.error('Failed to complete reminder');
    }
  };

  const totalCount = counts.overdue + counts.pending;

  const dropdownContent = (
    <div style={{ 
      width: 360, 
      background: '#fff', 
      borderRadius: 12, 
      boxShadow: '0 6px 24px rgba(0,0,0,0.12)',
      overflow: 'hidden'
    }}>
      <div style={{ 
        padding: '12px 16px', 
        borderBottom: '1px solid #f0f0f0',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <Space>
          <BellOutlined style={{ color: '#5b6cf0' }} />
          <Text strong>Reminders</Text>
          {counts.overdue > 0 && (
            <Tag color="red" style={{ margin: 0 }}>{counts.overdue} overdue</Tag>
          )}
        </Space>
        <Button 
          type="link" 
          size="small" 
          onClick={() => { setOpen(false); navigate('/workspace'); }}
        >
          View All <RightOutlined />
        </Button>
      </div>
      
      <div style={{ maxHeight: 400, overflow: 'auto' }}>
        {reminders.length === 0 ? (
          <Empty 
            image={Empty.PRESENTED_IMAGE_SIMPLE} 
            description="No reminders" 
            style={{ padding: '32px 0' }}
          />
        ) : (
          <List
            loading={loading}
            dataSource={reminders}
            renderItem={(reminder) => {
              const isOverdue = reminder.status === 'OVERDUE';
              const isToday = dayjs(reminder.reminderDatetime).isSame(dayjs(), 'day');
              
              return (
                <List.Item
                  style={{ 
                    padding: '12px 16px',
                    background: isOverdue ? '#fef2f2' : 'transparent',
                    cursor: 'pointer',
                    borderBottom: '1px solid #f5f5f5'
                  }}
                  onClick={() => { setOpen(false); navigate('/workspace'); }}
                  actions={[
                    reminder.status !== 'COMPLETED' && (
                      <Button 
                        type="text" 
                        size="small" 
                        icon={<CheckOutlined />}
                        onClick={(e) => handleComplete(reminder.id, e)}
                        key="complete"
                      />
                    )
                  ].filter(Boolean)}
                >
                  <List.Item.Meta
                    avatar={
                      <div style={{
                        width: 32,
                        height: 32,
                        borderRadius: 8,
                        background: isOverdue ? '#fee2e2' : isToday ? '#fef3c7' : '#e0e7ff',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}>
                        {isOverdue ? (
                          <ExclamationCircleOutlined style={{ color: '#ef4444' }} />
                        ) : (
                          <ClockCircleOutlined style={{ color: isToday ? '#f59e0b' : '#5b6cf0' }} />
                        )}
                      </div>
                    }
                    title={
                      <Space size={4}>
                        <Text ellipsis style={{ maxWidth: 180, fontSize: 13 }}>
                          {reminder.title}
                        </Text>
                        <Tag 
                          color={PRIORITY_COLORS[reminder.priority]} 
                          style={{ fontSize: 10, margin: 0, padding: '0 4px' }}
                        >
                          {reminder.priority}
                        </Tag>
                      </Space>
                    }
                    description={
                      <Text style={{ fontSize: 11, color: isOverdue ? '#dc2626' : '#64748b' }}>
                        {dayjs(reminder.reminderDatetime).fromNow()}
                      </Text>
                    }
                  />
                </List.Item>
              );
            }}
          />
        )}
      </div>
    </div>
  );

  return (
    <Dropdown
      dropdownRender={() => dropdownContent}
      trigger={['click']}
      open={open}
      onOpenChange={setOpen}
      placement="bottomRight"
    >
      <Badge 
        count={totalCount} 
        size="small"
        style={{ backgroundColor: counts.overdue > 0 ? '#ef4444' : '#5b6cf0' }}
      >
        <Button 
          type="text" 
          icon={<BellOutlined style={{ fontSize: 18 }} />}
          style={{ 
            width: 40, 
            height: 40,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        />
      </Badge>
    </Dropdown>
  );
};

export default NotificationBell;
