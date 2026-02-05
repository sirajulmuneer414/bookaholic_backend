-- Database Migration for OTP Email Verification
-- Execute this SQL script to add OTP fields to the users table

-- Add new columns for OTP verification
ALTER TABLE users
ADD COLUMN otp_code VARCHAR(6),
ADD COLUMN otp_expiry TIMESTAMP,
ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;

-- Set existing users as verified (migration only)
-- Step 2: Set existing users as verified (since they registered before OTP system)
-- Note: Using id >= 0 to satisfy MySQL safe update mode (uses primary key)
UPDATE users 
SET is_verified = TRUE 
WHERE id >= 0  -- Uses primary key to satisfy MySQL safe update mode
  AND is_verified IS NULL;

-- Create indexes for better query performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_otp_code ON users(otp_code);
CREATE INDEX idx_users_is_verified ON users(is_verified);

-- Verify the changes
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as verified_users FROM users WHERE is_verified = TRUE;
SELECT COUNT(*) as unverified_users FROM users WHERE is_verified = FALSE;
