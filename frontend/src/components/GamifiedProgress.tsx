import React, { useEffect, useState } from 'react';
import { Progress, Tooltip, Spin } from 'antd';
import { progressService, UserProgress } from '../services/progressService';
import { useAuth } from '../contexts/AuthContext';

const GamifiedProgress: React.FC = () => {
  const [progress, setProgress] = useState<UserProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    const fetchProgress = async () => {
      try {
        const data = await progressService.getMyProgress();
        setProgress(data);
      } catch (error) {
        console.error('Failed to fetch progress:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProgress();
    
    const interval = setInterval(fetchProgress, 30000);
    return () => clearInterval(interval);
  }, [isAuthenticated]);

  if (!isAuthenticated || loading) {
    return loading ? <Spin size="small" /> : null;
  }

  if (!progress || progress.totalTasks === 0) {
    return null;
  }

  const getProgressColor = (percent: number) => {
    if (percent >= 100) return '#52c41a';
    if (percent >= 60) return '#1890ff';
    if (percent >= 20) return '#faad14';
    return '#d9d9d9';
  };

  const getAvatarPosition = (percent: number) => {
    return Math.min(percent, 100);
  };

  const tooltipContent = (
    <div style={{ padding: '4px 0' }}>
      <div style={{ fontWeight: 600, marginBottom: 8, fontSize: 14 }}>
        {progress.emoji} {progress.progressLevel.replace('_', ' ')}
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px 16px', fontSize: 12 }}>
        <span>Assigned:</span>
        <span style={{ fontWeight: 500 }}>{progress.totalTasks}</span>
        <span>Done:</span>
        <span style={{ fontWeight: 500, color: '#52c41a' }}>{progress.completedTasks}</span>
        <span>In Progress:</span>
        <span style={{ fontWeight: 500, color: '#1890ff' }}>{progress.inProgressTasks}</span>
        <span>Blocked:</span>
        <span style={{ fontWeight: 500, color: '#ff4d4f' }}>{progress.blockedTasks}</span>
        <span>Pending:</span>
        <span style={{ fontWeight: 500, color: '#d9d9d9' }}>{progress.pendingTasks}</span>
      </div>
      <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px solid rgba(255,255,255,0.2)', fontSize: 11, color: 'rgba(255,255,255,0.7)' }}>
        Weighted Progress: {progress.completedWeight}/{progress.totalWeight}
      </div>
    </div>
  );

  return (
    <Tooltip title={tooltipContent} placement="bottom" overlayStyle={{ maxWidth: 280 }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          padding: '4px 12px',
          background: 'rgba(255,255,255,0.1)',
          borderRadius: 20,
          cursor: 'pointer',
          transition: 'all 0.3s ease',
          minWidth: 180,
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = 'rgba(255,255,255,0.15)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = 'rgba(255,255,255,0.1)';
        }}
      >
        <div style={{ position: 'relative', flex: 1 }}>
          <Progress
            percent={progress.progress}
            size="small"
            strokeColor={getProgressColor(progress.progress)}
            trailColor="rgba(255,255,255,0.2)"
            showInfo={false}
            style={{ marginBottom: 0 }}
          />
          <div
            style={{
              position: 'absolute',
              top: '-8px',
              left: `calc(${getAvatarPosition(progress.progress)}% - 10px)`,
              fontSize: 16,
              transition: 'left 0.5s ease-out',
              filter: progress.progress >= 100 ? 'drop-shadow(0 0 4px gold)' : 'none',
            }}
          >
            {progress.emoji}
          </div>
        </div>
        <div style={{ 
          fontSize: 12, 
          fontWeight: 600, 
          color: '#fff',
          whiteSpace: 'nowrap',
          minWidth: 75,
          textAlign: 'right'
        }}>
          {progress.progress}% ({progress.completedTasks}/{progress.totalTasks})
        </div>
      </div>
    </Tooltip>
  );
};

export default GamifiedProgress;
