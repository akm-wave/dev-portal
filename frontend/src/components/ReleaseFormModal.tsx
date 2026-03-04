import React, { useState, useEffect } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  Button,
  Space,
  Card,
  message,
  Divider,
  Row,
  Col,
  Typography,
} from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  Release,
  ReleaseRequest,
  ReleaseStatus,
  ReleaseMicroserviceRequest,
  ReleaseLinkRequest,
  ReleaseLinkType,
  Microservice,
  Feature,
  Incident,
  Hotfix,
  Issue,
} from '../types';
import releaseService from '../services/releaseService';
import { microserviceService } from '../services/microserviceService';
import { featureService } from '../services/featureService';
import { incidentService } from '../services/incidentService';
import { hotfixService } from '../services/hotfixService';
import { issueService } from '../services/issueService';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { Option } = Select;
const { Text } = Typography;

interface ReleaseFormModalProps {
  visible: boolean;
  release: Release | null;
  onCancel: () => void;
  onSuccess: () => void;
}

interface MicroserviceEntry {
  key: string;
  microserviceId: string;
  branchName: string;
  buildNumber: string;
  releaseDate: string | null;
  notes: string;
}

interface LinkEntry {
  key: string;
  entityType: ReleaseLinkType;
  entityId: string;
}

const ReleaseFormModal: React.FC<ReleaseFormModalProps> = ({
  visible,
  release,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [microservices, setMicroservices] = useState<Microservice[]>([]);
  const [features, setFeatures] = useState<Feature[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [hotfixes, setHotfixes] = useState<Hotfix[]>([]);
  const [issues, setIssues] = useState<Issue[]>([]);
  const [msEntries, setMsEntries] = useState<MicroserviceEntry[]>([]);
  const [linkEntries, setLinkEntries] = useState<LinkEntry[]>([]);

  useEffect(() => {
    if (visible) {
      loadOptions();
      if (release) {
        form.setFieldsValue({
          name: release.name,
          version: release.version,
          releaseDate: release.releaseDate ? dayjs(release.releaseDate) : null,
          description: release.description,
          status: release.status,
          oldBuildNumber: release.oldBuildNumber,
          featureBranch: release.featureBranch,
        });
        setMsEntries(
          release.microservices?.map((ms, idx) => ({
            key: `ms-${idx}`,
            microserviceId: ms.microserviceId,
            branchName: ms.branchName || '',
            buildNumber: ms.buildNumber || '',
            releaseDate: ms.releaseDate,
            notes: ms.notes || '',
          })) || []
        );
        setLinkEntries(
          release.links?.map((link, idx) => ({
            key: `link-${idx}`,
            entityType: link.entityType,
            entityId: link.entityId,
          })) || []
        );
      } else {
        form.resetFields();
        setMsEntries([]);
        setLinkEntries([]);
      }
    }
  }, [visible, release, form]);

  const loadOptions = async () => {
    try {
      const [msRes, featRes, incRes, hotRes, issRes] = await Promise.all([
        microserviceService.getAll({ page: 0, size: 100 }),
        featureService.getAll({ page: 0, size: 100 }),
        incidentService.getAll({ page: 0, size: 100 }),
        hotfixService.getAll({ page: 0, size: 100 }),
        issueService.getAll({ page: 0, size: 100 }),
      ]);
      setMicroservices(msRes.content);
      setFeatures(featRes.content);
      setIncidents(incRes.content);
      setHotfixes(hotRes.content);
      setIssues(issRes.content);
    } catch (error) {
      message.error('Failed to load options');
    }
  };

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (msEntries.length === 0) {
      message.error('At least one microservice is required');
      return;
    }

    setLoading(true);
    try {
      const msRequests: ReleaseMicroserviceRequest[] = msEntries.map((entry) => ({
        microserviceId: entry.microserviceId,
        branchName: entry.branchName || undefined,
        buildNumber: entry.buildNumber || undefined,
        releaseDate: entry.releaseDate || undefined,
        notes: entry.notes || undefined,
      }));

      const linkRequests: ReleaseLinkRequest[] = linkEntries.map((entry) => ({
        entityType: entry.entityType,
        entityId: entry.entityId,
      }));

      const request: ReleaseRequest = {
        name: values.name as string,
        version: values.version as string,
        releaseDate: values.releaseDate ? (values.releaseDate as dayjs.Dayjs).toISOString() : undefined,
        description: values.description as string | undefined,
        status: values.status as ReleaseStatus | undefined,
        oldBuildNumber: values.oldBuildNumber as string | undefined,
        featureBranch: values.featureBranch as string | undefined,
        microservices: msRequests,
        links: linkRequests,
      };

      if (release) {
        await releaseService.update(release.id, request);
        message.success('Release updated successfully');
      } else {
        await releaseService.create(request);
        message.success('Release created successfully');
      }
      onSuccess();
    } catch (error) {
      message.error('Failed to save release');
    } finally {
      setLoading(false);
    }
  };

  const addMicroserviceEntry = () => {
    setMsEntries([
      ...msEntries,
      {
        key: `ms-${Date.now()}`,
        microserviceId: '',
        branchName: '',
        buildNumber: '',
        releaseDate: null,
        notes: '',
      },
    ]);
  };

  const removeMicroserviceEntry = (key: string) => {
    setMsEntries(msEntries.filter((e) => e.key !== key));
  };

  const updateMicroserviceEntry = (key: string, field: keyof MicroserviceEntry, value: string | null) => {
    setMsEntries(
      msEntries.map((e) => (e.key === key ? { ...e, [field]: value } : e))
    );
  };

  const addLinkEntry = () => {
    setLinkEntries([
      ...linkEntries,
      {
        key: `link-${Date.now()}`,
        entityType: 'FEATURE',
        entityId: '',
      },
    ]);
  };

  const removeLinkEntry = (key: string) => {
    setLinkEntries(linkEntries.filter((e) => e.key !== key));
  };

  const updateLinkEntry = (key: string, field: keyof LinkEntry, value: string) => {
    setLinkEntries((prev) =>
      prev.map((e) => (e.key === key ? { ...e, [field]: value } : e))
    );
    
    // Auto-populate microservices when linking an entity
    if (field === 'entityId' && value) {
      const entry = linkEntries.find(e => e.key === key);
      if (entry) {
        autoPopulateMicroservices(entry.entityType, value);
      }
    }
  };

  const autoPopulateMicroservices = (entityType: ReleaseLinkType, entityId: string) => {
    let linkedMicroservices: string[] = [];
    
    switch (entityType) {
      case 'FEATURE':
        const feature = features.find(f => f.id === entityId);
        if (feature?.microservices) {
          linkedMicroservices = feature.microservices.map((m: { id: string }) => m.id);
        }
        break;
      case 'INCIDENT':
        const incident = incidents.find(i => i.id === entityId);
        if (incident?.microservices) {
          linkedMicroservices = incident.microservices.map((m: { id: string }) => m.id);
        }
        break;
      case 'HOTFIX':
        const hotfix = hotfixes.find(h => h.id === entityId);
        if (hotfix?.microservices) {
          linkedMicroservices = hotfix.microservices.map((m: { id: string }) => m.id);
        }
        break;
      case 'ISSUE':
        const issue = issues.find(i => i.id === entityId);
        // Issues are linked to features, get microservices from the main feature
        if (issue?.mainFeature) {
          const linkedFeature = features.find(f => f.id === issue.mainFeature.id);
          if (linkedFeature?.microservices) {
            linkedMicroservices = linkedFeature.microservices.map((m: { id: string }) => m.id);
          }
        }
        break;
    }
    
    // Add microservices that are not already in the list
    const existingMsIds = msEntries.map(e => e.microserviceId);
    const newMicroservices = linkedMicroservices.filter(msId => !existingMsIds.includes(msId));
    
    if (newMicroservices.length > 0) {
      const newEntries: MicroserviceEntry[] = newMicroservices.map((msId, idx) => ({
        key: `ms-auto-${Date.now()}-${idx}`,
        microserviceId: msId,
        branchName: '',
        buildNumber: '',
        releaseDate: null,
        notes: '',
      }));
      setMsEntries(prev => [...prev, ...newEntries]);
    }
  };

  const updateLinkEntryType = (key: string, entityType: ReleaseLinkType) => {
    setLinkEntries((prev) =>
      prev.map((e) => (e.key === key ? { ...e, entityType, entityId: '' } : e))
    );
  };

  const getLinkOptions = (entityType: ReleaseLinkType) => {
    switch (entityType) {
      case 'FEATURE':
        return features.map((f) => ({ value: f.id, label: f.name }));
      case 'INCIDENT':
        return incidents.map((i) => ({ value: i.id, label: i.title }));
      case 'HOTFIX':
        return hotfixes.map((h) => ({ value: h.id, label: h.title }));
      case 'ISSUE':
        return issues.map((i) => ({ value: i.id, label: i.title }));
      default:
        return [];
    }
  };

  return (
    <Modal
      title={release ? 'Edit Release' : 'Create Release'}
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={900}
      destroyOnHidden
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="name"
              label="Release Name"
              rules={[{ required: true, message: 'Please enter release name' }]}
            >
              <Input placeholder="e.g., Q1 2026 Release" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="version"
              label="Version"
              rules={[{ required: true, message: 'Please enter version' }]}
            >
              <Input placeholder="e.g., v1.3.2" />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="releaseDate" label="Release Date">
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="status" label="Status">
              <Select placeholder="Select status">
                <Option value="DRAFT">Draft</Option>
                <Option value="SCHEDULED">Scheduled</Option>
                <Option value="DEPLOYED">Deployed</Option>
                <Option value="ROLLED_BACK">Rolled Back</Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Form.Item name="description" label="Description">
          <TextArea rows={3} placeholder="Release notes or description" />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="oldBuildNumber" label="Old Build Number">
              <Input placeholder="e.g., build-1234" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="featureBranch" label="Feature Branch">
              <Input placeholder="e.g., feature/JIRA-123" />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">Microservices</Divider>
        <Card size="small" style={{ marginBottom: 16 }}>
          {msEntries.map((entry) => (
            <Row key={entry.key} gutter={8} style={{ marginBottom: 8 }}>
              <Col span={6}>
                <Select
                  placeholder="Select microservice"
                  style={{ width: '100%' }}
                  value={entry.microserviceId || undefined}
                  onChange={(val) => updateMicroserviceEntry(entry.key, 'microserviceId', val)}
                  showSearch
                  optionFilterProp="children"
                >
                  {microservices.map((ms) => (
                    <Option key={ms.id} value={ms.id}>
                      {ms.name}
                    </Option>
                  ))}
                </Select>
              </Col>
              <Col span={5}>
                <Input
                  placeholder="Branch name"
                  value={entry.branchName}
                  onChange={(e) => updateMicroserviceEntry(entry.key, 'branchName', e.target.value)}
                />
              </Col>
              <Col span={4}>
                <Input
                  placeholder="Build #"
                  value={entry.buildNumber}
                  onChange={(e) => updateMicroserviceEntry(entry.key, 'buildNumber', e.target.value)}
                />
              </Col>
              <Col span={6}>
                <Input
                  placeholder="Notes"
                  value={entry.notes}
                  onChange={(e) => updateMicroserviceEntry(entry.key, 'notes', e.target.value)}
                />
              </Col>
              <Col span={3}>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => removeMicroserviceEntry(entry.key)}
                />
              </Col>
            </Row>
          ))}
          <Button type="dashed" onClick={addMicroserviceEntry} icon={<PlusOutlined />}>
            Add Microservice
          </Button>
          {msEntries.length === 0 && (
            <Text type="danger" style={{ marginLeft: 16 }}>
              At least one microservice is required
            </Text>
          )}
        </Card>

        <Divider orientation="left">Linked Items</Divider>
        <Card size="small" style={{ marginBottom: 16 }}>
          {linkEntries.map((entry) => (
            <Row key={entry.key} gutter={8} style={{ marginBottom: 8 }}>
              <Col span={8}>
                <Select
                  placeholder="Select type"
                  style={{ width: '100%' }}
                  value={entry.entityType}
                  onChange={(val) => updateLinkEntryType(entry.key, val)}
                >
                  <Option value="FEATURE">Feature</Option>
                  <Option value="INCIDENT">Incident</Option>
                  <Option value="HOTFIX">Hotfix</Option>
                  <Option value="ISSUE">Issue</Option>
                </Select>
              </Col>
              <Col span={12}>
                <Select
                  placeholder="Select item"
                  style={{ width: '100%' }}
                  value={entry.entityId || undefined}
                  onChange={(val) => updateLinkEntry(entry.key, 'entityId', val)}
                  showSearch
                  optionFilterProp="children"
                >
                  {getLinkOptions(entry.entityType).map((opt) => (
                    <Option key={opt.value} value={opt.value}>
                      {opt.label}
                    </Option>
                  ))}
                </Select>
              </Col>
              <Col span={4}>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => removeLinkEntry(entry.key)}
                />
              </Col>
            </Row>
          ))}
          <Button type="dashed" onClick={addLinkEntry} icon={<PlusOutlined />}>
            Add Link
          </Button>
        </Card>

        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button onClick={onCancel}>Cancel</Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              {release ? 'Update' : 'Create'}
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ReleaseFormModal;
