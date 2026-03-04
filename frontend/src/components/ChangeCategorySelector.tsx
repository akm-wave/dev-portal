import React, { useEffect, useState } from 'react';
import { Select, Card, Button, Space, Input, Tag, message, Spin } from 'antd';
import { PlusOutlined, DeleteOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { impactService, ChangeType, MicroserviceChangeRequest, ImpactAnalysisResponse } from '../services/impactService';
import { microserviceService } from '../services/microserviceService';

const { TextArea } = Input;

interface Microservice {
  id: string;
  name: string;
}

interface ChangeCategoryEntry {
  microserviceId: string;
  changeTypes: string[];
  notes: string;
}

interface ChangeCategorySelectorProps {
  featureId?: string;
  incidentId?: string;
  hotfixId?: string;
  issueId?: string;
  onChange?: (changes: MicroserviceChangeRequest[]) => void;
  onImpactCalculated?: (impact: ImpactAnalysisResponse) => void;
}

const ChangeCategorySelector: React.FC<ChangeCategorySelectorProps> = ({
  featureId,
  incidentId,
  hotfixId,
  issueId,
  onChange,
  onImpactCalculated,
}) => {
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [changeTypes, setChangeTypes] = useState<ChangeType[]>([]);
  const [entries, setEntries] = useState<ChangeCategoryEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [calculating, setCalculating] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [msData, ctData] = await Promise.all([
          microserviceService.getAll(),
          impactService.getChangeTypes(),
        ]);
        setMicroservices(msData.content || []);
        setChangeTypes(ctData);
      } catch (error) {
        console.error('Failed to fetch data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (onChange) {
      onChange(entries.filter(e => e.microserviceId && e.changeTypes.length > 0));
    }
  }, [entries, onChange]);

  const addEntry = () => {
    setEntries([...entries, { microserviceId: '', changeTypes: [], notes: '' }]);
  };

  const removeEntry = (index: number) => {
    setEntries(entries.filter((_, i) => i !== index));
  };

  const updateEntry = (index: number, field: keyof ChangeCategoryEntry, value: any) => {
    const newEntries = [...entries];
    newEntries[index] = { ...newEntries[index], [field]: value };
    setEntries(newEntries);
  };

  const calculateImpact = async () => {
    const validEntries = entries.filter(e => e.microserviceId && e.changeTypes.length > 0);
    if (validEntries.length === 0) {
      message.warning('Please select at least one microservice with change types');
      return;
    }

    setCalculating(true);
    try {
      const response = await impactService.calculateImpact({
        featureId,
        incidentId,
        hotfixId,
        issueId,
        microserviceChanges: validEntries,
      });
      message.success('Impact analysis completed');
      if (onImpactCalculated) {
        onImpactCalculated(response);
      }
    } catch (error) {
      console.error('Failed to calculate impact:', error);
      message.error('Failed to calculate impact');
    } finally {
      setCalculating(false);
    }
  };

  const getChangeTypeColor = (value: string) => {
    const ct = changeTypes.find(c => c.value === value);
    if (!ct) return 'default';
    if (ct.riskWeight >= 5) return 'red';
    if (ct.riskWeight >= 4) return 'orange';
    if (ct.riskWeight >= 3) return 'blue';
    return 'green';
  };

  if (loading) {
    return <Card><Spin /></Card>;
  }

  return (
    <Card 
      title="Change Categories & Impact" 
      size="small"
      extra={
        <Button 
          type="primary" 
          icon={<ThunderboltOutlined />}
          onClick={calculateImpact}
          loading={calculating}
          disabled={entries.filter(e => e.microserviceId && e.changeTypes.length > 0).length === 0}
        >
          Calculate Impact
        </Button>
      }
    >
      <Space direction="vertical" style={{ width: '100%' }}>
        {entries.map((entry, index) => (
          <Card 
            key={index} 
            size="small" 
            style={{ background: '#fafafa' }}
            extra={
              <Button 
                type="text" 
                danger 
                icon={<DeleteOutlined />} 
                onClick={() => removeEntry(index)}
              />
            }
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              <Select
                placeholder="Select Microservice"
                style={{ width: '100%' }}
                value={entry.microserviceId || undefined}
                onChange={(value) => updateEntry(index, 'microserviceId', value)}
                showSearch
                optionFilterProp="children"
              >
                {microservices.map(ms => (
                  <Select.Option key={ms.id} value={ms.id}>{ms.name}</Select.Option>
                ))}
              </Select>
              
              <Select
                mode="multiple"
                placeholder="Select Change Types"
                style={{ width: '100%' }}
                value={entry.changeTypes}
                onChange={(value) => updateEntry(index, 'changeTypes', value)}
                tagRender={(props) => (
                  <Tag color={getChangeTypeColor(props.value as string)} closable={props.closable} onClose={props.onClose}>
                    {props.label}
                  </Tag>
                )}
              >
                {changeTypes.map(ct => (
                  <Select.Option key={ct.value} value={ct.value}>
                    {ct.label} (Risk: {ct.riskWeight})
                  </Select.Option>
                ))}
              </Select>

              <TextArea
                placeholder="Notes (optional)"
                rows={1}
                value={entry.notes}
                onChange={(e) => updateEntry(index, 'notes', e.target.value)}
              />
            </Space>
          </Card>
        ))}

        <Button 
          type="dashed" 
          onClick={addEntry} 
          block 
          icon={<PlusOutlined />}
        >
          Add Microservice Change
        </Button>
      </Space>
    </Card>
  );
};

export default ChangeCategorySelector;
