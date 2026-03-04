import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Avatar, theme, Typography } from 'antd';
import {
  DashboardOutlined,
  CheckSquareOutlined,
  ApiOutlined,
  AppstoreOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  NodeIndexOutlined,
  WarningOutlined,
  ThunderboltOutlined,
  BugOutlined,
  TeamOutlined,
  BarChartOutlined,
  SettingOutlined,
  FileTextOutlined,
  FolderOutlined,
  AuditOutlined,
  RocketOutlined,
  BookOutlined,
  DesktopOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import GlobalSearch from '../components/GlobalSearch';
import NotificationBell from '../components/NotificationBell';
import GamifiedProgress from '../components/GamifiedProgress';

const { Header, Sider, Content, Footer } = Layout;
const { Text } = Typography;

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAdmin } = useAuth();
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken();

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/workspace',
      icon: <DesktopOutlined />,
      label: 'My Workspace',
    },
    {
      key: '/relationships',
      icon: <NodeIndexOutlined />,
      label: 'Service Map',
    },
    {
      key: 'services',
      icon: <SettingOutlined />,
      label: 'Services',
      children: [
        {
          key: '/features',
          icon: <AppstoreOutlined />,
          label: 'Features',
        },
        {
          key: '/microservices',
          icon: <ApiOutlined />,
          label: 'Microservices',
        },
        {
          key: '/checklists',
          icon: <CheckSquareOutlined />,
          label: 'Checklists',
        },
        {
          key: '/incidents',
          icon: <WarningOutlined />,
          label: 'Incidents',
        },
        {
          key: '/hotfixes',
          icon: <ThunderboltOutlined />,
          label: 'Hotfixes',
        },
        {
          key: '/issues',
          icon: <BugOutlined />,
          label: 'Issues',
        },
        {
          key: '/domains',
          icon: <FolderOutlined />,
          label: 'Domains',
        },
      ],
    },
    {
      key: '/utilities',
      icon: <FileTextOutlined />,
      label: 'Utilities',
    },
    {
      key: '/qna',
      icon: <QuestionCircleOutlined />,
      label: 'Q&A Knowledge Base',
    },
    {
      key: '/impact-analysis',
      icon: <ThunderboltOutlined />,
      label: 'Impact Analysis',
    },
    {
      key: '/releases',
      icon: <RocketOutlined />,
      label: 'Release Management',
    },
    ...(isAdmin ? [
      {
        key: '/reporting',
        icon: <BarChartOutlined />,
        label: 'Reporting',
      },
      {
        key: '/audit',
        icon: <AuditOutlined />,
        label: 'Audit Logs',
      },
      {
        key: '/users',
        icon: <TeamOutlined />,
        label: 'User Management',
      },
    ] : []),
  ];

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: `${user?.username} (${user?.role})`,
      disabled: true,
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: () => {
        logout();
        navigate('/login');
      },
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed} 
        theme="dark"
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          zIndex: 100,
        }}
      >
        <div className="logo">
          <BookOutlined style={{ fontSize: collapsed ? 20 : 18, marginRight: collapsed ? 0 : 8 }} />
          {!collapsed && <span>Dev Wiki</span>}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header 
          style={{ 
            padding: '0 24px', 
            background: colorBgContainer, 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'space-between',
            position: 'sticky',
            top: 0,
            zIndex: 99,
            boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: '16px', width: 48, height: 48 }}
            />
            <GlobalSearch />
            <GamifiedProgress />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            {isAdmin && (
              <Text style={{ color: '#6366f1', fontWeight: 500, fontSize: 13 }}>Admin Mode</Text>
            )}
            <NotificationBell />
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Avatar style={{ cursor: 'pointer', backgroundColor: '#6366f1' }} icon={<UserOutlined />} />
            </Dropdown>
          </div>
        </Header>
        <Content 
          style={{ 
            margin: '24px', 
            padding: 24, 
            background: colorBgContainer, 
            borderRadius: borderRadiusLG, 
            minHeight: 'calc(100vh - 64px - 70px - 48px)',
            overflow: 'auto',
          }}
        >
          <Outlet />
        </Content>
        <Footer 
          style={{ 
            textAlign: 'center', 
            background: 'transparent',
            padding: '16px 24px',
            color: '#94a3b8',
            fontSize: 13,
          }}
        >
          Dev Wiki © {new Date().getFullYear()} • Built with ❤️ for Developers
        </Footer>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
