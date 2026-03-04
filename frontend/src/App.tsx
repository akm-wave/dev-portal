import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuth } from './contexts/AuthContext';
import MainLayout from './layouts/MainLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ChecklistPage from './pages/ChecklistPage';
import MicroservicePage from './pages/MicroservicePage';
import FeaturePage from './pages/FeaturePage';
import RelationshipPage from './pages/RelationshipPage';
import FeatureDetailsPage from './pages/FeatureDetailsPage';
import IncidentPage from './pages/IncidentPage';
import HotfixPage from './pages/HotfixPage';
import IssuePage from './pages/IssuePage';
import UserManagementPage from './pages/UserManagementPage';
import ReportingPage from './pages/ReportingPage';
import UtilityPage from './pages/UtilityPage';
import MicroserviceDetailsPage from './pages/MicroserviceDetailsPage';
import ChecklistDetailsPage from './pages/ChecklistDetailsPage';
import IncidentDetailsPage from './pages/IncidentDetailsPage';
import HotfixDetailsPage from './pages/HotfixDetailsPage';
import IssueDetailsPage from './pages/IssueDetailsPage';
import UtilityDetailsPage from './pages/UtilityDetailsPage';
import DomainPage from './pages/DomainPage';
import AuditPage from './pages/AuditPage';
import ReleasePage from './pages/ReleasePage';
import ReleaseDetailsPage from './pages/ReleaseDetailsPage';
import MyWorkspacePage from './pages/MyWorkspacePage';
import QnaListPage from './pages/QnaListPage';
import QnaDetailPage from './pages/QnaDetailPage';
import ImpactAnalysisPage from './pages/ImpactAnalysisPage';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

const App: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="checklists" element={<ChecklistPage />} />
        <Route path="microservices" element={<MicroservicePage />} />
        <Route path="features" element={<FeaturePage />} />
        <Route path="features/:id" element={<FeatureDetailsPage />} />
        <Route path="relationships" element={<RelationshipPage />} />
        <Route path="incidents" element={<IncidentPage />} />
        <Route path="hotfixes" element={<HotfixPage />} />
        <Route path="issues" element={<IssuePage />} />
        <Route path="users" element={<UserManagementPage />} />
        <Route path="reporting" element={<ReportingPage />} />
        <Route path="utilities" element={<UtilityPage />} />
        <Route path="domains" element={<DomainPage />} />
        <Route path="microservices/:id" element={<MicroserviceDetailsPage />} />
        <Route path="checklists/:id" element={<ChecklistDetailsPage />} />
        <Route path="incidents/:id" element={<IncidentDetailsPage />} />
        <Route path="hotfixes/:id" element={<HotfixDetailsPage />} />
        <Route path="issues/:id" element={<IssueDetailsPage />} />
        <Route path="utilities/:id" element={<UtilityDetailsPage />} />
        <Route path="audit" element={<AuditPage />} />
        <Route path="releases" element={<ReleasePage />} />
        <Route path="releases/:id" element={<ReleaseDetailsPage />} />
        <Route path="workspace" element={<MyWorkspacePage />} />
        <Route path="qna" element={<QnaListPage />} />
        <Route path="qna/:id" element={<QnaDetailPage />} />
        <Route path="impact-analysis" element={<ImpactAnalysisPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};

export default App;
