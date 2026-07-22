CREATE TABLE business_offer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    discount VARCHAR(50),
    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,
    banner_url VARCHAR(255),
    terms_and_conditions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES business(id)
);

ALTER TABLE business
    ADD COLUMN is_admin_featured BOOLEAN DEFAULT FALSE,
    ADD COLUMN priority_override INT DEFAULT 0,
    ADD COLUMN working_hours_json TEXT,
    ADD COLUMN social_links_json TEXT;
