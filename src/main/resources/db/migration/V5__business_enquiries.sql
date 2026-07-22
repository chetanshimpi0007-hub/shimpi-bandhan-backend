-- V5__business_enquiries.sql

CREATE TABLE business_enquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    wedding_date DATE NOT NULL,
    city VARCHAR(100) NOT NULL,
    estimated_budget DOUBLE,
    preferred_contact_time VARCHAR(100),
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW', -- NEW, CONTACTED, NEGOTIATING, BOOKED, COMPLETED, REJECTED, CANCELLED
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW', -- HIGH, MEDIUM, LOW
    lead_score INT NOT NULL DEFAULT 0,
    first_response_time_ms BIGINT,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (business_id) REFERENCES businesses(id),
    FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE business_enquiry_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (enquiry_id) REFERENCES business_enquiries(id)
);

CREATE TABLE business_enquiry_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (enquiry_id) REFERENCES business_enquiries(id)
);

CREATE TABLE business_enquiry_meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    meeting_date DATE NOT NULL,
    meeting_time TIME NOT NULL,
    meeting_type VARCHAR(50) NOT NULL, -- SHOP_VISIT, VIDEO_MEETING, PHONE_CALL
    address_or_link TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (enquiry_id) REFERENCES business_enquiries(id)
);

CREATE TABLE business_follow_up (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    due_date DATE NOT NULL,
    task VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED
    reminder BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (enquiry_id) REFERENCES business_enquiries(id)
);
