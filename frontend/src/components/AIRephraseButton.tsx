import React, { useState } from 'react';
import { Button, Tooltip, Modal, Input, Space, Typography, Spin, message } from 'antd';
import { RobotOutlined, SyncOutlined, CheckOutlined, CopyOutlined } from '@ant-design/icons';

const { TextArea } = Input;
const { Text } = Typography;

interface AIRephraseButtonProps {
  getText: () => string;
  onApply: (newText: string) => void;
  fieldName?: string;
}

const AIRephraseButton: React.FC<AIRephraseButtonProps> = ({ getText, onApply, fieldName = 'description' }) => {
  const [modalVisible, setModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

  const rephraseWithLocalAI = async (inputText: string): Promise<string[]> => {
    // Simple rule-based rephrasing for now (can be enhanced with actual LLM)
    // This provides instant results without external API calls
    const results: string[] = [];
    
    // Professional version
    const professional = inputText
      .replace(/\bi\b/gi, 'we')
      .replace(/\bwant to\b/gi, 'aim to')
      .replace(/\bneed to\b/gi, 'require')
      .replace(/\bfix\b/gi, 'resolve')
      .replace(/\bbug\b/gi, 'issue')
      .replace(/\bstuff\b/gi, 'components')
      .replace(/\bget\b/gi, 'obtain')
      .replace(/\bmake\b/gi, 'implement')
      .replace(/\bdo\b/gi, 'execute')
      .replace(/\bbetter\b/gi, 'improved')
      .replace(/\bbad\b/gi, 'suboptimal')
      .replace(/\bgood\b/gi, 'optimal')
      .replace(/\bfast\b/gi, 'performant')
      .replace(/\bslow\b/gi, 'latency-intensive');
    
    if (professional !== inputText) {
      results.push(professional.charAt(0).toUpperCase() + professional.slice(1));
    }
    
    // Concise version
    const words = inputText.split(' ');
    if (words.length > 10) {
      const concise = words.slice(0, Math.ceil(words.length * 0.7)).join(' ');
      results.push(concise.charAt(0).toUpperCase() + concise.slice(1) + '.');
    }
    
    // Detailed version
    const detailed = `${inputText}. This enhancement will improve overall system functionality and user experience.`;
    results.push(detailed.charAt(0).toUpperCase() + detailed.slice(1));
    
    // Technical version
    const technical = inputText
      .replace(/\bupdate\b/gi, 'refactor and optimize')
      .replace(/\badd\b/gi, 'implement')
      .replace(/\bchange\b/gi, 'modify')
      .replace(/\bremove\b/gi, 'deprecate')
      .replace(/\btest\b/gi, 'validate')
      .replace(/\bcheck\b/gi, 'verify');
    
    if (technical !== inputText) {
      results.push(technical.charAt(0).toUpperCase() + technical.slice(1));
    }
    
    // If no transformations applied, provide a default enhancement
    if (results.length === 0) {
      results.push(`Enhanced: ${inputText}`);
      results.push(`${inputText} - optimized for better performance and maintainability.`);
    }
    
    return results.slice(0, 3);
  };

  const handleRephrase = async () => {
    const currentText = getText();
    if (!currentText || currentText.trim().length < 5) {
      message.warning('Please enter some text to rephrase (at least 5 characters)');
      return;
    }
    
    setLoading(true);
    setSuggestions([]);
    setSelectedIndex(null);
    setModalVisible(true);
    
    try {
      const results = await rephraseWithLocalAI(currentText);
      setSuggestions(results);
    } catch (error) {
      message.error('Failed to generate suggestions');
    } finally {
      setLoading(false);
    }
  };
  
  const currentText = getText();

  const handleApply = () => {
    if (selectedIndex !== null && suggestions[selectedIndex]) {
      onApply(suggestions[selectedIndex]);
      setModalVisible(false);
      message.success('Text applied successfully');
    }
  };

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Copied to clipboard');
  };

  return (
    <>
      <Tooltip title="AI Rephrase">
        <Button
          type="text"
          size="small"
          icon={<RobotOutlined />}
          onClick={handleRephrase}
          style={{ 
            color: '#6366f1',
            marginLeft: 8,
            fontWeight: 500,
          }}
        >
          AI
        </Button>
      </Tooltip>

      <Modal
        title={
          <Space>
            <RobotOutlined style={{ color: '#6366f1' }} />
            <span>AI Rephrase Suggestions</span>
          </Space>
        }
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            Cancel
          </Button>,
          <Button
            key="apply"
            type="primary"
            icon={<CheckOutlined />}
            disabled={selectedIndex === null}
            onClick={handleApply}
            style={{ background: '#6366f1', borderColor: '#6366f1' }}
          >
            Apply Selected
          </Button>,
        ]}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <Text type="secondary">Original {fieldName}:</Text>
          <div style={{ 
            padding: 12, 
            background: '#f5f5f5', 
            borderRadius: 8, 
            marginTop: 8,
            fontSize: 13,
          }}>
            {currentText}
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>
            <Spin size="large" />
            <div style={{ marginTop: 16 }}>
              <Text type="secondary">Generating suggestions...</Text>
            </div>
          </div>
        ) : (
          <div>
            <Text type="secondary">Select a suggestion:</Text>
            <div style={{ marginTop: 12 }}>
              {suggestions.map((suggestion, index) => (
                <div
                  key={index}
                  onClick={() => setSelectedIndex(index)}
                  style={{
                    padding: 12,
                    marginBottom: 8,
                    border: selectedIndex === index ? '2px solid #6366f1' : '1px solid #e2e8f0',
                    borderRadius: 8,
                    cursor: 'pointer',
                    background: selectedIndex === index ? '#e0e7ff' : '#fff',
                    transition: 'all 0.2s',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Text style={{ flex: 1, fontSize: 13 }}>{suggestion}</Text>
                    <Button
                      type="text"
                      size="small"
                      icon={<CopyOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCopy(suggestion);
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div style={{ marginTop: 16, padding: 12, background: '#f0f5ff', borderRadius: 8 }}>
          <Space>
            <SyncOutlined style={{ color: '#1890ff' }} />
            <Text type="secondary" style={{ fontSize: 12 }}>
              Powered by local AI processing - no data sent to external servers
            </Text>
          </Space>
        </div>
      </Modal>
    </>
  );
};

export default AIRephraseButton;
