-- Add missing domains that were hardcoded in the frontend
INSERT INTO domains (name, description, color_code, is_active, created_by) VALUES
    ('Wallet', 'Digital wallet services', '#52c41a', true, 'system'),
    ('Fraud', 'Fraud detection and prevention', '#f5222d', true, 'system'),
    ('Infrastructure', 'Infrastructure and DevOps', '#fa8c16', true, 'system'),
    ('Admin', 'Administration services', '#13c2c2', true, 'system'),
    ('Reporting', 'Reporting and analytics', '#eb2f96', true, 'system'),
    ('User Experience', 'User experience and UI', '#2f54eb', true, 'system'),
    ('Security', 'Security services', '#722ed1', true, 'system'),
    ('Integration', 'Third-party integrations', '#1890ff', true, 'system'),
    ('Notifications', 'Notification services', '#faad14', true, 'system'),
    ('Core Banking', 'Core banking services', '#52c41a', true, 'system')
ON CONFLICT (name) DO NOTHING;
