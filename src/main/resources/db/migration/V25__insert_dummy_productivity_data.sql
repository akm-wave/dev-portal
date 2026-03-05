-- V25: Insert dummy data for productivity dashboard testing
-- This migration inserts sample data assigned to users for testing the productivity dashboard

DO $$
DECLARE
    admin_user_id UUID;
    test_user_id UUID;
    feature1_id UUID;
    feature2_id UUID;
    feature3_id UUID;
    microservice1_id UUID;
    microservice2_id UUID;
BEGIN
    -- Get existing user IDs (assuming admin and at least one other user exist)
    SELECT id INTO admin_user_id FROM users WHERE username = 'admin' LIMIT 1;
    SELECT id INTO test_user_id FROM users WHERE role = 'USER' LIMIT 1;
    
    -- If no regular user exists, use admin for all assignments
    IF test_user_id IS NULL THEN
        test_user_id := admin_user_id;
    END IF;

    -- Only insert if admin user exists
    IF admin_user_id IS NOT NULL THEN
        
        -- Insert Features with owner assignments
        feature1_id := gen_random_uuid();
        feature2_id := gen_random_uuid();
        feature3_id := gen_random_uuid();
        
        INSERT INTO features (id, name, description, release_version, target_date, status, owner_id, created_at, updated_at, weight, completed_at)
        VALUES 
            (feature1_id, 'User Authentication Enhancement', 'Implement OAuth2 and SSO integration', 'v2.1.0', 
             CURRENT_DATE + INTERVAL '15 days', 'IN_PROGRESS', admin_user_id, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP, 10, NULL),
            (feature2_id, 'Dashboard Analytics Module', 'Real-time analytics dashboard with charts', 'v2.2.0', 
             CURRENT_DATE + INTERVAL '30 days', 'PLANNED', admin_user_id, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP, 10, NULL),
            (feature3_id, 'Mobile App API', 'RESTful API for mobile application', 'v2.1.0', 
             CURRENT_DATE - INTERVAL '2 days', 'IN_PROGRESS', test_user_id, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP, 10, NULL);

        -- Insert Microservices with owner assignments
        INSERT INTO microservices (id, name, description, version, status, owner_id, created_at, updated_at)
        VALUES 
            (gen_random_uuid(), 'Auth Service', 'Authentication and authorization microservice', 
             'v1.2.0', 'IN_PROGRESS', admin_user_id, 
             CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP),
            (gen_random_uuid(), 'Payment Gateway Service', 'Payment processing microservice', 
             'v1.0.5', 'COMPLETED', admin_user_id, 
             CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP),
            (gen_random_uuid(), 'Notification Service', 'Email and SMS notification service', 
             'v2.0.0', 'IN_PROGRESS', test_user_id, 
             CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP);

        -- Insert Incidents with owner assignments
        IF feature1_id IS NOT NULL THEN
            INSERT INTO incidents (id, title, description, severity, status, main_feature_id, owner_id, created_at, updated_at, due_date)
            VALUES 
                (gen_random_uuid(), 'Login timeout issue', 'Users experiencing timeout during login', 
                 'HIGH', 'OPEN', feature1_id, admin_user_id, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP, 
                 CURRENT_DATE + INTERVAL '3 days'),
                (gen_random_uuid(), 'Session management bug', 'Session not persisting across tabs', 
                 'MEDIUM', 'IN_PROGRESS', feature1_id, admin_user_id, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP, 
                 CURRENT_DATE + INTERVAL '5 days');
        END IF;

        -- Insert Hotfixes with owner assignments
        IF feature2_id IS NOT NULL THEN
            INSERT INTO hotfixes (id, title, description, release_version, status, main_feature_id, owner_id, created_at, updated_at, due_date)
            VALUES 
                (gen_random_uuid(), 'Critical security patch', 'Fix SQL injection vulnerability', 
                 'v2.0.1', 'IN_PROGRESS', feature2_id, admin_user_id, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 
                 CURRENT_DATE + INTERVAL '2 days');
        END IF;

        -- Insert Issues with assigned_to
        IF feature3_id IS NOT NULL THEN
            INSERT INTO issues (id, title, description, priority, status, main_feature_id, assigned_to, owner_id, created_at, updated_at, due_date)
            VALUES 
                (gen_random_uuid(), 'API documentation incomplete', 'Missing endpoint documentation for mobile API', 
                 'MEDIUM', 'OPEN', feature3_id, test_user_id, test_user_id, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP, 
                 CURRENT_DATE + INTERVAL '7 days'),
                (gen_random_uuid(), 'Rate limiting not working', 'API rate limiting configuration issue', 
                 'HIGH', 'IN_PROGRESS', feature3_id, admin_user_id, admin_user_id, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP, 
                 CURRENT_DATE + INTERVAL '4 days');
        END IF;

        -- Insert some completed items for productivity score calculation
        INSERT INTO features (id, name, description, release_version, target_date, status, owner_id, created_at, updated_at, weight, completed_at)
        VALUES 
            (gen_random_uuid(), 'Email Notification System', 'Automated email notifications', 'v2.0.0', 
             CURRENT_DATE - INTERVAL '5 days', 'COMPLETED', admin_user_id, CURRENT_TIMESTAMP - INTERVAL '30 days', 
             CURRENT_TIMESTAMP - INTERVAL '5 days', 10, CURRENT_TIMESTAMP - INTERVAL '5 days'),
            (gen_random_uuid(), 'User Profile Management', 'Enhanced user profile features', 'v2.0.0', 
             CURRENT_DATE - INTERVAL '10 days', 'COMPLETED', test_user_id, CURRENT_TIMESTAMP - INTERVAL '25 days', 
             CURRENT_TIMESTAMP - INTERVAL '10 days', 10, CURRENT_TIMESTAMP - INTERVAL '10 days');

        -- Insert completed incidents
        IF feature1_id IS NOT NULL THEN
            INSERT INTO incidents (id, title, description, severity, status, main_feature_id, owner_id, created_at, updated_at, resolved_at, due_date)
            VALUES 
                (gen_random_uuid(), 'Password reset not working', 'Fixed password reset email issue', 
                 'HIGH', 'RESOLVED', feature1_id, admin_user_id, CURRENT_TIMESTAMP - INTERVAL '15 days', 
                 CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_DATE - INTERVAL '12 days');
        END IF;

        RAISE NOTICE 'Dummy productivity data inserted successfully for users: admin_id=%, test_user_id=%', admin_user_id, test_user_id;
    ELSE
        RAISE NOTICE 'No admin user found, skipping dummy data insertion';
    END IF;
END $$;
