ALTER TABLE profiles
    -- Personal
    ADD COLUMN profile_type VARCHAR(50),
    ADD COLUMN alternate_mobile VARCHAR(20),
    
    -- Birth Details
    ADD COLUMN birth_time TIME,
    ADD COLUMN birth_place VARCHAR(100),
    
    -- Horoscope
    ADD COLUMN rashi VARCHAR(50),
    ADD COLUMN nakshatra VARCHAR(50),
    ADD COLUMN gan VARCHAR(50),
    ADD COLUMN varna VARCHAR(50),
    ADD COLUMN nadi VARCHAR(50),
    ADD COLUMN charan VARCHAR(50),
    
    -- Physical
    ADD COLUMN body_type VARCHAR(50),
    ADD COLUMN complexion VARCHAR(50),
    ADD COLUMN spectacles VARCHAR(50),
    ADD COLUMN disability BOOLEAN DEFAULT FALSE,
    ADD COLUMN disability_details TEXT,
    
    -- Education/Career
    ADD COLUMN designation VARCHAR(100),
    ADD COLUMN work_location VARCHAR(100),
    
    -- Passport
    ADD COLUMN passport_available BOOLEAN DEFAULT FALSE,
    ADD COLUMN passport_number VARCHAR(50),
    
    -- Lifestyle
    ADD COLUMN hobbies TEXT,
    ADD COLUMN interests TEXT,
    ADD COLUMN skills TEXT,
    ADD COLUMN languages_known TEXT,
    
    -- Family
    ADD COLUMN father_name VARCHAR(100),
    ADD COLUMN father_occupation VARCHAR(100),
    ADD COLUMN mother_name VARCHAR(100),
    ADD COLUMN mother_occupation VARCHAR(100),
    ADD COLUMN brothers INT DEFAULT 0,
    ADD COLUMN sisters INT DEFAULT 0,
    ADD COLUMN maternal_uncle VARCHAR(100),
    ADD COLUMN mama_kul VARCHAR(100),
    ADD COLUMN family_status VARCHAR(50),
    ADD COLUMN native_place VARCHAR(100),
    
    -- Partner Preferences
    ADD COLUMN partner_marriage_preference VARCHAR(100),
    ADD COLUMN partner_age_from INT,
    ADD COLUMN partner_age_to INT,
    ADD COLUMN partner_height DOUBLE,
    ADD COLUMN partner_education VARCHAR(255),
    ADD COLUMN partner_occupation VARCHAR(255),
    ADD COLUMN partner_city VARCHAR(100),
    ADD COLUMN partner_state VARCHAR(100),
    ADD COLUMN partner_other_expectations TEXT,
    
    -- Verification
    ADD COLUMN verification_status VARCHAR(50) DEFAULT 'DRAFT',
    ADD COLUMN rejection_reason TEXT;
