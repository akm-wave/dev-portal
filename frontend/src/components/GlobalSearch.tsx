import React, { useState, useCallback, useRef, useEffect } from 'react';
import { Input, Dropdown, Spin, Empty, Tag, Typography } from 'antd';
import { SearchOutlined, AppstoreOutlined, ApiOutlined, CheckSquareOutlined, WarningOutlined, BugOutlined, ToolOutlined, FolderOutlined, FileTextOutlined, RocketOutlined, PaperClipOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { globalSearchService } from '../services/globalSearchService';
import { GlobalSearchResult, SearchItem } from '../types';
import debounce from 'lodash/debounce';

const { Text } = Typography;

const typeIcons: Record<string, React.ReactNode> = {
  domain: <FolderOutlined style={{ color: '#0ea5e9' }} />,
  feature: <AppstoreOutlined style={{ color: '#6366f1' }} />,
  microservice: <ApiOutlined style={{ color: '#22c55e' }} />,
  utility: <FileTextOutlined style={{ color: '#14b8a6' }} />,
  checklist: <CheckSquareOutlined style={{ color: '#f59e0b' }} />,
  incident: <WarningOutlined style={{ color: '#ef4444' }} />,
  hotfix: <ToolOutlined style={{ color: '#8b5cf6' }} />,
  issue: <BugOutlined style={{ color: '#ec4899' }} />,
  release: <RocketOutlined style={{ color: '#3b82f6' }} />,
  attachment: <PaperClipOutlined style={{ color: '#64748b' }} />,
  question: <QuestionCircleOutlined style={{ color: '#10b981' }} />,
};

const typeColors: Record<string, string> = {
  domain: 'cyan',
  feature: 'purple',
  microservice: 'green',
  utility: 'teal',
  checklist: 'orange',
  incident: 'red',
  hotfix: 'geekblue',
  issue: 'magenta',
  release: 'blue',
  attachment: 'default',
  question: 'lime',
};

const GlobalSearch: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<GlobalSearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const inputRef = useRef<any>(null);

  const performSearch = useCallback(
    debounce(async (searchQuery: string) => {
      if (!searchQuery.trim()) {
        setResults(null);
        setOpen(false);
        return;
      }

      setLoading(true);
      try {
        const data = await globalSearchService.search(searchQuery, 5);
        setResults(data);
        setOpen(data.totalCount > 0);
      } catch (error) {
        console.error('Search failed:', error);
        setResults(null);
      } finally {
        setLoading(false);
      }
    }, 300),
    []
  );

  useEffect(() => {
    performSearch(query);
  }, [query, performSearch]);

  const handleSelect = (item: SearchItem) => {
    setOpen(false);
    setQuery('');
    navigate(item.url);
  };

  const renderCategory = (title: string, items: SearchItem[], type: string) => {
    if (!items || items.length === 0) return null;

    return (
      <div key={type} style={{ marginBottom: 12 }}>
        <div style={{ padding: '4px 12px', background: '#f5f5f5', borderRadius: 4, marginBottom: 4 }}>
          <Text strong style={{ fontSize: 12, textTransform: 'uppercase', color: '#666' }}>
            {typeIcons[type]} {title} ({items.length})
          </Text>
        </div>
        {items.map((item) => (
          <div
            key={item.id}
            onClick={() => handleSelect(item)}
            style={{
              padding: '8px 12px',
              cursor: 'pointer',
              borderRadius: 4,
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              transition: 'background 0.2s',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.background = '#f0f0f0')}
            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
          >
            {typeIcons[type]}
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {item.name}
              </div>
              {item.contentSnippet ? (
                <div style={{ fontSize: 11, color: '#666', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', background: '#fffbe6', padding: '2px 4px', borderRadius: 2, marginTop: 2 }}>
                  "{item.contentSnippet}"
                </div>
              ) : item.description && (
                <div style={{ fontSize: 12, color: '#888', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {item.description}
                </div>
              )}
              {item.moduleType && (
                <div style={{ fontSize: 10, color: '#999', marginTop: 2 }}>
                  in {item.moduleType}
                </div>
              )}
            </div>
            <Tag color={typeColors[type]} style={{ fontSize: 10 }}>
              {item.status}
            </Tag>
          </div>
        ))}
      </div>
    );
  };

  const dropdownContent = (
    <div
      style={{
        background: '#fff',
        borderRadius: 8,
        boxShadow: '0 6px 16px rgba(0,0,0,0.12)',
        padding: 8,
        minWidth: 350,
        maxWidth: 450,
        maxHeight: 400,
        overflowY: 'auto',
      }}
    >
      {loading ? (
        <div style={{ textAlign: 'center', padding: 20 }}>
          <Spin size="small" />
          <div style={{ marginTop: 8, color: '#888' }}>Searching...</div>
        </div>
      ) : results && results.totalCount > 0 ? (
        <>
          {renderCategory('Domains', results.domains, 'domain')}
          {renderCategory('Features', results.features, 'feature')}
          {renderCategory('Microservices', results.microservices, 'microservice')}
          {renderCategory('Releases', results.releases, 'release')}
          {renderCategory('Utilities', results.utilities, 'utility')}
          {renderCategory('Checklists', results.checklists, 'checklist')}
          {renderCategory('Incidents', results.incidents, 'incident')}
          {renderCategory('Hotfixes', results.hotfixes, 'hotfix')}
          {renderCategory('Issues', results.issues, 'issue')}
          {renderCategory('Document Content', results.attachments, 'attachment')}
          {renderCategory('Q&A Questions', results.questions, 'question')}
        </>
      ) : query.trim() ? (
        <Empty description="No results found" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : null}
    </div>
  );

  return (
    <Dropdown
      open={open && (loading || (results && results.totalCount > 0) || query.trim().length > 0)}
      popupRender={() => dropdownContent}
      trigger={[]}
      placement="bottomLeft"
    >
      <Input
        ref={inputRef}
        placeholder="Search services, features, issues..."
        prefix={<SearchOutlined style={{ color: '#999' }} />}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={() => results && results.totalCount > 0 && setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 200)}
        style={{
          width: 280,
          borderRadius: 20,
          background: '#f5f5f5',
          border: 'none',
        }}
        allowClear
      />
    </Dropdown>
  );
};

export default GlobalSearch;
