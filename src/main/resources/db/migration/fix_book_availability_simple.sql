-- Simple MySQL-Safe Fix for Book Availability
-- This version updates ALL books unconditionally (simpler, always works)

-- Step 1: Disable safe update mode for this session (optional)
SET SQL_SAFE_UPDATES = 0;

-- Step 2: Fix all books by recalculating available_copies
UPDATE books
SET available_copies = (
    total_copies - (
        SELECT COUNT(*) 
        FROM borrow_records 
        WHERE book_id = books.id 
        AND status = 'BORROWED'
    )
);

-- Step 3: Re-enable safe update mode (optional)
SET SQL_SAFE_UPDATES = 1;

-- Step 4: Verify the fix - check for any impossible values
SELECT 
    id,
    title,
    total_copies,
    available_copies,
    (SELECT COUNT(*) FROM borrow_records WHERE book_id = books.id AND status = 'BORROWED') as borrowed_count,
    (total_copies - available_copies) as calculated_borrowed
FROM books
WHERE available_copies > total_copies 
   OR available_copies < 0
ORDER BY id;
