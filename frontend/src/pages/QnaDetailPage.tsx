import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Typography, Tag, Button, Space, Divider, Input, message, Spin, Empty, Upload, Popconfirm, Image } from 'antd';
import { ArrowLeftOutlined, LikeOutlined, CheckCircleOutlined, PaperClipOutlined, LinkOutlined, UploadOutlined, DeleteOutlined, FileOutlined, FilePdfOutlined, FileWordOutlined, FileExcelOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import { qnaService, QnaQuestionResponse, AttachmentResponse } from '../services/qnaService';
import { useAuth } from '../contexts/AuthContext';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const API_BASE = '/api';

const QnaDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();
  const [question, setQuestion] = useState<QnaQuestionResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [answerContent, setAnswerContent] = useState('');
  const [submittingAnswer, setSubmittingAnswer] = useState(false);
  const [commentContent, setCommentContent] = useState<Record<string, string>>({});
  const [submittingComment, setSubmittingComment] = useState<string | null>(null);
  const [answerFileList, setAnswerFileList] = useState<UploadFile[]>([]);
  const [answerHyperlinks, setAnswerHyperlinks] = useState<{ url: string; title: string }[]>([]);
  const [newLinkUrl, setNewLinkUrl] = useState('');
  const [newLinkTitle, setNewLinkTitle] = useState('');

  useEffect(() => {
    if (id) loadQuestion();
  }, [id]);

  const loadQuestion = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await qnaService.getQuestion(id);
      setQuestion(data);
    } catch (error) {
      message.error('Failed to load question');
    } finally {
      setLoading(false);
    }
  };

  const handleUpvoteQuestion = async () => {
    if (!id) return;
    try {
      await qnaService.upvoteQuestion(id);
      loadQuestion();
    } catch (error) {
      message.error('Failed to upvote');
    }
  };

  const handleUpvoteAnswer = async (answerId: string) => {
    try {
      await qnaService.upvoteAnswer(answerId);
      loadQuestion();
    } catch (error) {
      message.error('Failed to upvote');
    }
  };

  const handleAcceptAnswer = async (answerId: string) => {
    try {
      await qnaService.acceptAnswer(answerId);
      message.success('Answer accepted!');
      loadQuestion();
    } catch (error) {
      message.error('Failed to accept answer');
    }
  };

  const handleSubmitAnswer = async () => {
    if (!id || !answerContent.trim()) return;
    setSubmittingAnswer(true);
    try {
      const answer = await qnaService.createAnswer({ 
        questionId: id, 
        content: answerContent,
        hyperlinks: answerHyperlinks.length > 0 ? answerHyperlinks : undefined,
      });

      // Upload files if any
      for (const file of answerFileList) {
        if (file.originFileObj) {
          await qnaService.uploadAttachment(file.originFileObj, undefined, answer.id);
        }
      }

      message.success('Answer posted!');
      setAnswerContent('');
      setAnswerFileList([]);
      setAnswerHyperlinks([]);
      loadQuestion();
    } catch (error) {
      message.error('Failed to post answer');
    } finally {
      setSubmittingAnswer(false);
    }
  };

  const addAnswerHyperlink = () => {
    if (newLinkUrl.trim()) {
      setAnswerHyperlinks([...answerHyperlinks, { url: newLinkUrl.trim(), title: newLinkTitle.trim() || newLinkUrl.trim() }]);
      setNewLinkUrl('');
      setNewLinkTitle('');
    }
  };

  const removeAnswerHyperlink = (index: number) => {
    setAnswerHyperlinks(answerHyperlinks.filter((_, i) => i !== index));
  };

  const isImageFile = (fileName: string) => {
    const ext = fileName.toLowerCase().split('.').pop();
    return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext || '');
  };

  const getFileIcon = (fileName: string) => {
    const ext = fileName.toLowerCase().split('.').pop();
    if (['pdf'].includes(ext || '')) return <FilePdfOutlined style={{ fontSize: 24, color: '#ff4d4f' }} />;
    if (['doc', 'docx'].includes(ext || '')) return <FileWordOutlined style={{ fontSize: 24, color: '#1890ff' }} />;
    if (['xls', 'xlsx'].includes(ext || '')) return <FileExcelOutlined style={{ fontSize: 24, color: '#52c41a' }} />;
    return <FileOutlined style={{ fontSize: 24, color: '#8c8c8c' }} />;
  };

  const renderAttachments = (attachments: AttachmentResponse[] | undefined) => {
    if (!attachments || attachments.length === 0) return null;
    
    return (
      <div style={{ marginTop: 12 }}>
        <Text type="secondary"><PaperClipOutlined /> Attachments:</Text>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginTop: 8 }}>
          {attachments.map((att) => {
            const isImage = isImageFile(att.fileName);
            const fileUrl = `${API_BASE}${att.fileUrl}`;
            
            if (isImage) {
              return (
                <div key={att.id} style={{ textAlign: 'center' }}>
                  <Image
                    src={fileUrl}
                    alt={att.fileName}
                    width={120}
                    height={120}
                    style={{ objectFit: 'cover', borderRadius: 4 }}
                    preview={{ src: fileUrl }}
                  />
                  <div style={{ fontSize: 11, color: '#888', marginTop: 4 }}>{att.fileName}</div>
                </div>
              );
            }
            
            return (
              <a 
                key={att.id} 
                href={fileUrl} 
                target="_blank" 
                rel="noopener noreferrer"
                style={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center', 
                  padding: 12, 
                  border: '1px solid #d9d9d9', 
                  borderRadius: 4,
                  minWidth: 80,
                  textDecoration: 'none'
                }}
              >
                {getFileIcon(att.fileName)}
                <div style={{ fontSize: 11, color: '#888', marginTop: 4, maxWidth: 80, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {att.fileName}
                </div>
              </a>
            );
          })}
        </div>
      </div>
    );
  };

  const handleSubmitComment = async (answerId: string) => {
    const content = commentContent[answerId];
    if (!content?.trim()) return;
    setSubmittingComment(answerId);
    try {
      await qnaService.createComment({ answerId, content });
      message.success('Comment added!');
      setCommentContent(prev => ({ ...prev, [answerId]: '' }));
      loadQuestion();
    } catch (error) {
      message.error('Failed to add comment');
    } finally {
      setSubmittingComment(null);
    }
  };

  const handleDeleteQuestion = async () => {
    if (!id) return;
    try {
      await qnaService.deleteQuestion(id);
      message.success('Question deleted');
      navigate('/qna');
    } catch (error) {
      message.error('Failed to delete question');
    }
  };

  const isOwner = question?.createdBy?.id === user?.id;

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!question) {
    return <Empty description="Question not found" />;
  }

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/qna')} style={{ marginBottom: 16 }}>
        Back to Q&A
      </Button>

      <Card>
        <div style={{ display: 'flex', gap: 16 }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 60 }}>
            <Button type="text" icon={<LikeOutlined />} onClick={handleUpvoteQuestion}>
              {question.upvotes}
            </Button>
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                {question.isResolved && (
                  <Tag color="success" icon={<CheckCircleOutlined />} style={{ marginBottom: 8 }}>Resolved</Tag>
                )}
                <Title level={3} style={{ marginBottom: 8 }}>{question.title}</Title>
              </div>
              {(isOwner || isAdmin) && (
                <Popconfirm title="Delete this question?" onConfirm={handleDeleteQuestion}>
                  <Button danger icon={<DeleteOutlined />}>Delete</Button>
                </Popconfirm>
              )}
            </div>
            
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{question.content}</Paragraph>

            {question.tags?.length > 0 && (
              <Space wrap style={{ marginBottom: 16 }}>
                {question.tags.map((tag, idx) => (
                  <Tag key={idx} color="blue">{tag}</Tag>
                ))}
              </Space>
            )}

            {question.hyperlinks?.length > 0 && (
              <div style={{ marginBottom: 16 }}>
                <Text type="secondary"><LinkOutlined /> Links:</Text>
                <Space wrap style={{ marginLeft: 8 }}>
                  {question.hyperlinks.map((link) => (
                    <a key={link.id} href={link.url} target="_blank" rel="noopener noreferrer">
                      {link.title || link.url}
                    </a>
                  ))}
                </Space>
              </div>
            )}

            {renderAttachments(question.attachments)}

            <div style={{ marginTop: 16, color: '#888', fontSize: 12 }}>
              Asked {dayjs(question.createdAt).fromNow()} by <Text strong>{question.createdBy?.username}</Text>
              {' • '} {question.viewCount} views
            </div>
          </div>
        </div>
      </Card>

      <Divider orientation="left">{question.answerCount} Answer{question.answerCount !== 1 ? 's' : ''}</Divider>

      {question.answers?.map((answer) => (
        <Card key={answer.id} style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', gap: 16 }}>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 60 }}>
              <Button type="text" icon={<LikeOutlined />} onClick={() => handleUpvoteAnswer(answer.id)}>
                {answer.upvotes}
              </Button>
              {answer.isAccepted ? (
                <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 24 }} />
              ) : isOwner && (
                <Button type="text" size="small" onClick={() => handleAcceptAnswer(answer.id)}>
                  Accept
                </Button>
              )}
            </div>
            <div style={{ flex: 1 }}>
              <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{answer.content}</Paragraph>

              {answer.hyperlinks?.length > 0 && (
                <div style={{ marginBottom: 8 }}>
                  <Space wrap>
                    {answer.hyperlinks.map((link) => (
                      <a key={link.id} href={link.url} target="_blank" rel="noopener noreferrer">
                        <LinkOutlined /> {link.title || link.url}
                      </a>
                    ))}
                  </Space>
                </div>
              )}

              {renderAttachments(answer.attachments)}

              <div style={{ color: '#888', fontSize: 12, marginBottom: 16 }}>
                Answered {dayjs(answer.createdAt).fromNow()} by <Text strong>{answer.createdBy?.username}</Text>
              </div>

              {answer.comments?.length > 0 && (
                <div style={{ background: '#fafafa', padding: 12, borderRadius: 4, marginBottom: 12 }}>
                  {answer.comments.map((comment) => (
                    <div key={comment.id} style={{ marginBottom: 8, fontSize: 13 }}>
                      <Text>{comment.content}</Text>
                      <Text type="secondary" style={{ marginLeft: 8 }}>
                        – {comment.createdBy?.username} {dayjs(comment.createdAt).fromNow()}
                      </Text>
                    </div>
                  ))}
                </div>
              )}

              <div style={{ display: 'flex', gap: 8 }}>
                <Input
                  size="small"
                  placeholder="Add a comment..."
                  value={commentContent[answer.id] || ''}
                  onChange={(e) => setCommentContent(prev => ({ ...prev, [answer.id]: e.target.value }))}
                  onPressEnter={() => handleSubmitComment(answer.id)}
                  style={{ flex: 1 }}
                />
                <Button 
                  size="small" 
                  onClick={() => handleSubmitComment(answer.id)}
                  loading={submittingComment === answer.id}
                >
                  Comment
                </Button>
              </div>
            </div>
          </div>
        </Card>
      ))}

      <Card title="Your Answer" style={{ marginTop: 24 }}>
        <TextArea
          rows={6}
          value={answerContent}
          onChange={(e) => setAnswerContent(e.target.value)}
          placeholder="Write your answer here..."
        />

        <div style={{ marginTop: 16 }}>
          <Text strong>Attachments</Text>
          <Upload
            fileList={answerFileList}
            onChange={({ fileList }) => setAnswerFileList(fileList)}
            beforeUpload={() => false}
            multiple
            listType="picture"
            style={{ marginTop: 8 }}
          >
            <Button icon={<UploadOutlined />}>Add Files (Images, PDF, DOCX, etc.)</Button>
          </Upload>
        </div>

        <div style={{ marginTop: 16 }}>
          <Text strong>Hyperlinks</Text>
          <Space direction="vertical" style={{ width: '100%', marginTop: 8 }}>
            {answerHyperlinks.map((link, index) => (
              <div key={index} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <LinkOutlined />
                <a href={link.url} target="_blank" rel="noopener noreferrer" style={{ flex: 1 }}>
                  {link.title || link.url}
                </a>
                <Button type="text" danger icon={<DeleteOutlined />} onClick={() => removeAnswerHyperlink(index)} />
              </div>
            ))}
            <Space.Compact style={{ width: '100%' }}>
              <Input
                placeholder="URL (e.g., https://example.com)"
                value={newLinkUrl}
                onChange={(e) => setNewLinkUrl(e.target.value)}
                style={{ flex: 2 }}
              />
              <Input
                placeholder="Title (optional)"
                value={newLinkTitle}
                onChange={(e) => setNewLinkTitle(e.target.value)}
                style={{ flex: 1 }}
              />
              <Button type="primary" onClick={addAnswerHyperlink} disabled={!newLinkUrl.trim()}>
                Add
              </Button>
            </Space.Compact>
          </Space>
        </div>

        <Button 
          type="primary" 
          style={{ marginTop: 16 }}
          onClick={handleSubmitAnswer}
          loading={submittingAnswer}
          disabled={!answerContent.trim()}
        >
          Post Your Answer
        </Button>
      </Card>
    </div>
  );
};

export default QnaDetailPage;
